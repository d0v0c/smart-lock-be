package ie.tcd.scss.smartdoorlockbe.vo.req;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class AccessCodeReqMqtt {
    private Long deviceId;
    private Long codeId;
    private String code;
    private ZonedDateTime validFrom;
    private ZonedDateTime validTo;
}
