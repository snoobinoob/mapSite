package mapsite.spark;

import necesse.engine.GameLog;
import necesse.engine.network.server.Server;
import necesse.engine.save.LoadData;
import necesse.engine.util.LevelIdentifier;
import necesse.engine.world.WorldFile;
import necesse.level.gameObject.GameObject;
import necesse.level.gameTile.GameTile;
import necesse.level.maps.Level;
import necesse.level.maps.regionSystem.Region;
import necesse.level.maps.regionSystem.RegionManager;
import necesse.level.maps.regionSystem.managers.RegionFilesManager;
import org.eclipse.jetty.util.ajax.JSON;
import spark.Route;
import spark.Spark;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MapRoute extends SparkRouteHandler {
    public MapRoute(Server server) {
        super(server);
    }

    @Override
    public void register(String path) {
        Spark.get(path, handleGetRootWrapper);
    }

    private final Route handleGetRoot = (req, res) -> {
        int[] xLimits = parsePairedNumbers(req.queryParams("x"));
        int[] yLimits = parsePairedNumbers(req.queryParams("y"));

        Level level = server.world.levelManager.getLevel(LevelIdentifier.SURFACE_IDENTIFIER);
        RegionManager regionManager = level.regionManager;
        int startRegionX = regionManager.getRegionCoordByTile(xLimits[0]);
        int startRegionY = regionManager.getRegionCoordByTile(yLimits[0]);
        int endRegionX = regionManager.getRegionCoordByTile(xLimits[1]);
        int endRegionY = regionManager.getRegionCoordByTile(yLimits[1]);

        Map<Point, Region> regions = new HashMap<>();
        for (int y = startRegionY; y <= endRegionY; y++) {
            for (int x = startRegionX; x <= endRegionX; x++) {
                regions.compute(new Point(x, y), (key, prevValue) -> {
                    if (!regionManager.isRegionGenerated(key.x, key.y)) {
                        return null;
                    }
                    if (regionManager.isRegionLoaded(key.x, key.y)) {
                        return regionManager.getRegion(key.x, key.y, false);
                    }
                    return hackyFetchOfRegionWithoutFullLoading(regionManager, key.x, key.y);
                });
            }
        }
        int[][] mapColors = new int[yLimits[1] - yLimits[0] + 1][xLimits[1] - xLimits[0] + 1];

        int tileX, tileY;
        for (int y = 0; y < mapColors.length; y++) {
            tileY = yLimits[0] + y;
            for (int x = 0; x < mapColors[y].length; x++) {
                tileX = xLimits[0] + x;
                Color mapColor = getTileColor(regions, regionManager, tileX, tileY);
                mapColors[y][x] = mapColor.getRGB();
            }
        }

        JSON json = new JSON();
        String result = json.toJSON(mapColors);

        res.header("content-type", "text/json");
        return result;
    };

    private Color getTileColor(Map<Point, Region> regions, RegionManager regionManager, int tileX, int tileY) {
        Point regionCoords = new Point(
                regionManager.getRegionCoordByTile(tileX),
                regionManager.getRegionCoordByTile(tileY)
        );
        Region tileRegion = regions.get(regionCoords);
        if (tileRegion == null) {
            return Color.BLACK;
        }

        int regionTileX = tileX - tileRegion.tileXOffset;
        int regionTileY = tileY - tileRegion.tileYOffset;

        GameObject obj = tileRegion.objectLayer.getObjectByRegion(0, regionTileX, regionTileY);
        if (obj.getID() != 0) {
            return obj.mapColor;
        }

        GameTile tile = tileRegion.tileLayer.getTileByRegion(regionTileX, regionTileY);
        if (tile.getID() != 0) {
            return tile.mapColor;
        }

        GameLog.warn.println("Tile (" + tileX + ", " + tileY + ") is generated, but empty?");
        return Color.BLACK;
    }

    private Region hackyFetchOfRegionWithoutFullLoading(RegionManager regionManager, int regionX, int regionY) {
        Region region = constructRegion(regionManager, regionX, regionY);
        if (region == null) {
            return null;
        }

        int worldRegionX = RegionFilesManager.getWorldRegionPos(regionX);
        int worldRegionY = RegionFilesManager.getWorldRegionPos(regionY);
        WorldFile worldFile = server.world.fileSystem.getWorldRegionFile(LevelIdentifier.SURFACE_IDENTIFIER, worldRegionX, worldRegionY);
        if (!worldFile.exists()) {
            return null;
        }

        LoadData worldSave = new LoadData(worldFile);
        LoadData regionSave = worldSave.getFirstLoadDataByName(regionX + "x" + regionY);
        region.layers.loadSaveData(regionSave, null);
        return region;
    }

    private Region constructRegion(RegionManager regionManager, int regionX, int regionY) {
        int tileXOffset = regionManager.getTileCoordByRegion(regionX);
        int tileYOffset = regionManager.getTileCoordByRegion(regionY);
        int tileWidth = regionManager.getRegionTileWidth(regionX, tileXOffset);
        if (tileWidth <= 0) {
            return null;
        } else {
            int tileHeight = regionManager.getRegionTileHeight(regionY, tileYOffset);
            return tileHeight <= 0 ? null : new Region(regionManager, regionX, regionY, tileXOffset, tileYOffset, tileWidth, tileHeight);
        }
    }

    private final Route handleGetRootWrapper = (req, res) -> {
        try {
            return handleGetRoot.handle(req, res);
        } catch (Exception e) {
            e.printStackTrace(GameLog.err);
            res.status(400);
            return e.getMessage();
        }
    };

    private int[] parsePairedNumbers(String numberStr) {
        String[] splitNumbers = numberStr.split(",");
        try {
            int firstNumber = Integer.parseInt(splitNumbers[0]);
            int secondNumber = Integer.parseInt(splitNumbers[1]);
            return new int[]{firstNumber, secondNumber};
        } catch (NumberFormatException e) {
            GameLog.warn.println("Could not parse number pair \"" + numberStr + "\"");
        }
        return new int[]{0, 0};
    }
}
