package mapsite.spark.websocket;

import mapsite.spark.SparkWebapp;

import java.util.TimerTask;

public class SendPlayerInfoTask extends TimerTask {
    @Override
    public void run() {
        SparkWebapp.websocket.sendPlayerInformation();
    }
}
