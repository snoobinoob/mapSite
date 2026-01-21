package mapsite.command;

import mapsite.Settings;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.parameterHandlers.StringParameterHandler;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

public class ConfigureWebappCommand extends ModularChatCommand {

    public ConfigureWebappCommand() {
        super(
                "mapsite:config",
                "Configure web server",
                PermissionLevel.ADMIN,
                false,
                new CmdParameter(
                        "option",
                        new StringParameterHandler(
                                null,
                                "webappPort",
                                "playerUpdateRateMs",
                                "mapUpdateRateMs",
                                "mapChunkFetchRateMs",
                                "mapChunkSize",
                                "mapChunkUpdateRateMs"
                        ),
                        true,
                        new CmdParameter(
                                "value",
                                new StringParameterHandler(),
                                true
                        )
                )
        );
    }

    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] strings, CommandLog log) {
        String option = (String) args[0];
        String value = (String) args[1];

        if (option == null) {
            Settings.printToLog(log);
            return;
        }

        if (Settings.hasOption(option)) {
            if (value == null) {
                Settings.logOptionValue(option, log);
            } else {
                Settings.setOptionAndLog(option, value, log);
            }
        }
    }
}
