package telematic.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Message {
    private Long id;
    private LocalDateTime timestamp_create;
    private Double lat;
    private Double lon;
    private String params;
    private Double speed;
    private Long imei;
    private Double hdop;
    private Integer input;
    private Integer output;
    private Integer satellites_count;
    private Integer altitude;
    private Long flags;
    private Integer course;
    private Date timestamp_edit;
    private Long unit;
}
