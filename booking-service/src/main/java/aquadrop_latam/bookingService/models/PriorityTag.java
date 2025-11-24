package aquadrop_latam.bookingService.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "priority_tag")
public class PriorityTag {
    private Integer id;
    @ManyToOne
    private Booking booking_id;
    private String type;
    private Integer score;
}
