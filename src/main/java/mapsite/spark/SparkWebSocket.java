package mapsite.spark;

import necesse.engine.GameLog;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebSocket
public class SparkWebSocket {
    private final List<Session> sessions = new ArrayList<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connection received from " + session.getRemoteAddress());
        sessions.add(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int code, String reason) {
        System.out.println("Connection closed from " + session.getRemoteAddress());
        sessions.remove(session);
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
}
