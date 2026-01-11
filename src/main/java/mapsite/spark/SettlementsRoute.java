package mapsite.spark;

import mapsite.webmodel.SettlementInfoModel;
import necesse.engine.network.server.Server;
import necesse.engine.world.worldData.SettlementsWorldData;
import org.eclipse.jetty.util.ajax.JSON;
import spark.Route;
import spark.Spark;

import java.util.List;
import java.util.Map;

public class SettlementsRoute extends SparkRouteHandler {
    public SettlementsRoute(Server server) {
        super(server);
    }

    @Override
    public void register(String path) {
        Spark.get(path, handleGetIndex);
    }

    private final Route handleGetIndex = (req, res) -> {
        SettlementsWorldData settlementsData = (SettlementsWorldData) server.world.worldEntity.getWorldData("settlements");
        List<Map<String, Object>> settlements = settlementsData
                .streamSettlements()
                .map(SettlementInfoModel::new)
                .map(SettlementInfoModel::toMap)
                .toList();
        JSON json = new JSON();
        return json.toJSON(settlements);
    };
}
