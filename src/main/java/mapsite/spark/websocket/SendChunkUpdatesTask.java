package mapsite.spark.websocket;

import mapsite.spark.SparkWebapp;

import java.awt.*;
import java.util.Set;
import java.util.TimerTask;

public class SendChunkUpdatesTask extends TimerTask {
    @Override
    public void run() {
        Set<Point> chunkUpdates = WebSocketUpdateService.pullChunkUpdates();
        if (chunkUpdates.isEmpty()) {
            return;
        }
        SparkWebapp.websocket.sendChunkUpdates(chunkUpdates);
    }
}
