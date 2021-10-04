package ChatBot;


import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ServerCommandLineRunner implements CommandLineRunner {
    //
//    private final Matcher matcher;
//    private final Passwords passwords;
    private final SocketIOServer server;

    @Autowired
    private ChatBot chatBot;

    @Autowired
    Application.MessageProducer producer;

    //
    @Autowired
    public ServerCommandLineRunner(SocketIOServer server) {
        this.server = server;
//        this.matcher = matcher;
//        this.passwords = passwords;
    }

    //
    @Override
    public void run(String... args) throws Exception {

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                HandshakeData handshakeData = client.getHandshakeData();
                System.out.println("Client[{}] - Connected to order module through '{}'" + client.getSessionId().toString() + handshakeData.getUrl());
                server.getBroadcastOperations().sendEvent("serverMessage", "Connected");
            }
        });
        server.addEventListener("userMessage", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackSender) {
                client.sendEvent("userMessage", data);
                producer.sendMessage(data);
                String response = chatBot.processMessage(data);
                client.sendEvent("serverMessage", response);
            }
        });
//
        server.start();
    }
}