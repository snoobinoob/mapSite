package mapsite.spark.websocket;

import mapsite.TileLocation;
import mapsite.spark.SparkWebapp;
import necesse.engine.GameLog;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WebSocketUpdateService {
    private static Timer taskTimer = null;
    private static final Lock mapUpdateLock = new ReentrantLock();
    private static final Map<TileLocation, Integer> mapUpdates = new HashMap<>();

    public static void start() {
        if (isRunning()) {
            return;
        }

        taskTimer = new Timer("mapsiteTaskTimer");
        taskTimer.scheduleAtFixedRate(new SendPlayerInfoTask(), 0, 1000);
        taskTimer.scheduleAtFixedRate(new SendMapUpdatesTask(), 0, 5000);
    }

    public static void stop() {
        if (!isRunning()) {
            return;
        }

        taskTimer.cancel();
        taskTimer = null;
        safeOperateOnMapUpdates(mapUpdates::clear);
    }

    public static boolean isRunning() {
        return taskTimer != null;
    }

    public static void putMapUpdate(TileLocation tilePoint, Color color) {
        if (isRunning() && SparkWebapp.websocket.hasSessions()) {
            safeOperateOnMapUpdates(() -> mapUpdates.put(tilePoint, color.getRGB()));
        }
    }

    public static Map<TileLocation, Integer> pullMapUpdates() {
        AtomicReference<Map<TileLocation, Integer>> updatesToSend = new AtomicReference<>();
        safeOperateOnMapUpdates(() -> {
            updatesToSend.set(new HashMap<>(mapUpdates));
            mapUpdates.clear();
        });
        return updatesToSend.get();
    }

    private static void safeOperateOnMapUpdates(Runnable operation) {
        try {
            mapUpdateLock.lock();
            operation.run();
        } catch (Exception e) {
            GameLog.warn.println("Exception operating on mapUpdates: " + e.getMessage());
            e.printStackTrace(GameLog.warn);
        } finally {
            mapUpdateLock.unlock();
        }
    }
}
