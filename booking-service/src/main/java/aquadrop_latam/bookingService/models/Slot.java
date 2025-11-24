package aquadrop_latam.bookingService.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(name = "slots")
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private Date date;
    private String window_start;
    private String window_end;
    private float capacity_liters;
    @Column(name = "available_quota")
    private int availableQuota;
    private int quantity;
    private int tanker_id;
    private String zone;
}
