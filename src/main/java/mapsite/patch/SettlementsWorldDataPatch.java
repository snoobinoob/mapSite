package mapsite.patch;

import mapsite.spark.SparkWebapp;
import mapsite.webmodel.SettlementInfoModel;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.world.worldData.SettlementsWorldData;
import necesse.level.maps.Level;
import necesse.level.maps.levelData.settlementData.CachedSettlementData;
import net.bytebuddy.asm.Advice;

import java.util.function.Predicate;

public class SettlementsWorldDataPatch {
    public static boolean settlementCreated;
    public static int createdSettlementID;
    public static final Predicate<CachedSettlementData> isCreatedSettlement = s -> s.uniqueID == createdSettlementID;

    @ModMethodPatch(target = SettlementsWorldData.class, name = "getNewSettlementUniqueID", arguments = {})
    public static class GetNewSettlementUniqueIDPatch {
        @Advice.OnMethodExit
        public static void onExit(@Advice.Return int settlementID) {
            settlementCreated = true;
            createdSettlementID = settlementID;
        }
    }

    @ModMethodPatch(target = SettlementsWorldData.class, name = "getOrCreateLoadedData", arguments = {Level.class, int.class, int.class})
    public static class GetOrCreateLoadedDataPatch {
        @Advice.OnMethodEnter
        public static void onEnter() {
            settlementCreated = false;
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.This SettlementsWorldData thiz) {
            if (settlementCreated) {
                CachedSettlementData settlementData = thiz.streamSettlements()
                        .filter(isCreatedSettlement)
                        .findAny()
                        .orElseThrow();
                SparkWebapp.websocket.sendSettlementUpdate(new SettlementInfoModel(settlementData));
            }
        }
    }
}
