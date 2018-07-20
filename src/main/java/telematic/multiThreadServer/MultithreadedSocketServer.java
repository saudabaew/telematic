package telematic.multiThreadServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import telematic.util.MessageUtil;

import java.net.ServerSocket;
import java.net.Socket;

@Component
public class MultithreadedSocketServer {

    private static final Logger LOG = LoggerFactory.getLogger(MultithreadedSocketServer.class);

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    @Value("${server.socket}")
    private Integer socketNumber;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MessageUtil messageUtil;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private TaskExecutor taskExecutor;

    public void init() throws Exception {
        try {
            ServerSocket server = new ServerSocket(socketNumber);
            LOG.info("Server started on port: {}", socketNumber);
            while (true) {
                Socket socket = server.accept();
                LOG.info("New client started!");
                taskExecutor.execute(new ServerClientThread(socket, jdbcTemplate, messageUtil, kafkaTemplate, topicName));
//                ServerClientThread sct = new ServerClientThread(socket, jdbcTemplate, messageUtil, kafkaTemplate, topicName);
//                sct.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
