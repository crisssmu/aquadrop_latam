package aquadrop_latam.models;

import java.util.Date;
import java.util.List;

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

    private int user_sub;
    
    @ManyToOne
    private Address address;

    private Float volume_liters;
    private Float price_estimate;

    private int available_quota;
    private int quantity;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private Date created_at;
    private Float amount;
    @OneToMany(mappedBy = "booking_id")
    private List<PriorityTag> priority_tags;

    @PrePersist
    protected void onCreate() {
        this.created_at = new Date();
    }
    

}