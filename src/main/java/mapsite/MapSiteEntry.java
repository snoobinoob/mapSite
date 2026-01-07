package mapsite;

import mapsite.jte.JteTemplateEngine;
import mapsite.spark.SparkWebapp;
import necesse.engine.GameEventListener;
import necesse.engine.GameEvents;
import necesse.engine.commands.*;
import necesse.engine.commands.parameterHandlers.StringParameterHandler;
import necesse.engine.events.ServerStartEvent;
import necesse.engine.events.ServerStopEvent;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

@ModEntry
public class MapSiteEntry {
    public void init() {
        System.out.println("Hello World!");
    }

    public void postInit() {
        GameEvents.addListener(ServerStartEvent.class, new GameEventListener<ServerStartEvent>() {
            @Override
            public void onEvent(ServerStartEvent serverStartEvent) {
                JteTemplateEngine.init();
                SparkWebapp.init(serverStartEvent.server);
            }
        });

        GameEvents.addListener(ServerStopEvent.class, new GameEventListener<ServerStopEvent>() {
            @Override
            public void onEvent(ServerStopEvent serverStopEvent) {
                SparkWebapp.stop();
            }
        });

        CommandsManager.registerServerCommand(new ModularChatCommand(
                "mapsite:ws_send",
                "Send websocket message",
                PermissionLevel.ADMIN,
                false,
                new CmdParameter("message", new StringParameterHandler(), false)
        ) {
            @Override
            public void runModular(Client client, Server server, ServerClient serverClient, Object[] objects, String[] strings, CommandLog commandLog) {
                SparkWebapp.websocket.sendMessageToAll((String) objects[0]);
            }
        });
    }
}
