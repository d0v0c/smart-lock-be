package ie.tcd.scss.smartdoorlockbe.vo.req;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class AccessCodeGenerateReqVO {
    @NotNull(message = "deviceId 不能为空")
    private Long deviceId;
    private ZonedDateTime validFrom;
    @Future(message = "validTo 必须是将来的时间")
    private ZonedDateTime validTo;
}
