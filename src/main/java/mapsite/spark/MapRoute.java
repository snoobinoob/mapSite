package mapsite.spark;

import mapsite.Settings;
import mapsite.TileLocation;
import necesse.engine.GameLog;
import necesse.engine.network.server.Server;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.registries.TileRegistry;
import necesse.engine.registries.VersionMigration;
import necesse.engine.save.LoadData;
import necesse.engine.util.GameMath;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        Point startRegionPoint = startLocation.toRegionPoint();
        Point endRegionPoint = endLocation.toRegionPoint();

        Map<Point, Map<Point, Color>> tileColors = new HashMap<>();
        Set<Point> regionsToLoadFromFileSystem = new HashSet<>();
        boolean containsGeneratedRegion = false;
        for (int y = startRegionPoint.y; y <= endRegionPoint.y; y++) {
            for (int x = startRegionPoint.x; x <= endRegionPoint.x; x++) {
                Point regionPoint = new Point(x, y);
                if (level == null) {
                    regionsToLoadFromFileSystem.add(regionPoint);
                    continue;
                }
                RegionManager regionManager = level.regionManager;
                if (!regionManager.isRegionGenerated(x, y)) {
                    continue;
                }
                containsGeneratedRegion = true;
                if (regionManager.isRegionLoaded(x, y)) {
                    tileColors.put(regionPoint, toColorMap(regionManager.getRegion(x, y, false)));
                    continue;
                }
                regionsToLoadFromFileSystem.add(regionPoint);
            }
        }
        if (!regionsToLoadFromFileSystem.isEmpty()) {
            boolean fileSystemContainsGeneratedRegion = loadRegionsFromFileSystem(tileColors, regionsToLoadFromFileSystem);
            containsGeneratedRegion = containsGeneratedRegion || fileSystemContainsGeneratedRegion;
        }

        HttpServletResponse rawResponse = res.raw();
        ServletOutputStream out = rawResponse.getOutputStream();
        rawResponse.addHeader("content-type", "application/octet-stream");

        if (!containsGeneratedRegion) {
            out.write(0);
            return rawResponse;
        }

        out.write(1);
        int tileX, tileY;
        for (int y = 0; y < Settings.mapChunkSize; y++) {
            tileY = startLocation.tileY + y;
            for (int x = 0; x < Settings.mapChunkSize; x++) {
                tileX = startLocation.tileX + x;
                Point regionPoint = new Point(GameMath.getRegionCoordByTile(tileX), GameMath.getRegionCoordByTile(tileY));
                Map<Point, Color> regionColors = tileColors.get(regionPoint);
                Point offsetPoint = new Point(tileX - regionPoint.x * 16, tileY - regionPoint.y * 16);
                Color mapColor = regionColors == null ? Color.BLACK : regionColors.get(offsetPoint);
                out.write(mapColor.getRed());
                out.write(mapColor.getGreen());
                out.write(mapColor.getBlue());
            }
        }

        return rawResponse;
    };

    private boolean loadRegionsFromFileSystem(Map<Point, Map<Point, Color>> tileColors, Set<Point> regionsToLoad) {
        Map<Point, LoadData> worldSaveDataMap = new HashMap<>();
        for (Point regionPoint : regionsToLoad) {
            int worldRegionX = RegionFilesManager.getWorldRegionPos(regionPoint.x);
            int worldRegionY = RegionFilesManager.getWorldRegionPos(regionPoint.y);
            worldSaveDataMap.computeIfAbsent(new Point(worldRegionX, worldRegionY), key -> {
                WorldFile file = server.world.fileSystem.getWorldRegionFile(LevelIdentifier.SURFACE_IDENTIFIER, worldRegionX, worldRegionY);
                return file.exists() ? new LoadData(file) : null;
            });
        }

        boolean containsGeneratedRegion = false;
        for (Point regionPoint : regionsToLoad) {
            int worldRegionX = RegionFilesManager.getWorldRegionPos(regionPoint.x);
            int worldRegionY = RegionFilesManager.getWorldRegionPos(regionPoint.y);
            LoadData worldSaveData = worldSaveDataMap.get(new Point(worldRegionX, worldRegionY));
            if (worldSaveData != null) {
                LoadData regionData = worldSaveData.getFirstLoadDataByName(regionPoint.x + "x" + regionPoint.y);
                if (regionData != null) {
                    containsGeneratedRegion = true;
                    Map<Point, Color> regionColors = toColorMap(regionData);
                    tileColors.put(regionPoint, regionColors);
                }
            }
        }
        return containsGeneratedRegion;
    }

    private Map<Point, Color> toColorMap(Region region) {
        Map<Point, Color> colorMap = new HashMap<>();
        for (int tileY = 0; tileY < region.tileHeight; tileY++) {
            for (int tileX = 0; tileX < region.tileWidth; tileX++) {
                Point tilePoint = new Point(tileX, tileY);
                GameObject obj = region.objectLayer.getObjectByRegion(0, tileX, tileY);
                if (obj.getID() != 0) {
                    colorMap.put(tilePoint, obj.mapColor);
                    continue;
                }

                GameTile tile = region.tileLayer.getTileByRegion(tileX, tileY);
                if (tile.getID() != 0) {
                    colorMap.put(tilePoint, tile.mapColor);
                    continue;
                }

                GameLog.warn.println("Tile (" + tileX + ", " + tileY + ") is generated, but empty?");
                colorMap.put(tilePoint, Color.BLACK);
            }
        }
        return colorMap;
    }

    private Map<Point, Color> toColorMap(LoadData regionData) {
        Map<Point, Color> regionColors = new HashMap<>();
        short[] objectIDs = loadObjectIDs(regionData);
        short[] tileIDs = loadTileIDs(regionData);
        for (int tileY = 0; tileY < 16; tileY++) {
            for (int tileX = 0; tileX < 16; tileX++) {
                int index = tileY * 16 + tileX;
                Point tilePoint = new Point(tileX, tileY);
                if (objectIDs[index] != 0) {
                    regionColors.put(tilePoint, ObjectRegistry.getObject(objectIDs[index]).mapColor);
                    continue;
                }

                if (tileIDs[index] != 0) {
                    regionColors.put(tilePoint, TileRegistry.getTile(tileIDs[index]).mapColor);
                    continue;
                }

                GameLog.warn.println("Tile (" + tileX + ", " + tileY + ") has no object/tile data");
                regionColors.put(tilePoint, Color.BLACK);
            }
        }
        return regionColors;
    }

    private short[] loadObjectIDs(LoadData regionData) {
        LoadData objectData = regionData.getFirstLoadDataByName("objects");
        short[] data;
        try {
            data = LoadData.getCompressedShortArray(objectData);
        } catch (Exception e) {
            data = LoadData.getShortArray(objectData);
        }
        int[] conversionArray = null;
        if (regionData.hasLoadDataByName("objectIDs")) {
            String[] objectIDs = regionData.getStringArray("objectIDs");
            conversionArray = VersionMigration.generateStringIDsArrayConversionArray(objectIDs, ObjectRegistry.getObjectStringIDs(), 0, VersionMigration.oldObjectStringIDs);
        }

        if (conversionArray != null) {
            int[] intData = new int[data.length];

            for (int i = 0; i < intData.length; ++i) {
                intData[i] = data[i];
            }

            VersionMigration.convertArray(intData, conversionArray);
            for (int i = 0; i < data.length; ++i) {
                data[i] = (short) intData[i];
            }
        }

        return data;
    }

    private short[] loadTileIDs(LoadData regionData) {
        LoadData tileData = regionData.getFirstLoadDataByName("tiles");
        short[] data;
        try {
            data = LoadData.getCompressedShortArray(tileData);
        } catch (Exception e) {
            data = LoadData.getShortArray(tileData);
        }
        int[] conversionArray = null;
        if (regionData.hasLoadDataByName("tileIDs")) {
            String[] tileIDData = regionData.getStringArray("tileIDs");
            conversionArray = VersionMigration.generateStringIDsArrayConversionArray(tileIDData, TileRegistry.getTileStringIDs(), TileRegistry.waterID, VersionMigration.oldTileStringIDs);
        }

        if (conversionArray != null) {
            int[] intData = new int[data.length];

            for (int i = 0; i < intData.length; ++i) {
                intData[i] = data[i];
            }

            VersionMigration.convertArray(intData, conversionArray);
            for (int i = 0; i < data.length; ++i) {
                data[i] = (short) intData[i];
            }
        }

        for (int i = 0; i < data.length; ++i) {
            if (data[i] == -1 || data[i] == TileRegistry.emptyID) {
                data[i] = (short) TileRegistry.waterID;
            }
        }

        return data;
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
