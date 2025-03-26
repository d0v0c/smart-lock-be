package ie.tcd.scss.smartdoorlockbe.vo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessCodeReqMqtt {
    private Long deviceId;
    private String codeId;
    private String code;
    private ZonedDateTime validFrom;
    private ZonedDateTime validTo;
}
