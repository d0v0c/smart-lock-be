package ie.tcd.smartlock.app.controller.vo.req;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccessCodeGenerateReqVO {
    @NotNull(message = "deviceId cannot be null")
    private Long deviceId;
    private LocalDateTime validFrom;
    @Future(message = "validTo must be a future time")
    private LocalDateTime validTo;

    @AssertTrue(message = "validTo must be after validFrom")
    private boolean isValidRange() {
        if (validFrom == null || validTo == null) return true;
        return validTo.isAfter(validFrom);
    }
}