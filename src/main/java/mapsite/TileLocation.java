package mapsite;

import necesse.engine.util.LevelIdentifier;

public class TileLocation {
    public LevelIdentifier levelIdentifier;
    public int tileX;
    public int tileY;

    public TileLocation(LevelIdentifier levelIdentifier, int tileX, int tileY) {
        this.levelIdentifier = levelIdentifier;
        this.tileX = tileX;
        this.tileY = tileY;
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
