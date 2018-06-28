package telematic.multiThreadServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import telematic.util.MessageUtil;

import javax.annotation.PostConstruct;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class MultithreadedSocketServer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MessageUtil messageUtil;

    @PostConstruct
    public void init() throws Exception {
        try {
            ServerSocket server = new ServerSocket(6666);
            System.out.println("Server started...");
            while (true) {
                Socket socket = server.accept();                          // сервер принимает запрос на подключение клиента
                System.out.println("Client started!");
                ServerClientThread sct = new ServerClientThread(socket, jdbcTemplate, messageUtil);  // отправляем запрос в отдельный поток
                sct.start();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
