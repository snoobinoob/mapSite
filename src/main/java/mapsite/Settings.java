package mapsite;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class Settings extends ModSettings {
    public static int webappPort = 8080;
    public static long playerUpdateRateMs = 1000;
    public static long mapUpdateRateMs = 5000;
    public static int mapChunkFetchRateMs = 100;
    public static int mapChunkSize = 64;

    @Override
    public void addSaveData(SaveData save) {
        save.addInt("webappPort", webappPort);
        save.addLong("playerUpdateRateMs", playerUpdateRateMs);
        save.addLong("mapUpdateRateMs", mapUpdateRateMs);
        save.addInt("mapChunkFetchRateMs", mapChunkFetchRateMs);
        save.addInt("mapChunkSize", mapChunkSize);
    }

    @Override
    public void applyLoadData(LoadData save) {
        webappPort = save.getInt("webappPort", 8080);
        playerUpdateRateMs = save.getLong("playerUpdateRateMs", 1000);
        mapUpdateRateMs = save.getLong("mapUpdateRateMs", 5000);
        mapChunkFetchRateMs = save.getInt("mapChunkFetchRateMs", 100);
        mapChunkSize = save.getInt("mapChunkSize", 64);
    }
}
