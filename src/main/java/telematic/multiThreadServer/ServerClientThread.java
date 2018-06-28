package telematic.multiThreadServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import telematic.util.MessageUtil;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ServerClientThread extends Thread {

    private final Logger LOG = LoggerFactory.getLogger(ServerClientThread.class);

    private Socket socket;
    private MessageUtil messageUtil;
    private JdbcTemplate jdbcTemplate;

    public ServerClientThread(Socket socket, JdbcTemplate jdbcTemplate, MessageUtil messageUtil) {
        this.socket = socket;
        this.messageUtil = messageUtil;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void run() {
        Long login = 0L;
        try (DataInputStream inStream = new DataInputStream(socket.getInputStream())) {
            DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String data = inStream.readLine();
                char type = data.charAt(1);

                switch (type) {
                    case 'L':
                        login = Long.parseLong(data.substring(3, 18));
                        break;
                    case 'D':
                        messageUtil.saveMessage(login, data.substring(3), jdbcTemplate);
                        break;
                    case 'S':
                        messageUtil.saveMessage(login, data.substring(4), jdbcTemplate);
                        break;
                    case 'B':
                        messageUtil.saveBlackBox(login, data.substring(3), jdbcTemplate);
                        break;
                }

                System.out.println("Client send: " + data);
//                serverMessage="From Server to Client-" + clientNo + " Square of " + clientMessage + " is " +squre;
//                outStream.writeUTF(serverMessage);
//                outStream.flush();
                //inStream.close();
                //serverClient.close();
            }
        }
        catch (Exception e) {
            LOG.info(e.getMessage());
        }
    }
}
