package ie.tcd.scss.smartdoorlockbe.vo.req;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class AccessCodeGenerateReqVO {
    @NotNull(message = "deviceId cannot be null")
    private Long deviceId;
    private ZonedDateTime validFrom;
    @Future(message = "validTo must be a future time")
    private ZonedDateTime validTo;

    public void setValidFrom(ZonedDateTime validFrom) {
        // 当反序列化过程中 validFrom 字段为 null 时，使用当前时间
        this.validFrom = (validFrom == null ? ZonedDateTime.now() : validFrom);
    }
}