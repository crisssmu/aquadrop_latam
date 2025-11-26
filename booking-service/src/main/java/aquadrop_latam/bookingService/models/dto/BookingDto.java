package aquadrop_latam.bookingService.models.dto;

import lombok.Data;

@Data
public class BookingDto {
    public Integer bookingId;
    public float amount;
    public String status;
    public Integer userSub;
    public Float volumeLiters;
    public Float priceEstimate;
    public String zone;
    public String address;
    public Integer priorityTag = 1; // Default to DEFAULT priority tag
}
