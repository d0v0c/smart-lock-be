package ie.tcd.scss.smartdoorlockbe.vo.resp;

import lombok.Data;

@Data
public class AccessCodeRespMqtt {
    private Long deviceId;
    private Long codeId;
    private String code;
}
