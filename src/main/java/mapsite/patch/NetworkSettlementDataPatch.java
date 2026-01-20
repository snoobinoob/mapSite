package mapsite.patch;

import mapsite.spark.SparkWebapp;
import mapsite.webmodel.SettlementInfoModel;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.server.ServerClient;
import necesse.level.maps.levelData.settlementData.NetworkSettlementData;
import net.bytebuddy.asm.Advice;

public class NetworkSettlementDataPatch {
    @ModMethodPatch(target = NetworkSettlementData.class, name = "disband", arguments = {})
    public static class DisbandPatch {
        @Advice.OnMethodExit
        public static void onExit(@Advice.This NetworkSettlementData thiz) {
            SparkWebapp.websocket.sendSettlementUpdate(new SettlementInfoModel(thiz.getCacheData(true)));
        }
    }

    @ModMethodPatch(target = NetworkSettlementData.class, name = "setFlagTier", arguments = {int.class})
    public static class SetFlagTierPatch {
        @Advice.OnMethodExit
        public static void onExit(@Advice.This NetworkSettlementData thiz) {
            SparkWebapp.websocket.sendSettlementUpdate(new SettlementInfoModel(thiz.getCacheData(true)));
        }
    }

    @ModMethodPatch(target = NetworkSettlementData.class, name = "setName", arguments = {GameMessage.class})
    public static class SetNamePatch {
        @Advice.OnMethodExit
        public static void onExit(@Advice.This NetworkSettlementData thiz) {
            SparkWebapp.websocket.sendSettlementUpdate(new SettlementInfoModel(thiz.getCacheData(true)));
        }
    }

    @ModMethodPatch(target = NetworkSettlementData.class, name = "setOwner", arguments = {ServerClient.class})
    public static class SetOwnerPatch {
        @Advice.OnMethodExit
        public static void onExit(@Advice.This NetworkSettlementData thiz) {
            SparkWebapp.websocket.sendSettlementUpdate(new SettlementInfoModel(thiz.getCacheData(true)));
        }
    }
}
