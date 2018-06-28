package telematic.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Message {
    private Long id;
    private LocalDateTime timestamp_create;                          // отправлено MTU
    private Double lat;                                     // широта
    private Double lon;                                     // долгота
    private String params;                                  // значения датчиков
    private Double speed;
    private Long imei;                                      // код imei
    private Double hdop;
    private Integer input;
    private Integer output;
    private Integer satellites_count;
    private Double altitude;
    private Long flags;
    private Integer course;
    private Date timestamp_edit;                            // получено ТП
    private Long unit;
}
