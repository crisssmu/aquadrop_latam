package aquadrop_latam.service;

import org.springframework.beans.factory.annotation.Autowired;

import aquadrop_latam.commands.CancelBookingCommand;
import aquadrop_latam.commands.ConfirmeBookingCommand;
import aquadrop_latam.commands.RequestBookingCommand;
import aquadrop_latam.controller.BookingDto;
import aquadrop_latam.events.BookingCancelledEvent;
import aquadrop_latam.events.BookingConfirmedEvent;
import aquadrop_latam.events.BookingRequestedEvent;
import aquadrop_latam.events.DeliveryCompletedEvent;
import aquadrop_latam.models.Address;
import aquadrop_latam.models.Booking;
import aquadrop_latam.models.BookingStatus;
import aquadrop_latam.repository.BookingRepository;
import jakarta.transaction.Transactional;

public class BookingService {

    @Autowired
    private CommandService commandService;
    @Autowired
    private BookingRepository bookingRepository;

    public Booking createBooking(BookingDto dto) {

        Booking booking = new Booking();
        booking.setUser_sub(dto.user_sub);
        booking.setVolume_liters(dto.volume_liters);
        booking.setStatus(BookingStatus.PENDING);

        Float priceEstimate = dto.price_estimate + (dto.volume_liters * 1.2f);
        booking.setPrice_estimate(priceEstimate);
        booking.setAmount(priceEstimate);

        Address address = new Address();
        address.setAddress(dto.address);
        address.setZone(dto.zone);
        booking.setAddress(address);

        Booking saved = bookingRepository.save(booking);

        // Emitir evento BookingRequested según diagrama
        BookingRequestedEvent bookingRequestedEvent = new BookingRequestedEvent(
                saved.getId(),
                saved.getUser_sub(),
                saved.getAmount(),
                saved.getStatus().name()
        );
        commandService.publishBookingEvent(bookingRequestedEvent);

        // Enviar comando Saga -> PaymentService
        RequestBookingCommand command = new RequestBookingCommand(
                saved.getId(),
                saved.getAmount(),
                saved.getStatus().name()
        );

        commandService.sendCreateBooking(command);

        return saved;
    }

    public Booking updateBooking(int id, BookingDto dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setVolume_liters(dto.volume_liters);
        booking.setPrice_estimate(dto.price_estimate);
        booking.setAmount(dto.amount);

        Address address = booking.getAddress();
        address.setAddress(dto.address);
        address.setZone(dto.zone);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(int bookingId) {
        return cancelBooking(bookingId, "USER_REQUESTED", BookingCancelledEvent.REFUND_REQUESTED);
    }

    @Transactional
    public Booking cancelBooking(int bookingId, String reason, String cancellationType) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

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

    @Transactional
    public Booking confirmBooking(int bookingId, String paymentId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        // Emitir evento BookingConfirmed basado en paymentAuthorized según diagrama
        BookingConfirmedEvent confirmedEvent = new BookingConfirmedEvent(
                bookingId,
                booking.getAmount(),
                paymentId,
                booking.getStatus().name()
        );
        commandService.publishBookingEvent(confirmedEvent);

        // Enviar comando al FleetService para asignar tanker
        ConfirmeBookingCommand command = new ConfirmeBookingCommand(bookingId);
        commandService.sendConfirmeBooking(command);

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

    public Booking getBookingById(int id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
}
