package telematic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import telematic.dto.Message;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class MessageUtil {

    private final Logger LOG = LoggerFactory.getLogger(MessageUtil.class);
    private String flag = "1\r\n";

    public String saveMessage(Long login, String data, JdbcTemplate jdbcTemplate) {
        Message message = new Message();
        message.setImei(login);
        message = parserMessage(message, data);
        if (message != null) {
            String sql = "INSERT INTO public.dirty_date (imei, timestamp_create, lat, lon, speed, course, altitude, satellites_count, hdop, params) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, message.getImei(), message.getTimestamp_create(), message.getLat(), message.getLon(), message.getSpeed(),
                    message.getCourse(), message.getAltitude(), message.getSatellites_count(), message.getHdop(), message.getParams());
        }
        return flag;
    }

    public Message parserMessage(Message message, String data) {
        String[] array;
        if (data.length() > 10) {
            array = data.split(";");
            //время
            final String date = array[0];
            final String time = array[1];
            final String dateStr = date + time;
            LocalDateTime datetime = null;
            try{
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
                datetime = LocalDateTime.parse(dateStr, formatter);
                message.setTimestamp_create(datetime);
            }
            catch (Exception e){
                e.printStackTrace();
                message.setTimestamp_create(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
                LOG.info("Incorrect time");
                flag = "0\r\n";
            }
            //широта
            String lat1 = array[2];
            String lat2 = array[3];
            boolean south = lat2.equals("S");
            message.setLat(parseLat(lat1, south));
            //долгота
            String lon1 = array[4];
            String lon2 = array[5];
            boolean west = lon2.equals("W");
            message.setLon(parseLon(lon1, west));
            //скорость
            try {
                if (!("".equals(array[6]) || "NA".equals(array[6]))) message.setSpeed(Double.parseDouble(array[6]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                flag = "11\r\n";
            }
            //курс
            try {
                if (!("".equals(array[7]) || "NA".equals(array[7]))) message.setCourse(Integer.parseInt(array[7]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                flag = "11\r\n";
            }
            //высота над уровнем моря
            try {
                if (!("".equals(array[8]) || "NA".equals(array[8]))) message.setAltitude((int)Double.parseDouble(array[8]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                flag = "11\r\n";
            }
            //количество спутников
            try {
                if (!("".equals(array[9]) || "NA".equals(array[9]))) message.setSatellites_count(Integer.parseInt(array[9]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                flag = "12\r\n";
            }
            if (array.length > 10) {
                //снижение точности в горизонтальной плоскости
                try {
                    if (!("".equals(array[10]) || "NA".equals(array[10]))) message.setHdop(Double.parseDouble(array[10]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    flag = "12\r\n";
                }
                //цифровые входы
                //message.setInput(Integer.parseInt(array[11]));
                //цифровые выходы
                //message.setOutput(Integer.parseInt(array[12]));
                //набор параметров
                try {
                    if (!("".equals(array[15]) || "NA".equals(array[15]))) message.setParams(array[15]);
                } catch (Exception e) {
                    e.printStackTrace();
                    flag = "15\r\n";
                }
            }
        } else {
            return null;
        }
        return message;
    }

    public void saveBlackBox(Long login, String data, JdbcTemplate jdbcTemplate){
        String[] message = data.split("\\|");
        Arrays.stream(message).forEach(m -> saveMessage(login, m, jdbcTemplate));
    }

    private Double parseLat(String latString, boolean negative) {

        if (latString.contains("NA")) return null;

        String[] dotSplitted = latString.split("\\.");
        if (dotSplitted.length > 1 && dotSplitted[0] != null && dotSplitted[1] != null) {
            try {
                Double grad = new Double(dotSplitted[0].substring(0, 2));
                Double min = new Double(dotSplitted[0].substring(2) + "." + dotSplitted[1]) / 60;
                Double lat = grad + min;
                if (negative) {
                    lat = lat * (-1);
                }
                return lat;
            } catch (java.lang.Throwable e) {
                flag = "10\r\n";
            }
        }
        return null;
    }

    private Double parseLon(String lonString, boolean negative) {
        String[] dotSplitted = lonString.split("\\.");
        if (dotSplitted.length > 1 && dotSplitted[0] != null && dotSplitted[1] != null) {
            try {
                Double grad = new Double(dotSplitted[0].substring(0, 3));
                Double min = new Double(dotSplitted[0].substring(3) + "." + dotSplitted[1]) / 60;
                Double lon = grad + min;
                if (negative) {
                    lon = lon * (-1);
                }
                return lon;
            } catch (Throwable e) {
                flag = "10\r\n";
            }
        }
        return null;
    }
}
