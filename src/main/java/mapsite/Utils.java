package mapsite;

import necesse.engine.util.GameMath;
import necesse.level.maps.regionSystem.Region;

import java.awt.*;

public class Utils {
    public static Point toChunkPoint(Region region) {
        return toChunkPoint(GameMath.getTileCoordByRegion(region.regionX), GameMath.getTileCoordByRegion(region.regionY));
    }

    public static Point toChunkPoint(int tileX, int tileY) {
        return new Point(tileX / Settings.mapChunkSize, tileY / Settings.mapChunkSize);
    }
}
