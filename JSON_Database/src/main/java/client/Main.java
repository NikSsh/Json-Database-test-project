package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import server.requestANDresponse.Request;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    private static final Gson gson = new Gson();
    private static final String clientFilesPath = ".\\src\\main\\java\\client\\data\\";

    @Parameter(names={"--key", "-k"})
    String key;

    @Parameter(names={"--type", "-t"})
    String type;

    @Parameter(names={"--value", "-v"})
    String value;

    @Parameter(names={"--filename", "-in"})
    String fileName;

    public static String getJsonRequestFromFile(String fileName) {
        String jsonRequest = "";
        try(InputStream input = new FileInputStream(clientFilesPath + fileName)) {
            jsonRequest = new String(input.readAllBytes());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return jsonRequest;
    }

    public static void main(String[] args) {

    Main main = new Main();
    JCommander.newBuilder()
            .addObject(main)
            .build()
            .parse(args);

    Client client;
    if (main.fileName != null) {
        String jsonRequest = getJsonRequestFromFile(main.fileName);
        client = new Client(jsonRequest);
    } else {
        client = new Client(gson.toJson(new Request(main.type, main.key, main.value)));
    }

    client.start();
    }
}
