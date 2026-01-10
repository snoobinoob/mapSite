package mapsite.patch;

import mapsite.TileLocation;
import mapsite.spark.websocket.WebSocketUpdateService;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.util.LevelIdentifier;
import necesse.level.gameObject.GameObject;
import necesse.level.gameTile.GameTile;
import necesse.level.maps.Level;
import necesse.level.maps.regionSystem.managers.ObjectLayerManager;
import net.bytebuddy.asm.Advice;

public class ObjectLayerManagerPatch {
    @ModMethodPatch(
            target = ObjectLayerManager.class,
            name = "setObject",
            arguments = {int.class, int.class, int.class, int.class}
    )
    public static class SetObjectPatch {
        @Advice.OnMethodExit
        public static void onExit(@Advice.FieldValue("level") Level level, @Advice.AllArguments Object[] args) {
            if (level.isClient() || !level.getIdentifier().equals(LevelIdentifier.SURFACE_IDENTIFIER)) {
                return;
            }
            int tileX = (int) args[1];
            int tileY = (int) args[2];
            int objectID = (int) args[3];

            GameObject object = ObjectRegistry.getObject(objectID);
            if (objectID == 0) {
                GameTile tile = level.getTile(tileX, tileY);
                WebSocketUpdateService.putMapUpdate(
                        new TileLocation(level.getIdentifier(), tileX, tileY),
                        tile.getMapColor(level, tileX, tileY));
            } else {
                WebSocketUpdateService.putMapUpdate(
                        new TileLocation(level.getIdentifier(), tileX, tileY),
                        object.getMapColor(level, tileX, tileY)
                );
            }
        }
    }
}
