package mapsite.spark.websocket;

import mapsite.TileLocation;
import mapsite.webmodel.PlayerInfoModel;
import necesse.engine.GameLog;
import necesse.engine.network.server.Server;
import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebSocket
public class SparkWebSocket {
    private final Server server;
    private final List<Session> sessions = new ArrayList<>();

    public SparkWebSocket(Server server) {
        this.server = server;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connection received from " + session.getRemoteAddress());
        sessions.add(session);
        if (sessions.size() == 1) {
            WebSocketUpdateService.start();
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int code, String reason) {
        System.out.println("Connection closed from " + session.getRemoteAddress());
        System.out.println("Code: " + code + ", Reason: " + reason);
        sessions.remove(session);
        if (sessions.isEmpty()) {
            WebSocketUpdateService.stop();
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received message \"" + message + "\"");
    }

    public boolean hasSessions() {
        return !sessions.isEmpty();
    }

    public void sendMessageToAll(String message) {
        sessions.forEach(session -> {
            session.getRemote().sendString(message, new WriteCallback() {
                @Override
                public void writeFailed(Throwable x) {
                    GameLog.warn.println("Could not send websocket message to " + session.getRemoteAddress());
                    x.printStackTrace(GameLog.warn);
                }

                @Override
                public void writeSuccess() {
                    // GameLog.debug.println("Sent websocket message to " + session.getRemoteAddress());
                }
            });
        });
    }

    public void sendPlayerInformation() {
        List<Map<String, Object>> playerInfo = server
                .streamClients()
                .map(PlayerInfoModel::new)
                .map(PlayerInfoModel::toMap)
                .toList();

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
}
