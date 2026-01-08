package mapsite;

import mapsite.spark.SparkWebapp;

import java.util.Timer;
import java.util.TimerTask;

public class WebSocketUpdateService {
    private static Timer playerInfoTimer = null;

    private static final TimerTask sendPlayerInfoTask = new TimerTask() {
        @Override
        public void run() {
            SparkWebapp.websocket.sendPlayerInformation();
        }
    };

    public static void start() {
        if (playerInfoTimer == null) {
            playerInfoTimer = new Timer("playerInfoTimer");
            playerInfoTimer.scheduleAtFixedRate(sendPlayerInfoTask, 1000, 500);
        }
    }

    public static void stop() {
        playerInfoTimer.cancel();
        playerInfoTimer = null;
    }
}
