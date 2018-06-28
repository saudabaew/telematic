package telematic.multiThreadServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000; i++) {
            Socket serverClient = new Socket("127.0.0.1",6666);
            try (DataInputStream inStream = new DataInputStream(serverClient.getInputStream())){
                System.out.println(i);
            };
        }
        System.out.println("For exit press any key...");
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        br.readLine();
    }

    public static void clientRun() {
    }
}
