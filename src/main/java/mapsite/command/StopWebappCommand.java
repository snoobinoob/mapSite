package mapsite.command;

import mapsite.spark.SparkWebapp;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import spark.Spark;

public class StopWebappCommand extends ModularChatCommand {
    public StopWebappCommand() {
        super("mapsite:stop", "Start web server", PermissionLevel.ADMIN, false);
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] objects, String[] strings, CommandLog commandLog) {
        if (Spark.routes().isEmpty()) {
            commandLog.add("Mapsite already stopped");
            return;
        }
        commandLog.add("Stopping mapsite server");
        SparkWebapp.stop();
    }
}
