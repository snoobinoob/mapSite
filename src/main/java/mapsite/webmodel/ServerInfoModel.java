package mapsite.webmodel;

import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

import java.awt.*;
import java.util.List;

public class ServerInfoModel {
    public Point spawn;
    public List<String> onlinePlayers;

    public ServerInfoModel(Server server) {
        spawn = server.world.worldEntity.defaultSpawnTile;
        onlinePlayers = server.streamClients().map(ServerClient::getName).toList();
    }
}
