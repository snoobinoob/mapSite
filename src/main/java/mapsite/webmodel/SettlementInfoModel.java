package mapsite.webmodel;

import necesse.engine.network.server.Server;
import necesse.engine.util.GameMath;
import necesse.entity.mobs.PlayerMob;
import necesse.level.maps.levelData.settlementData.CachedSettlementData;
import necesse.level.maps.regionSystem.RegionManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SettlementInfoModel {
    private final int uniqueID;
    private final String name;
    public final long ownerAuth;
    private final int tileX;
    private final int tileY;
    private final int[][] bounds;
    private final boolean isDisbanded;

    public SettlementInfoModel(CachedSettlementData settlementData) {
        uniqueID = settlementData.uniqueID;
        name = settlementData.getName().translate();
        ownerAuth = settlementData.getOwnerAuth();
        tileX = settlementData.getTileX();
        tileY = settlementData.getTileY();
        isDisbanded = settlementData.isLoaded() && settlementData.getLoadedData().isDisbanded();


        Rectangle regionRectangle = settlementData.getRegionRectangle();
        int left = GameMath.getTileCoordByRegion(regionRectangle.x);
        int top = GameMath.getTileCoordByRegion(regionRectangle.y);
        int right = left + RegionManager.REGION_SIZE * regionRectangle.width - 1;
        int bottom = top + RegionManager.REGION_SIZE * regionRectangle.height - 1;
        bounds = new int[][]{new int[]{left, top}, new int[]{right, bottom}};
    }

    public Map<String, Object> toMap(Server server) {
        PlayerMob player = server.getPlayerByAuth(ownerAuth);
        Map<String, Object> obj = new HashMap<>();
        obj.put("id", uniqueID);
        obj.put("name", name);
        obj.put("owner", player == null ? "N/A" : player.playerName);
        obj.put("x", tileX);
        obj.put("y", tileY);
        obj.put("bounds", bounds);
        obj.put("disbanded", isDisbanded);
        return obj;
    }
}
