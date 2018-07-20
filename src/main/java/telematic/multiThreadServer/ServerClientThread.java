package telematic.multiThreadServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import telematic.checkSum.CRC16;
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
    private CRC16 crc16;

    public ServerClientThread(Socket socket, JdbcTemplate jdbcTemplate, MessageUtil messageUtil, KafkaTemplate kafkaTemplate, String topicName, CRC16 crc16) {
        this.socket = socket;
        this.messageUtil = messageUtil;
        this.jdbcTemplate = jdbcTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
        this.crc16 = crc16;
    }

    public void run() {
        Long login = 0L;
        String answer = "";
        String flag = "";
        boolean IPS_2_0 = false;
        try (DataInputStream inStream = new DataInputStream(socket.getInputStream())) {
            try (OutputStream outputStream = socket.getOutputStream()) {
                while (true) {
                    String data = inStream.readLine();
                    LOG.info("Client send: {}", data);
                    switch (data.charAt(1)) {
                        case 'L':
                            try {
                                if (data.charAt(3) != '2') {
                                    login = Long.parseLong(data.substring(3, 18));
                                } else {
                                    IPS_2_0 = true;
                                    login = Long.parseLong(data.substring(7, 22));
                                    if (!crc16.equals(data.substring(3), false, login)) {
                                        outputStream.write("#AL#10\r\n".getBytes("UTF-8"));
                                        break;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                outputStream.write("#AL#0\r\n".getBytes("UTF-8"));
                                LOG.info("Server reply: to {} #AL#0", login);
                            }
                            kafkaTemplate.send(topicName, data);
                            if (login == 0) break;
                            outputStream.write("#AL#1\r\n".getBytes("UTF-8"));
                            LOG.info("Server reply: to {} #AL#1", login);
                            break;
                        case 'S':
                            if (login != 0) {
                                if (IPS_2_0) {
                                    if (!crc16.equals(data.substring(4), false, login)) {
                                        outputStream.write("#ASD#16\r\n".getBytes("UTF-8"));
                                        break;
                                    }
                                }
                                answer = "#ASD#";
                                try {
                                    flag = messageUtil.saveMessage(login, data.substring(4), jdbcTemplate);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    outputStream.write("#ASD#-1\r\n".getBytes("UTF-8"));
                                    LOG.info("Server reply: to {} #ASD#-1", login);
                                    break;
                                }
                                answer = answer + flag;
                                outputStream.write(answer.getBytes("UTF-8"));
                                LOG.info("Server reply: to {} answer {}", login, answer);
                                kafkaTemplate.send(topicName, data);
                            } else {
                                socket.close();
                            }
                            break;
                        case 'D':
                            if (login != 0) {
                                if (IPS_2_0) {
                                    if (!crc16.equals(data.substring(3), false, login)) {
                                        outputStream.write("#AD#16\r\n".getBytes("UTF-8"));
                                        break;
                                    }
                                }
                                answer = "#AD#";
                                try {
                                    flag = messageUtil.saveMessage(login, data.substring(3), jdbcTemplate);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    outputStream.write("#AD#-1\r\n".getBytes("UTF-8"));
                                    LOG.info("Server reply: to {} #AD#-1", login);
                                    break;
                                }
                                answer = answer + flag;
                                outputStream.write(answer.getBytes("UTF-8"));
                                LOG.info("Server reply: to {} answer {}", login, answer);
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
                                if (IPS_2_0) {
                                    if (!crc16.equals(data.substring(3), true, login)) {
                                        outputStream.write("#AB#\r\n".getBytes("UTF-8"));
                                        break;
                                    }
                                }
                                String bData = data.substring(3);
                                String[] message = bData.split("\\|");
                                int count = message.length - 1;
                                messageUtil.saveBlackBox(login, bData, jdbcTemplate);
                                String bAnswer = "#AB#" + count + "\r\n";
                                byte[] bAns = bAnswer.getBytes("UTF-8");
                                outputStream.write(bAns);
                                LOG.info("Server reply: to {} answer {}", login, bAnswer);
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
