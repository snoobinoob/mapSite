package mapsite.spark;

import mapsite.jte.JteTemplateEngine;
import mapsite.webmodel.ServerInfoModel;
import necesse.engine.network.server.Server;
import spark.Route;
import spark.Spark;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class IndexRoute extends SparkRouteHandler {
    public IndexRoute(Server server) {
        super(server);
    }

    @Override
    public void register(String path) {
        Spark.path(path, () -> {
            Spark.get(path, handleGetRoot);
            Spark.get(path + "index.js", getResource("index.js"));
            Spark.get(path + "chunkFetcher.js", getResource("chunkFetcher.js"));
            Spark.get(path + "websocket.js", getResource("websocket.js"));
            Spark.get(path + "styles.css", getResource("styles.css"));
            Spark.get(path + "player.png", getResource("player.png", "image/webp"));
        });
    }

    private final Route handleGetRoot = (req, res) -> {
        ServerInfoModel model = new ServerInfoModel(server);
        return JteTemplateEngine.render("index.jte", model);
    };

    private Route getResource(String resourceName) {
        return getResource(resourceName, "text/html");
    }

    private Route getResource(String resourceName, String contentType) {
        return (req, res) -> {
            res.header("content-type", contentType);

            HttpServletResponse rawResponse = res.raw();
            try (ServletOutputStream out = rawResponse.getOutputStream()) {
                JteTemplateEngine.writeResourceBytes(resourceName, out);
                out.flush();
            } catch (Exception e) {
                res.status(500);
                return "Error reading resource: " + e.getMessage();
            }
            return rawResponse;
        };
    }
}
