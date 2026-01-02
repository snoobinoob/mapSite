package mapsite;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateException;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import mapsite.webmodel.ServerInfoModel;
import necesse.engine.GameEventListener;
import necesse.engine.GameEvents;
import necesse.engine.GlobalData;
import necesse.engine.events.ServerStartEvent;
import necesse.engine.events.ServerStopEvent;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.server.ServerClient;
import necesse.engine.state.MainGame;
import spark.Spark;

import java.util.List;

@ModEntry
public class MapSiteEntry {
    public void init() {
        System.out.println("Hello World!");
    }

    public void postInit() {
        GameEvents.addListener(ServerStartEvent.class, new GameEventListener<ServerStartEvent>() {
            @Override
            public void onEvent(ServerStartEvent serverStartEvent) {
                System.out.println("Starting map webapp");
                JteTemplateEngine.init();
                Spark.port(8080);
                Spark.get("/", (req, res) -> {
                    if (!(GlobalData.getCurrentState() instanceof MainGame)) {
                        return "Not running the main game";
                    }

                    List<String> onlinePlayers = serverStartEvent.server.streamClients().map(ServerClient::getName).toList();
                    ServerInfoModel model = new ServerInfoModel(onlinePlayers);

                    return JteTemplateEngine.render("index.jte", model);
                });
            }
        });

        GameEvents.addListener(ServerStopEvent.class, new GameEventListener<ServerStopEvent>() {
            @Override
            public void onEvent(ServerStopEvent serverStopEvent) {
                System.out.println("Stopping map webapp");
                Spark.stop();
            }
        });
    }

    public String renderTemplate(Object model, String templateName) {
        TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);

        TemplateOutput output = new StringOutput();
        try {
            templateEngine.render(templateName, model, output);
            return output.toString();
        } catch (TemplateException e) {
            return e.getMessage();
        }
    }
}
