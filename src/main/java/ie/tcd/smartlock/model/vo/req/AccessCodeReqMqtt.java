package ie.tcd.smartlock.model.vo.req;

import ie.tcd.smartlock.model.entity.AccessCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessCodeReqMqtt {
    private Long deviceId;
    private Long codeId;
    private String code;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    public static AccessCodeReqMqtt of(AccessCode source) {
        if (source == null) return null;
        AccessCodeReqMqtt target = new AccessCodeReqMqtt();
        target.setDeviceId(source.getDeviceId());
        target.setCodeId(source.getCodeId());
        target.setCode(source.getCode());
        target.setValidFrom(source.getValidFrom());
        target.setValidTo(source.getValidTo());
        return target;
    }
}
