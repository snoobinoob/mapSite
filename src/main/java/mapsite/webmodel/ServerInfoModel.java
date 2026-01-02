package mapsite.webmodel;

import java.util.List;

public class ServerInfoModel {
    public List<String> onlinePlayers;

    public ServerInfoModel(List<String> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
}
