package server.Model;

public interface Database {
    boolean setDataByKey(String key, String data);
    String getDataByKey(String key);
    boolean deleteDataByKey(String key);
}
