package mapsite.spark;

import mapsite.WebSocketUpdateService;
import mapsite.webmodel.PlayerInfoModel;
import necesse.engine.GameLog;
import necesse.engine.network.server.Server;
import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
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
        sessions.remove(session);
        if (sessions.isEmpty()) {
            WebSocketUpdateService.stop();
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("Received message \"" + message + "\"");
    }

    public void sendMessageToAll(String message) {
        sessions.forEach(session -> {
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                GameLog.warn.println("Could not send websocket message to " + session.getRemoteAddress());
            }
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
}
