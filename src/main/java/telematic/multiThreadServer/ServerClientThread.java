package telematic.multiThreadServer;

import org.springframework.jdbc.core.JdbcTemplate;
import telematic.dto.Message;
import telematic.util.MessageUtil;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ServerClientThread extends Thread {

    private Socket socket;
    private MessageUtil messageUtil;
    private JdbcTemplate jdbcTemplate;

    public ServerClientThread(Socket socket, JdbcTemplate jdbcTemplate, MessageUtil messageUtil) {
        this.socket = socket;
        this.messageUtil = messageUtil;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void run() {
        Message message = new Message();
        try (DataInputStream inStream = new DataInputStream(socket.getInputStream())) {
            DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String data = inStream.readLine();

                switch (data.charAt(1)) {
                    case 'L':
                        message.setImei(Long.parseLong(data.substring(3, 18)));
                        break;
                    case 'D':
                        messageUtil.fullPackage(message, data, jdbcTemplate);
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
            System.out.println(e.toString());
        }
    }
}
