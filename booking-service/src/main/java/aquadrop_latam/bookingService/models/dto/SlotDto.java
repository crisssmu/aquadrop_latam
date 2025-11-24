package aquadrop_latam.bookingService.models.dto;

import java.util.Date;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SlotDto {
    private int id;
    private Date date;
    private String window_start;
    private String window_end;
    private int available_quota;
    private int quantity;
    private Float capacity_liters;
    private int tanker_id;
    private Float volumen_liters;
    private String zone;
}
