package ie.tcd.smartlock.app.controller.vo.resp;

import lombok.Data;

@Data
public class AccessCodeRespMqtt {
    private Long deviceId;
    private Long codeId;
    private String code;
}
