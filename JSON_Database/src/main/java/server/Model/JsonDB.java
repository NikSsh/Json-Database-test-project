package server.Model;

import com.google.gson.*;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonDB implements Database{
    private static final Gson gson = new Gson();
    private static final String dbPath = ".\\src\\main\\java\\server\\data\\db.json";
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private static JsonDB dbInstance;

    public static JsonDB getDbInstance() {
        if (dbInstance == null) {
            dbInstance = new JsonDB();
        }

        return dbInstance;
    }

    public boolean setDataByKey(String key, String data) {
        writeLock.lock();

        boolean result = false;
        JsonObject jsonDBData = load();

        if (isStringJson(key)) {
            JsonArray keysArray = gson.fromJson(key, JsonArray.class);
            Iterator<JsonElement> keysIterator = keysArray.iterator();

            if (keysIterator.hasNext()) {
                String mainKey = keysIterator.next().getAsString();
                String jsonKey = mainKey;
                if (jsonDBData.has(jsonKey)) {
                    String value;
                    if (jsonDBData.get(jsonKey).isJsonObject()) {
                        value = jsonDBData.getAsJsonObject(jsonKey).toString();
                    } else {
                        value = jsonDBData.get(jsonKey).getAsString();
                    }

                    if (keysIterator.hasNext()) {
                        JsonObject mainObject = gson.fromJson(value, JsonObject.class);
                        JsonObject jsonObject = mainObject;
                        while (keysIterator.hasNext()) {
                            jsonKey = keysIterator.next().getAsString();
                            if (keysIterator.hasNext()) {
                                jsonObject = jsonObject.getAsJsonObject(jsonKey);
                            } else {
                                if (isStringJson(data)) {
                                    jsonObject.add(jsonKey, gson.fromJson(data, JsonObject.class));
                                } else {
                                    JsonElement element = gson.fromJson(data, JsonElement.class);
                                    jsonObject.add(jsonKey, element);
                                }

                                jsonDBData.add(mainKey, mainObject);
                                result = true;
                                break;
                            }
                        }
                    } else {
                        if (isStringJson(data)) {
                            jsonDBData.add(mainKey, gson.fromJson(data, JsonObject.class));
                        } else {
                            jsonDBData.add(mainKey, gson.fromJson(data, JsonElement.class));
                        }
                        result = true;
                    }
                } else {
                    if (keysIterator.hasNext()) {
                        JsonObject prevObject = new JsonObject();
                        JsonElement jsonData = gson.fromJson(data, JsonElement.class);
                        prevObject.add(keysArray.get(keysArray.size() - 1).getAsString(), jsonData);

                        for (int i = keysArray.size() - 2; i > 0; i--) {

                            String currentKey = keysArray.get(i).getAsString();
                            JsonObject newObject = new JsonObject();
                            newObject.add(currentKey, prevObject);
                            prevObject = newObject;

                        }

                        jsonDBData.add(jsonKey, prevObject);

                    } else {
                        if (isStringJson(data)) {
                            jsonDBData.add(jsonKey, gson.fromJson(data, JsonObject.class));
                        } else {
                            jsonDBData.add(jsonKey, gson.fromJson(data, JsonElement.class));
                        }
                    }
                    result = true;
                }
            }
        } else {
            if (isStringJson(data)) {
                jsonDBData.add(key, gson.fromJson(data, JsonObject.class));
            } else {
                jsonDBData.add(key, gson.fromJson(data, JsonElement.class));
            }
            result = jsonDBData.has(key);
        }

        save(jsonDBData);

        writeLock.unlock();

        return result;
    }

    public String getDataByKey(String key) {
        readLock.lock();
        JsonObject jsonDBData = load();
        String data;
        if (isStringJson(key)) {
            JsonArray keysArray = gson.fromJson(key, JsonArray.class);
            Iterator<JsonElement> keysIterator = keysArray.iterator();

            if (keysIterator.hasNext()) {
                String jsonKey = keysIterator.next().getAsString();

                if (jsonDBData.has(jsonKey)) {
                    if (jsonDBData.get(jsonKey).isJsonObject()) {
                        data = jsonDBData.getAsJsonObject(jsonKey).toString();
                    } else {
                        data = jsonDBData.getAsJsonPrimitive(jsonKey).getAsString();
                    }

                    while (keysIterator.hasNext()) {
                        JsonObject jsonObject = gson.fromJson(data, JsonObject.class);

                        jsonKey = keysIterator.next().getAsString();

                        if (jsonObject.has(jsonKey)) {
                            if (jsonObject.get(jsonKey).isJsonObject()) {
                                data = jsonObject.getAsJsonObject(jsonKey).toString();
                            } else {
                                data = jsonObject.get(jsonKey).getAsString();
                            }
                        } else {
                            data = "";
                            break;
                        }
                    }
                } else {
                    data = "";
                }
            } else {
                data = "";
            }
        } else {
            data = jsonDBData.getAsJsonObject(key).toString();
            if (data.equals("{}")) {
                data = "";
            }
        }

        save(jsonDBData);
        readLock.unlock();

        return data;
    }

    public boolean deleteDataByKey(String key) {

        writeLock.lock();
        boolean success = false;

        JsonObject jsonDBData = load();

        if (isStringJson(key)) {
            JsonArray keysArray = gson.fromJson(key, JsonArray.class);
            Iterator<JsonElement> keysIterator = keysArray.iterator();

            String firstKey = keysIterator.next().getAsString();
            String jsonKey = firstKey;

            if (jsonDBData.has(jsonKey)) {
                String data;
                if (jsonDBData.get(jsonKey).isJsonObject()) {
                    data = jsonDBData.getAsJsonObject(jsonKey).toString();
                } else {
                    data = jsonDBData.get(jsonKey).getAsString();
                }

                if (keysIterator.hasNext()) {
                    JsonObject mainDataObject = gson.fromJson(data, JsonObject.class);

                    JsonObject dataObject = mainDataObject;
                    while (keysIterator.hasNext()) {
                        jsonKey = keysIterator.next().getAsString();

                        if (keysIterator.hasNext()) {
                            if (dataObject.has(jsonKey)) {
                                dataObject = dataObject.getAsJsonObject(jsonKey);
                            } else {
                                success = false;
                                break;
                            }
                        } else {
                            if (dataObject.has(jsonKey)) {

                                dataObject.remove(jsonKey);
                                jsonDBData.add(firstKey, mainDataObject);
                                success = true;
                            } else {
                                success = false;
                            }
                        }
                    }
                } else {
                    jsonDBData.remove(jsonKey);
                    success = true;
                }
            }
        } else {
            success = jsonDBData.has(key);
            if (success) {
                jsonDBData.remove(key);
            }
        }

        save(jsonDBData);
        writeLock.unlock();
        return success;
    }

    private void save(JsonObject dbDataObj) {
        try (FileWriter writer = new FileWriter(dbPath)){
            writer.write(dbDataObj.toString());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private JsonObject load() {
        JsonObject dbJsonData = null;
        try (InputStream reader = new FileInputStream(dbPath)){
            dbJsonData = gson.fromJson(new String(reader.readAllBytes()), JsonObject.class);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (dbJsonData == null) {
            dbJsonData = new JsonObject();
        }

        return  dbJsonData;
    }

    private boolean isStringJson(String data) {
        Matcher matcher = Pattern.compile("(\\{(\\s*\"\\w+\"\\s*:\\s*.*)+\\s*})|(\\[[\"\\w+\",?]+])").matcher(data);
        return matcher.find();
    }
}
