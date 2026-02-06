package mapsite.spark.websocket;

import mapsite.MainThread;
import mapsite.Settings;
import mapsite.TileLocation;
import mapsite.webmodel.PlayerInfoModel;
import mapsite.webmodel.SettlementInfoModel;
import necesse.engine.GameLog;
import necesse.engine.network.server.Server;
import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@WebSocket
public class SparkWebSocket {
    private final Server server;

    private static final List<Session> sessions = Collections.synchronizedList(new ArrayList<>());

    private static final Map<String, Session> sessionByIp = Collections.synchronizedMap(new HashMap<>());

    private static final ScheduledExecutorService KICK_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "MapSite-WebMap-KickScheduler");
                thread.setDaemon(true);
                return thread;
            });

    private static final Map<Session, ScheduledFuture<?>> kickTasks = Collections.synchronizedMap(new HashMap<>());

    public SparkWebSocket(Server server) {
        this.server = server;
    }

    private static int getMaxViewers() {
        return Settings.maxWebMapViewers <= 0 ? 5 : Settings.maxWebMapViewers;
    }

    private static int getKickMinutes() {
        return Settings.maxWebMapSessionMinutes; //  0 disabled
    }

    private static String getIp(Session session) {
        try {
            if (session != null && session.getRemoteAddress() != null && session.getRemoteAddress().getAddress() != null) {
                return session.getRemoteAddress().getAddress().getHostAddress();
            }
        } catch (Throwable ignored) {
        }
        return "unknown";
    }

    private static void cleanupSession(Session session) {
        // cancel kick timer
        ScheduledFuture<?> kickTask;
        synchronized (kickTasks) {
            kickTask = kickTasks.remove(session);
        }
        if (kickTask != null) {
            try {
                kickTask.cancel(false);
            } catch (Throwable ignored) {
            }
        }

        String ip = getIp(session);
        synchronized (sessions) {
            sessions.remove(session);

            Session mappedSession = sessionByIp.get(ip);
            if (mappedSession == session) {
                sessionByIp.remove(ip);
            }

            if (sessions.isEmpty()) {
                WebSocketUpdateService.stop();
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        final int maxViewers = getMaxViewers();
        final String ip = getIp(session);

        synchronized (sessions) {
            // ---- 1 session per IP ----
            Session existingSession = sessionByIp.get(ip);
            if (existingSession != null && existingSession.isOpen()) {
                try {
                    session.close(1008, "Only 1 web map session allowed per IP.");
                } catch (Exception ignored) {
                }
                return;
            }

            // ---- max viewers ----
            if (sessions.size() >= maxViewers) {
                try {
                    session.close(1008, "Web map is full (" + maxViewers + " max viewers). Try again later.");
                } catch (Exception ignored) {
                }
                return;
            }

            sessions.add(session);
            sessionByIp.put(ip, session);

            if (sessions.size() == 1) {
                WebSocketUpdateService.start();
            }
        }

        // ---- timeout kick ----
        int kickMinutes = getKickMinutes();
        if (kickMinutes > 0) {
            ScheduledFuture<?> kickTask = KICK_SCHEDULER.schedule(() -> {
                try {
                    if (session != null && session.isOpen()) {
                        session.close(1001, "Session timed out (" + kickMinutes + " minutes).");
                    }
                } finally {
                    cleanupSession(session);
                }
            }, kickMinutes, TimeUnit.MINUTES);

            synchronized (kickTasks) {
                kickTasks.put(session, kickTask);
            }
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int code, String reason) {
        cleanupSession(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
    }

    public boolean hasSessions() {
        synchronized (sessions) {
            return !sessions.isEmpty();
        }
    }

    public void sendMessageToAll(String message) {
        List<Session> currentSessions;
        synchronized (sessions) {
            currentSessions = new ArrayList<>(sessions);
        }

        for (Session session : currentSessions) {
            try {
                if (session == null || !session.isOpen()) continue;

                session.getRemote().sendString(message, new WriteCallback() {
                    @Override
                    public void writeFailed(Throwable x) {
                        GameLog.warn.println("Could not send websocket message to " + session.getRemoteAddress());
                        x.printStackTrace(GameLog.warn);
                    }

                    @Override
                    public void writeSuccess() {
                    }
                });
            } catch (Throwable t) {
                cleanupSession(session);
            }
        }
    }

    void sendPlayerInformation() {
        List<Map<String, Object>> playerInfo = MainThread.call(() ->
                server.streamClients()
                        .map(PlayerInfoModel::new)
                        .map(PlayerInfoModel::toMap)
                        .toList()
        );

        JSON json = new JSON();
        Map<String, Object> toSend = new HashMap<>();
        toSend.put("players", playerInfo);
        sendMessageToAll(json.toJSON(toSend));
    }

    public void sendMapUpdates(Map<TileLocation, Integer> mapUpdates) {
        JSON json = new JSON();
        Map<String, Object> toSend = new HashMap<>();
        List<Object[]> updateList = new ArrayList<>();
        mapUpdates.forEach((tileLocation, colorValue) ->
                updateList.add(new Object[]{tileLocation.tileX, tileLocation.tileY, colorValue})
        );
        toSend.put("mapUpdates", updateList);
        sendMessageToAll(json.toJSON(toSend));
    }

    public void sendChunkUpdates(Set<Point> chunkUpdates) {
        JSON json = new JSON();
        Map<String, Object> toSend = new HashMap<>();
        List<Object[]> updateList = new ArrayList<>();
        chunkUpdates.forEach(chunkPoint -> updateList.add(new Object[]{chunkPoint.x, chunkPoint.y}));
        toSend.put("chunkUpdates", updateList);
        sendMessageToAll(json.toJSON(toSend));
    }

    public void sendSettlementUpdate(SettlementInfoModel settlementInfo) {
        String payload = MainThread.call(() -> {
            JSON json = new JSON();
            Map<String, Object> toSend = new HashMap<>();
            toSend.put("settlementUpdate", settlementInfo.toMap(server));
            return json.toJSON(toSend);
        });
        sendMessageToAll(payload);
    }
}
