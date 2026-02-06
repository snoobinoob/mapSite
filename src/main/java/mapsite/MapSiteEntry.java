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
import necesse.engine.gameLoop.GameLoopListener;
import necesse.engine.gameLoop.ServerGameLoop;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.modLoader.ModSettings;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.window.GameWindow;

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

                TickManager tm = serverStartEvent.server.tickManager();
                if (tm instanceof ServerGameLoop) {
                    ServerGameLoop loop = (ServerGameLoop) tm;

                    loop.addGameLoopListener(new GameLoopListener() {
                        @Override
                        public void frameTick(TickManager tickManager, GameWindow window) {
                            if (tickManager.isGameTick()) {
                                MainThread.drainOnce(500);
                            }
                        }

                        @Override
                        public void drawTick(TickManager tickManager) {
                        }

                        @Override
                        public boolean isDisposed() {
                            return serverStartEvent.server.hasClosed();
                        }
                    });

                    MainThread.markReady();
                    GameLog.out.println("[MapSite] Installed MainThread ServerGameLoop drainer");
                } else {
                    GameLog.warn.println("[MapSite] tickManager is not ServerGameLoop – drainer not installed");
                }
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
