package telematic.multiThreadServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import telematic.util.MessageUtil;

import java.io.DataInputStream;
import java.net.Socket;

public class ServerClientThread extends Thread {

    private final Logger LOG = LoggerFactory.getLogger(ServerClientThread.class);

    private Socket socket;
    private MessageUtil messageUtil;
    private JdbcTemplate jdbcTemplate;
    private KafkaTemplate kafkaTemplate;
    private String topicName;

    public ServerClientThread(Socket socket, JdbcTemplate jdbcTemplate, MessageUtil messageUtil, KafkaTemplate kafkaTemplate, String topicName) {
        this.socket = socket;
        this.messageUtil = messageUtil;
        this.jdbcTemplate = jdbcTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void run() {
        Long login = 0L;
        try (DataInputStream inStream = new DataInputStream(socket.getInputStream())) {

            while (true) {
                String data = inStream.readLine();
                char type = data.charAt(1);

                switch (type) {
                    case 'L':
                        login = Long.parseLong(data.substring(3, 18));
                        //условие идентификации
                        //if (login != 123456789666666L) socket.close();
                        kafkaTemplate.send(topicName, data);
                        break;
                    case 'D':
                        if (login != 0) {
                            messageUtil.saveMessage(login, data.substring(3), jdbcTemplate);
                            kafkaTemplate.send(topicName, data);
                        } else {
                            socket.close();
                        }
                        break;
                    case 'S':
                        if (login != 0) {
                            messageUtil.saveMessage(login, data.substring(4), jdbcTemplate);
                            kafkaTemplate.send(topicName, data);
                        } else {
                            socket.close();
                        }
                        break;
                    case 'B':
                        if (login != 0) {
                            messageUtil.saveBlackBox(login, data.substring(3), jdbcTemplate);
                            kafkaTemplate.send(topicName, data);
                        } else {
                            socket.close();
                        }
                        break;
                    default:
                        socket.close();
                        break;
                }
                System.out.println("Client send: " + data);
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
    }
}
