package mapsite.webmodel;

import necesse.engine.util.GameMath;
import necesse.level.maps.levelData.settlementData.CachedSettlementData;
import necesse.level.maps.regionSystem.RegionManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SettlementInfoModel {
    private final String name;
    private final String owner;
    private final int tileX;
    private final int tileY;
    private final int[][] bounds;

    public SettlementInfoModel(CachedSettlementData settlementData) {
        name = settlementData.getLoadedData().getSettlementName().translate();
        owner = settlementData.getLoadedData().getOwnerName();
        tileX = settlementData.getTileX();
        tileY = settlementData.getTileY();

        Rectangle regionRectangle = settlementData.getRegionRectangle();
        int left = GameMath.getTileCoordByRegion(regionRectangle.x);
        int top = GameMath.getTileCoordByRegion(regionRectangle.y);
        int right = left + RegionManager.REGION_SIZE * regionRectangle.width - 1;
        int bottom = top + RegionManager.REGION_SIZE * regionRectangle.height - 1;
        bounds = new int[][]{new int[]{left, top}, new int[]{right, bottom}};
    }

    public Map<String, Object> toMap() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("name", name);
        obj.put("owner", owner);
        obj.put("x", tileX);
        obj.put("y", tileY);
        obj.put("bounds", bounds);
        return obj;
    }
}
