package mapsite;

import necesse.engine.util.GameMath;
import necesse.engine.util.LevelIdentifier;

import java.awt.*;

public class TileLocation {
    public LevelIdentifier levelIdentifier;
    public int tileX;
    public int tileY;

    public TileLocation(LevelIdentifier levelIdentifier, int tileX, int tileY) {
        this.levelIdentifier = levelIdentifier;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public TileLocation(int tileX, int tileY) {
        this(LevelIdentifier.SURFACE_IDENTIFIER, tileX, tileY);
    }

    public static TileLocation fromChunkCoords(int chunkX, int chunkY) {
        return new TileLocation(
                chunkX * Settings.mapChunkSize,
                chunkY * Settings.mapChunkSize
        );
    }

    public Point toRegionPoint() {
        return new Point(GameMath.getRegionCoordByTile(tileX), GameMath.getRegionCoordByTile(tileY));
    }

    public Point toChunkPoint() {
        return new Point(tileX / Settings.mapChunkSize, tileY / Settings.mapChunkSize);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TileLocation other)) {
            return false;
        }

        return levelIdentifier.equals(other.levelIdentifier) && tileX == other.tileX && tileY == other.tileY;
    }

    @Override
    public int hashCode() {
        return levelIdentifier.hashCode() + 1337 * tileX + 9001 * tileY;
    }
}
