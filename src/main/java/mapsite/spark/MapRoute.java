package mapsite.spark;

import mapsite.Settings;
import mapsite.TileLocation;
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
import spark.Route;
import spark.Spark;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapRoute extends SparkRouteHandler {
    public MapRoute(Server server) {
        super(server);
    }

    @Override
    public void register(String path) {
        Spark.get(path, handleGetRootWrapper);
    }

    private final Route handleGetRoot = (req, res) -> {
        int chunkX = req.queryMap("x").integerValue();
        int chunkY = req.queryMap("y").integerValue();

        TileLocation startLocation = TileLocation.fromChunkCoords(chunkX, chunkY);
        TileLocation endLocation = new TileLocation(
                startLocation.tileX + Settings.mapChunkSize,
                startLocation.tileY + Settings.mapChunkSize
        );

        Level level = server.world.levelManager.getLevel(LevelIdentifier.SURFACE_IDENTIFIER);
        RegionManager regionManager = level.regionManager;
        Point startRegionPoint = startLocation.toRegionPoint();
        Point endRegionPoint = endLocation.toRegionPoint();

        Map<Point, Region> regions = new HashMap<>();
        AtomicBoolean containsGeneratedRegion = new AtomicBoolean(false);
        for (int y = startRegionPoint.y; y <= endRegionPoint.y; y++) {
            for (int x = startRegionPoint.x; x <= endRegionPoint.x; x++) {
                regions.compute(new Point(x, y), (key, prevValue) -> {
                    if (!regionManager.isRegionGenerated(key.x, key.y)) {
                        return null;
                    }
                    containsGeneratedRegion.set(true);
                    if (regionManager.isRegionLoaded(key.x, key.y)) {
                        return regionManager.getRegion(key.x, key.y, false);
                    }
                    return hackyFetchOfRegionWithoutFullLoading(regionManager, key.x, key.y);
                });
            }
        }

        HttpServletResponse rawResponse = res.raw();
        ServletOutputStream out = rawResponse.getOutputStream();
        rawResponse.addHeader("content-type", "application/octet-stream");

        if (!containsGeneratedRegion.get()) {
            out.write(0);
            return rawResponse;
        }

        out.write(1);
        int tileX, tileY;
        for (int y = 0; y < Settings.mapChunkSize; y++) {
            tileY = startLocation.tileY + y;
            for (int x = 0; x < Settings.mapChunkSize; x++) {
                tileX = startLocation.tileX + x;
                Color mapColor = getTileColor(regions, regionManager, tileX, tileY);
                out.write(mapColor.getRed());
                out.write(mapColor.getGreen());
                out.write(mapColor.getBlue());
            }
        }

        return rawResponse;
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
}
