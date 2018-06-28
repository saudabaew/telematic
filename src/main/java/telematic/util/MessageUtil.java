package telematic.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import telematic.dto.Message;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class MessageUtil {

    public void fullPackage(Message message, String data, JdbcTemplate jdbcTemplate){
        LocalDateTime timestamp_create;
        Double lat;
        Double lon;
        Double speed;
        Integer course;
        Integer altitude;
        Integer satellites_count;
        Double hdop;
        String params;

        //время
        String[] array = data.substring(3).split(";");
        try {
            int year = Integer.parseInt(array[0].substring(4, 6));
            int month = Integer.parseInt(array[0].substring(2, 4));
            int dayOfMonth = Integer.parseInt(array[0].substring(0, 2));
            int hour = Integer.parseInt(array[1].substring(0, 2));
            int minute = Integer.parseInt(array[1].substring(2, 4));
            int second = Integer.parseInt(array[1].substring(4, 6));
            timestamp_create = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
        } catch (NumberFormatException e) {
            timestamp_create = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        }

        //широта
        String lat1 = array[2];
        try {
            int degrees = Integer.parseInt(lat1.substring(0, 2));
            double minutesAndSeconds = Double.parseDouble(lat1.substring(2, 10)) / 60;
            String lat2 = array[3];
            lat = degrees + minutesAndSeconds;
            if (lat2.equals("S")) lat = lat * (-1);
        } catch (Exception e) {
            lat = -1.0;
        }

        //долгота
        String lon1 = array[4];
        try {
            int degrees = Integer.parseInt(lon1.substring(0, 3));
            double minutesAndSeconds = Double.parseDouble(lon1.substring(3, 11)) / 60;
            String lon2 = array[5];
            lon = degrees + minutesAndSeconds;
            if (lon2.equals("W")) lon = lon * (-1);
        } catch (Exception e) {
            lon = -1.0;
        }

        //скорость
        try {
            speed = Double.parseDouble(array[6]);
        } catch (NumberFormatException e) {
            speed = -1.0;
        }

        //курс
        try {
            course = Integer.parseInt(array[7]);
        } catch (NumberFormatException e) {
            course = -1;
        }

        //высота над уровнем моря
        try {
            altitude = Integer.parseInt(array[8]);
        } catch (NumberFormatException e) {
            altitude = -1;
        }

        //количество спутников
        try {
            satellites_count = Integer.parseInt(array[9]);
        } catch (NumberFormatException e) {
            satellites_count = -1;
        }

        //снижение точности в горизонтальной плоскости
        try {
            hdop = Double.parseDouble(array[10]);
        } catch (NumberFormatException e) {
            hdop = -1.0;
        }

        //input = Integer.parseInt(array[11]);
        //output = Integer.parseInt(array[12]);

        //набор параметров
        try {
            params = array[15];
        } catch (Exception e) {
            params = "";
        }

        if (params.isEmpty()) {
            String sql = "INSERT INTO public.messages (imei, timestamp_create, lat, lon, speed, course, altitude, satellites_count, hdop) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, message.getImei(), timestamp_create, lat, lon, speed, course, altitude, satellites_count, hdop);
        } else {
            String sql = "INSERT INTO public.messages (imei, timestamp_create, lat, lon, speed, course, altitude, satellites_count, hdop, params) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, message.getImei(), timestamp_create, lat, lon, speed, course, altitude, satellites_count, hdop, params);
        }
    }

    public void shortPackage(Message message, String data, JdbcTemplate jdbcTemplate){

    }
}
