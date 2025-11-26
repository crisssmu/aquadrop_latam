package aquadrop_latam.bookingService.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import aquadrop_latam.bookingService.commands.CancelBookingCommand;
import aquadrop_latam.bookingService.commands.ConfirmeBookingCommand;
import aquadrop_latam.bookingService.commands.RequestBookingCommand;
import aquadrop_latam.bookingService.events.BookingCancelledEvent;
import aquadrop_latam.bookingService.events.BookingConfirmedEvent;
import aquadrop_latam.bookingService.events.BookingRequestedEvent;
import aquadrop_latam.bookingService.events.DeliveryCompletedEvent;
import aquadrop_latam.bookingService.models.Address;
import aquadrop_latam.bookingService.models.Booking;
import aquadrop_latam.bookingService.models.BookingStatus;
import aquadrop_latam.bookingService.models.PriorityTag;
import aquadrop_latam.bookingService.models.dto.BookingDto;
import aquadrop_latam.bookingService.repository.AddressRepository;
import aquadrop_latam.bookingService.repository.BookingRepository;
import aquadrop_latam.bookingService.repository.PriorityTagRepository;
import jakarta.transaction.Transactional;

@Service
public class BookingService {

    @Autowired
    private CommandService commandService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PriorityTagRepository priorityTagRepository;

    @SuppressWarnings("null")
    public Booking createBooking(BookingDto dto) {
        Booking booking = new Booking();
        booking.setUserSub(dto.userSub != null ? dto.userSub : 0);
        booking.setVolumeLiters(dto.volumeLiters);
        booking.setStatus(BookingStatus.PENDING);

        PriorityTag priorityTag = priorityTagRepository.findById(dto.priorityTag)
                .orElseThrow(() -> new RuntimeException("PriorityTag not found"));

        booking.setPriorityTag(priorityTag);
    
        Float priceEstimate = dto.priceEstimate != null ? dto.priceEstimate : 0f;
        priceEstimate += booking.getVolumeLiters() != null ? booking.getVolumeLiters() * 1.2f : 0;
        booking.setPriceEstimate(priceEstimate);
        booking.setAmount(priceEstimate);

        Address address = new Address();
        address.setAddress(dto.address);
        address.setZone(dto.zone);
        address = addressRepository.save(address);
        booking.setAddress(address);


        Booking saved = bookingRepository.save(booking);

        BookingRequestedEvent bookingRequestedEvent = new BookingRequestedEvent(
                saved.getId(),
                saved.getUserSub(),
                saved.getVolumeLiters(),
                saved.getAddress().getZone(),
                saved.getAmount(),
                saved.getStatus().name()
        );
        try {
            commandService.publishBookingEvent(bookingRequestedEvent);
        } catch (Exception e) {
            System.err.println("Error al publicar evento en RabbitMQ: " + e.getMessage());
            throw new RuntimeException("RabbitMQ no disponible", e);
        }

        // Enviar comando Saga -> PaymentService
        RequestBookingCommand command = new RequestBookingCommand(
                saved.getId(),
                saved.getUserSub(),
                saved.getVolumeLiters(),
                saved.getAddress().getZone(),
                saved.getAmount(),
                saved.getStatus().name()
        );

        commandService.sendCreateBooking(command);

        return saved;
    }

    public Booking updateBooking(BookingDto dto) {
        Booking booking = new Booking();
        booking.setVolumeLiters(dto.volumeLiters);
        booking.setPriceEstimate(dto.priceEstimate);
        booking.setAmount(dto.amount);

        Address address = booking.getAddress();
        address.setAddress(dto.address);
        address.setZone(dto.zone);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBookingUser(int bookingId) {
        return cancelBooking(bookingId, "USER_REQUESTED", BookingCancelledEvent.CANCEL_BOOKING);
    }

    @Transactional
    public Booking cancelBooking(int bookingId, String reason, String cancellationType) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if(reason.equals("PAYMENT_FAILED")) {
            // Enviar comando Saga -> PaymentService
            CancelBookingCommand command = new CancelBookingCommand(bookingId);
            commandService.sendCancelBooking(command);
        }
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        // Emitir evento BookingCancelled con tipo específico según diagrama
        BookingCancelledEvent cancelledEvent = new BookingCancelledEvent(
                bookingId,
                reason,
                cancellationType,
                booking.getAmount() // refundAmount
        );
        commandService.publishBookingEvent(cancelledEvent);

        // Enviar comando para reembolso si es necesario
        if (BookingCancelledEvent.REFUND_REQUESTED.equals(cancellationType)) {
            CancelBookingCommand command = new CancelBookingCommand(bookingId);
            commandService.sendCancelBooking(command);
        }

        return saved;
    }

    /**
     * Llamado cuando el pago fue autorizado
     * Envía comando al FleetService para asignar tanker
     */
    @Transactional
    public void onPaymentAuthorized(int bookingId, String paymentId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        
        // Actualizar el booking con la referencia del pago
        booking.setPaymentReference(paymentId);
        bookingRepository.save(booking);
        
        // Enviar comando al FleetService para asignar tanker
        ConfirmeBookingCommand command = new ConfirmeBookingCommand(bookingId);
        commandService.sendConfirmeBooking(command);
    }

    @Transactional
    public Booking confirmBooking(int bookingId, String assignmentId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        // Emitir evento BookingConfirmed - el tanker ya fue asignado
        BookingConfirmedEvent confirmedEvent = new BookingConfirmedEvent(
                bookingId,
                booking.getAmount(),
                assignmentId,
                booking.getStatus().name()
        );
        commandService.publishBookingEvent(confirmedEvent);

        return saved;
    }

    // Método para completar entrega
    @Transactional
    public Booking completeDelivery(int bookingId, int tankerId, Float volumeDelivered) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.DELIVERED);
        Booking saved = bookingRepository.save(booking);

        // Emitir evento DeliveryCompleted según diagrama
        DeliveryCompletedEvent deliveryEvent = new DeliveryCompletedEvent(
                bookingId,
                tankerId,
                new java.util.Date(),
                volumeDelivered
        );
        commandService.publishBookingEvent(deliveryEvent);

        return saved;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(int id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
}
