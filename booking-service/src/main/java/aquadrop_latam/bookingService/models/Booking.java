package aquadrop_latam.bookingService.models;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "user_sub")
    private int userSub;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JsonBackReference
    private Address address;

    @Column(name = "volume_liters")
    private Float volumeLiters;

    @Column(name = "price_estimate")
    private Float priceEstimate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(name = "created_at")
    private Date createdAt;
    private Float amount;
    
    @OneToMany(mappedBy = "booking_id")
    @Column(name = "priority_tag")
    private PriorityTag priorityTag;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }
    
    public Float calculePrice() {
        return this.priceEstimate += this.volumeLiters * 1.2f;   
    }

}