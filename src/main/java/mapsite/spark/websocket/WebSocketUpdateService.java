package mapsite.spark.websocket;

import mapsite.Settings;
import mapsite.TileLocation;
import mapsite.Utils;
import mapsite.spark.SparkWebapp;
import necesse.engine.GameLog;
import necesse.engine.util.GameMath;
import necesse.level.maps.regionSystem.Region;

import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WebSocketUpdateService {
    private static Timer taskTimer = null;
    private static final Lock mapUpdateLock = new ReentrantLock();
    private static final Map<TileLocation, Integer> mapUpdates = new HashMap<>();
    private static final Lock chunkUpdateLock = new ReentrantLock();
    private static final Set<Point> chunksToUpdate = new HashSet<>();
    private static final Set<Point> fullRegionsUpdating = new HashSet<>();

    public static void start() {
        if (isRunning()) {
            return;
        }

        taskTimer = new Timer("mapsiteTaskTimer");
        taskTimer.scheduleAtFixedRate(new SendPlayerInfoTask(), 0, Settings.playerUpdateRateMs);
        taskTimer.scheduleAtFixedRate(new SendMapUpdatesTask(), 0, Settings.mapUpdateRateMs);
        taskTimer.scheduleAtFixedRate(new SendChunkUpdatesTask(), 0, Settings.mapChunkUpdateRateMs);
    }

    public static void stop() {
        if (!isRunning()) {
            return;
        }

        taskTimer.cancel();
        taskTimer = null;
        safeOperateWithLock(mapUpdates::clear, mapUpdateLock);
        safeOperateWithLock(() -> {
            fullRegionsUpdating.clear();
            chunksToUpdate.clear();
        }, chunkUpdateLock);
    }

    public static boolean isRunning() {
        return taskTimer != null;
    }

    public static void setFullRegionUpdating(Region region) {
        safeOperateWithLock(() -> {
            WebSocketUpdateService.fullRegionsUpdating.add(new Point(region.regionX, region.regionY));
        }, chunkUpdateLock);
    }

    public static void setFullRegionUpdated(Region region) {
        safeOperateWithLock(() -> {
            WebSocketUpdateService.fullRegionsUpdating.remove(new Point(region.regionX, region.regionY));
            WebSocketUpdateService.chunksToUpdate.add(Utils.toChunkPoint(region));
        }, chunkUpdateLock);
    }

    public static void putMapUpdate(TileLocation tilePoint, Color color) {
        if (!isRunning() || !SparkWebapp.websocket.hasSessions()) {
            return;
        }
        AtomicBoolean shouldUseUpdate = new AtomicBoolean(true);
        Point regionPoint = new Point(GameMath.getRegionCoordByTile(tilePoint.tileX), GameMath.getRegionCoordByTile(tilePoint.tileY));
        Point chunkPoint = Utils.toChunkPoint(tilePoint);
        safeOperateWithLock(() -> {
            shouldUseUpdate.set(!(fullRegionsUpdating.contains(regionPoint) || chunksToUpdate.contains(chunkPoint)));
        }, chunkUpdateLock);
        if (shouldUseUpdate.get()) {
            safeOperateWithLock(() -> mapUpdates.put(tilePoint, color.getRGB()), mapUpdateLock);
        }
    }

    public static Map<TileLocation, Integer> pullMapUpdates() {
        AtomicReference<Map<TileLocation, Integer>> updatesToSend = new AtomicReference<>();
        safeOperateWithLock(() -> {
            updatesToSend.set(new HashMap<>(mapUpdates));
            mapUpdates.clear();
        }, mapUpdateLock);
        return updatesToSend.get();
    }

    public static Set<Point> pullChunkUpdates() {
        AtomicReference<Set<Point>> updatesToSend = new AtomicReference<>();
        safeOperateWithLock(() -> {
            updatesToSend.set(new HashSet<>(chunksToUpdate));
            chunksToUpdate.clear();
        }, chunkUpdateLock);
        return updatesToSend.get();
    }

    private static void safeOperateWithLock(Runnable operation, Lock lock) {
        try {
            lock.lock();
            operation.run();
        } catch (Exception e) {
            GameLog.warn.println("Exception operating: " + e.getMessage());
            e.printStackTrace(GameLog.warn);
        } finally {
            lock.unlock();
        }
    }
}
