package mapsite.spark;

import mapsite.spark.websocket.SparkWebSocket;
import mapsite.spark.websocket.WebSocketUpdateService;
import necesse.engine.GameLog;
import necesse.engine.network.server.Server;
import spark.Spark;

public class SparkWebapp {
    private static Server server;
    public static SparkWebSocket websocket;

    public static void init(Server server) {
        System.out.println("Starting webapp");
        SparkWebapp.server = server;
        websocket = new SparkWebSocket(server);
        Spark.port(8080);
        Spark.webSocket("/ws", websocket);
        registerPath("/", IndexRoute.class);
        registerPath("/map", MapRoute.class);
    }

    private static void registerPath(String path, Class<? extends SparkRouteHandler> handlerClass) {
        try {
            SparkRouteHandler handler = handlerClass.getConstructor(Server.class).newInstance(server);
            handler.register(path);
        } catch (Exception e) {
            GameLog.warn.println("Could not register webhandler " + handlerClass.getSimpleName());
        }
    }

    public static void stop() {
        WebSocketUpdateService.stop();
        Spark.stop();
    }
}
