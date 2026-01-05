package mapsite.spark;

import necesse.engine.GameLog;
import necesse.engine.network.server.Server;
import spark.Spark;

public class SparkWebapp {
    private static Server server;

    public static void init(Server server) {
        System.out.println("Starting webapp");
        SparkWebapp.server = server;
        Spark.port(8080);
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
        Spark.stop();
    }
}
