package mapsite.webmodel;

import necesse.engine.network.server.Server;

import java.awt.*;

public class ServerInfoModel {
    public Point spawn;

    public ServerInfoModel(Server server) {
        spawn = server.world.worldEntity.defaultSpawnTile;
    }
}
