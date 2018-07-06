package telematic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import telematic.dto.Message;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@Component
public class MessageUtil {

    private final Logger LOG = LoggerFactory.getLogger(MessageUtil.class);

    public void saveMessage(Long login, String data, JdbcTemplate jdbcTemplate) {
        Message message = new Message();
        message.setImei(login);
        message = parserMessage(message, data);
        String sql = "INSERT INTO public.messages (imei, timestamp_create, lat, lon, speed, course, altitude, satellites_count, hdop, params) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, message.getImei(), message.getTimestamp_create(), message.getLat(), message.getLon(), message.getSpeed(),
                message.getCourse(), message.getAltitude(), message.getSatellites_count(), message.getHdop(), message.getParams());
    }

    public Message parserMessage(Message message, String data) {
        String[] array = data.split(";");

        //время
        try {
            int year = Integer.parseInt(array[0].substring(4, 6));
            int month = Integer.parseInt(array[0].substring(2, 4));
            int dayOfMonth = Integer.parseInt(array[0].substring(0, 2));
            int hour = Integer.parseInt(array[1].substring(0, 2));
            int minute = Integer.parseInt(array[1].substring(2, 4));
            int second = Integer.parseInt(array[1].substring(4, 6));
            message.setTimestamp_create(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second));
        } catch (NumberFormatException e) {
            message.setTimestamp_create(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        }

        //широта
        String lat1 = array[2];
        try {
            int degrees = Integer.parseInt(lat1.substring(0, 2));
            double minutesAndSeconds = Double.parseDouble(lat1.substring(2, 10)) / 60;
            String lat2 = array[3];
            Double lat = degrees + minutesAndSeconds;
            if (lat2.equals("S")) lat = lat * (-1);
            message.setLat(lat);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //долгота
        String lon1 = array[4];
        try {
            int degrees = Integer.parseInt(lon1.substring(0, 3));
            double minutesAndSeconds = Double.parseDouble(lon1.substring(3, 11)) / 60;
            String lon2 = array[5];
            Double lon = degrees + minutesAndSeconds;
            if (lon2.equals("W")) lon = lon * (-1);
            message.setLon(lon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //скорость
        try {
            message.setSpeed(Double.parseDouble(array[6]));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //курс
        try {
            message.setCourse(Integer.parseInt(array[7]));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //высота над уровнем моря
        try {
            message.setAltitude(Integer.parseInt(array[8]));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //количество спутников
        try {
            message.setSatellites_count(Integer.parseInt(array[9]));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (array.length > 10) {
            //снижение точности в горизонтальной плоскости
            try {
                message.setHdop(Double.parseDouble(array[10]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            //цифровые входы
            //message.setInput(Integer.parseInt(array[11]));

            //цифровые выходы
            //message.setOutput(Integer.parseInt(array[12]));

            //набор параметров
            try {
                message.setParams(array[15]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return message;
    }

    public void saveBlackBox(Long login, String data, JdbcTemplate jdbcTemplate){
        String[] message = data.split("\\|");
        Arrays.stream(message).forEach(m -> saveMessage(login, m, jdbcTemplate));
    }
}
