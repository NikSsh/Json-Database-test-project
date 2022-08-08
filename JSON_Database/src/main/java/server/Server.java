package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import server.Model.Database;
import server.Model.JsonDB;
import server.requestANDresponse.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Server {
    private static final Gson gson = new Gson();
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 8080;
    private final Database database = JsonDB.getDbInstance();
    private final ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    volatile boolean exitFlag = false;

    public void start() {
        System.out.println("Server started!");

        try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {

            do {
                Socket socket = server.accept();

                executors.execute(() -> {
                    try (DataInputStream input = new DataInputStream(socket.getInputStream());
                         DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                        String jsonRequest = input.readUTF();

                        JsonObject requestObject = gson.fromJson(jsonRequest, JsonObject.class);

                        String type = null, key = null, value = null;

                        if (requestObject.has("type")) {
                            type = requestObject.get("type").getAsString();
                        }

                        if (requestObject.has("key")) {
                            if (requestObject.get("key").isJsonArray()) {
                                key = requestObject.get("key").getAsJsonArray().toString();
                            } else {
                                key = requestObject.get("key").getAsString();
                            }
                        }

                        if (requestObject.has("value")) {
                            if (requestObject.get("value").isJsonObject()) {
                                value = requestObject.get("value").getAsJsonObject().toString();
                            } else {
                                value = requestObject.get("value").toString();
                            }
                        }

                        System.out.println(type + ", " + key + ", " + value);

                        if ("exit".equals(type)) {
                            exitFlag = true;
                            executors.shutdown();
                            server.close();
                        }

                        String jsonResponse = gson.toJson(processAndGetResponse(type, key, value)).replaceAll("\"\\{", "{")
                                .replaceAll("}\"", "}")
                                .replaceAll("\\\\\"", "\"");

                        output.writeUTF(jsonResponse);
                        socket.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                });
            } while (!exitFlag);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private Response processAndGetResponse(String type, String key, String value) {
        Response.ResponseBuilder responseBuilder = new Response.ResponseBuilder();

        switch (type) {
            case "set": {

                boolean success = database.setDataByKey(key, value);

                if (success) {
                    responseBuilder.setResponse("OK");
                } else {
                    responseBuilder.setResponse("ERROR");
                }

                break;
            }

            case "get": {
                String data = database.getDataByKey(key);

                if (data.isEmpty()) {
                    responseBuilder.setResponse("ERROR")
                            .setReason("No such key");
                } else {
                    responseBuilder.setResponse("OK")
                            .setValue(data);
                }

                break;
            }

            case "delete": {
                boolean success = database.deleteDataByKey(key);

                if (success) {
                    responseBuilder.setResponse("OK");
                } else {
                    responseBuilder.setResponse("ERROR")
                            .setReason("No such key");
                }
                break;
            }

            case "exit": {
                responseBuilder.setResponse("OK");
                break;
            }

            default : {
                responseBuilder.setResponse("ERROR")
                        .setReason("Unknown type");
            }
        }

        return responseBuilder.create();
    }
}
