package mapsite;

import mapsite.command.ConfigureWebappCommand;
import mapsite.command.StartWebappCommand;
import mapsite.command.StopWebappCommand;
import mapsite.jte.JteTemplateEngine;
import mapsite.spark.SparkWebapp;
import necesse.engine.GameEventListener;
import necesse.engine.GameEvents;
import necesse.engine.GameLog;
import necesse.engine.commands.CommandsManager;
import necesse.engine.events.ServerStartEvent;
import necesse.engine.events.ServerStopEvent;
import necesse.engine.modLoader.ModSettings;
import necesse.engine.modLoader.annotations.ModEntry;

@ModEntry
public class MapSiteEntry {
    public ModSettings initSettings() {
        return new Settings();
    }

    public void postInit() {
        GameEvents.addListener(ServerStartEvent.class, new GameEventListener<ServerStartEvent>() {
            @Override
            public void onEvent(ServerStartEvent serverStartEvent) {
                GameLog.out.println("Starting mapsite server on port " + Settings.webappPort);
                JteTemplateEngine.init();
                SparkWebapp.init(serverStartEvent.server);
            }
        });

        GameEvents.addListener(ServerStopEvent.class, new GameEventListener<ServerStopEvent>() {
            @Override
            public void onEvent(ServerStopEvent serverStopEvent) {
                GameLog.out.println("Stopping mapsite server");
                SparkWebapp.stop();
            }
        });

        CommandsManager.registerServerCommand(new StartWebappCommand());
        CommandsManager.registerServerCommand(new StopWebappCommand());
        CommandsManager.registerServerCommand(new ConfigureWebappCommand());
    }
}
