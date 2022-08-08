package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    private final String strRequest;

    public Client(String strRequest) {
        this.strRequest = strRequest;
    }

    public void start() {
        System.out.println("Client started!");
        try(Socket client = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
            DataInputStream input = new DataInputStream(client.getInputStream());
            DataOutputStream output = new DataOutputStream(client.getOutputStream())){

            output.writeUTF(strRequest);
            System.out.println("Sent: " + strRequest);
            String receivedMsg = input.readUTF();
            System.out.println("Received: " + receivedMsg);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
