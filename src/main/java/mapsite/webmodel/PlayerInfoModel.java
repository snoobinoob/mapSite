package mapsite.webmodel;

import necesse.engine.network.server.ServerClient;

import java.util.HashMap;
import java.util.Map;

public class PlayerInfoModel {
    private final String name;
    private final float x;
    private final float y;
    private final String levelID;

    public PlayerInfoModel(ServerClient serverClient) {
        name = serverClient.getName();
        x = serverClient.playerMob.x / 32;
        y = serverClient.playerMob.y / 32;
        levelID = serverClient.getLevelIdentifier().stringID;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("name", name);
        obj.put("x", x);
        obj.put("y", y);
        obj.put("levelID", levelID);
        return obj;
    }
}
