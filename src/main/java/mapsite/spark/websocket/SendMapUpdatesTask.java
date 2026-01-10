package mapsite.spark.websocket;

import mapsite.TileLocation;
import mapsite.spark.SparkWebapp;

import java.util.Map;
import java.util.TimerTask;

public class SendMapUpdatesTask extends TimerTask {
    @Override
    public void run() {
        Map<TileLocation, Integer> mapUpdates = WebSocketUpdateService.pullMapUpdates();
        if (mapUpdates.isEmpty()) {
            return;
        }
        SparkWebapp.websocket.sendMapUpdates(mapUpdates);
    }
}
