package telematic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import telematic.multiThreadServer.MultithreadedSocketServer;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private MultithreadedSocketServer multithreadedSocketServer;

    public static void main(String[] args){
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        multithreadedSocketServer.init();
    }
}