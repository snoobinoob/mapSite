package mapsite.spark;

import mapsite.jte.JteTemplateEngine;
import mapsite.webmodel.ServerInfoModel;
import necesse.engine.network.server.Server;
import spark.Route;
import spark.Spark;

public class IndexRoute extends SparkRouteHandler {
    public IndexRoute(Server server) {
        super(server);
    }

    @Override
    public void register(String path) {
        Spark.path(path, () -> {
            Spark.get("/", handleGetRoot);
            Spark.get("/index.js", getResource("index.js"));
            Spark.get("/chunkFetcher.js", getResource("chunkFetcher.js"));
            Spark.get("/websocket.js", getResource("websocket.js"));
            Spark.get("/styles.css", getResource("styles.css"));
        });
    }

    private final Route handleGetRoot = (req, res) -> {
        ServerInfoModel model = new ServerInfoModel(server);
        return JteTemplateEngine.render("index.jte", model);
    };

    private Route getResource(String resourceName) {
        return (req, res) -> JteTemplateEngine.renderResource(resourceName);
    }
}
