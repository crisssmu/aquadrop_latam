package aquadrop_latam.bookingService.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import aquadrop_latam.bookingService.models.Booking;
import aquadrop_latam.bookingService.models.dto.BookingDto;
import aquadrop_latam.bookingService.service.BookingService;



@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<Booking> createBooking(@RequestBody BookingDto booking) {

        Booking created = bookingService.createBooking(booking);

        if (created == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();

        if (bookings == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(bookings);
    }
    

    @PutMapping("update/{id}")
    public ResponseEntity<Booking> updateBooking(@RequestBody BookingDto booking, @PathVariable int id) {

        Booking exist = bookingService.getBookingById(id);

        BookingDto dto = new BookingDto();
        dto.bookingId = booking.bookingId;
        dto.amount = booking.amount;
        dto.status = booking.status;
        dto.userSub = booking.userSub;
        dto.volumeLiters = booking.volumeLiters;
        dto.priceEstimate = booking.priceEstimate;
        dto.zone = booking.zone;
        dto.address = booking.address;

        if (exist == null) {
            return ResponseEntity.badRequest().build();
        }

        Booking updated = bookingService.updateBooking(dto);

        if (updated == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancelBooking(@RequestBody BookingDto booking, @PathVariable int id) {

        Booking exist = bookingService.getBookingById(id);

        if (exist == null) {
            return ResponseEntity.badRequest().body("Booking not found");
        }

        Booking cancelled = bookingService.cancelBookingUser(booking.bookingId);

        if (cancelled == null) {
            return ResponseEntity.badRequest().body("Proccess cancel failed");
        }
        
        return ResponseEntity.ok("Cancelled");
    }

    @GetMapping("{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable int id) {
        Booking booking = bookingService.getBookingById(id);

        if (booking == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(booking);
    }

    
}
