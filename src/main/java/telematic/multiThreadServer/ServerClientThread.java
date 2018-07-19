package telematic.multiThreadServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import telematic.util.MessageUtil;

import java.io.DataInputStream;
import java.io.OutputStream;
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
        String answer = "";
        String flag = "";
        try (DataInputStream inStream = new DataInputStream(socket.getInputStream())) {
            try (OutputStream outputStream = socket.getOutputStream()) {
                while (true) {
                    String data = inStream.readLine();
                    LOG.info("Client send: {}", data);
                    switch (data.charAt(1)) {
                        case 'L':
                            try {
                                if (data.charAt(3) != '2') {
                                    login = Long.parseLong(data.substring(3, 18)); //IPS 1.1
                                } else {
                                    login = Long.parseLong(data.substring(7, 22)); //IPS 2.0
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                outputStream.write("#AL#0\r\n".getBytes("UTF-8"));
                                LOG.info("Server reply: #AL#0\r\n");
                            }
                            kafkaTemplate.send(topicName, data);
                            if (login == 0) break;
                            outputStream.write("#AL#1\r\n".getBytes("UTF-8"));
                            LOG.info("Server reply: #AL#1");
                            break;
                        case 'S':
                            answer = "#ASD#";
                            if (login != 0) {
                                try {
                                    flag = messageUtil.saveMessage(login, data.substring(4), jdbcTemplate);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    outputStream.write("#ASD#-1\r\n".getBytes("UTF-8"));
                                    LOG.info("Server reply: #ASD#-1");
                                    break;
                                }
                                answer = answer + flag;
                                outputStream.write(answer.getBytes("UTF-8"));
                                LOG.info("Server reply: {}", answer);
                                kafkaTemplate.send(topicName, data);
                            } else {
                                socket.close();
                            }
                            break;
                        case 'D':
                            answer = "#AD#";
                            if (login != 0) {
                                try {
                                    flag = messageUtil.saveMessage(login, data.substring(3), jdbcTemplate);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    outputStream.write("#AD#-1\r\n".getBytes("UTF-8"));
                                    LOG.info("Server reply: #AD#-1");
                                    break;
                                }
                                answer = answer + flag;
                                outputStream.write(answer.getBytes("UTF-8"));
                                LOG.info("Server reply: {}", answer);
                                kafkaTemplate.send(topicName, data);
                            } else {
                                socket.close();
                            }
                            break;
                        case 'P':
                            if (login != 0) {
                                outputStream.write("#AP#\r\n".getBytes("UTF-8"));
                                LOG.info("Server reply: #AP# from {}", login);
                            }
                            break;
                        case 'B':
                            if (login != 0) {
                                String bData = data.substring(3);
                                String[] message = bData.split("\\|");
                                int count = message.length - 1;
                                messageUtil.saveBlackBox(login, bData, jdbcTemplate);
                                String bAnswer = "#AB#" + count + "\r\n";
                                byte[] bAns = bAnswer.getBytes("UTF-8");
                                outputStream.write(bAns);
                                LOG.info("Server reply: {}", bAnswer);
                                kafkaTemplate.send(topicName, data);
                            } else {
                                socket.close();
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
