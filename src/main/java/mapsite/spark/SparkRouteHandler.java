package mapsite.spark;

import necesse.engine.network.server.Server;

public abstract class SparkRouteHandler {
    protected final Server server;

    public SparkRouteHandler(Server server) {
        this.server = server;
    }

    public abstract void register(String path);
}
