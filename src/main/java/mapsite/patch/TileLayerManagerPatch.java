package mapsite.patch;

import mapsite.TileLocation;
import mapsite.spark.websocket.WebSocketUpdateService;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.util.LevelIdentifier;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;
import necesse.level.maps.regionSystem.managers.TileLayerManager;
import net.bytebuddy.asm.Advice;

public class TileLayerManagerPatch {
    @ModMethodPatch(target = TileLayerManager.class, name = "setTile", arguments = {int.class, int.class, int.class})
    public static class SetTilePatch {
        @Advice.OnMethodExit
        public static void onExit(@Advice.FieldValue("level") Level level, @Advice.AllArguments Object[] args) {
            if (level.isClient() || !level.getIdentifier().equals(LevelIdentifier.SURFACE_IDENTIFIER)) {
                return;
            }

            int tileX = (int) args[0];
            int tileY = (int) args[1];

            GameObject object = level.getObject(tileX, tileY);
            if (object.getID() == 0) {
                WebSocketUpdateService.putMapUpdate(
                        new TileLocation(level.getIdentifier(), tileX, tileY),
                        level.getTile(tileX, tileY).getMapColor(level, tileX, tileY)
                );
            }
        }
    }
}
