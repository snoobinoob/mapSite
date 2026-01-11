package mapsite.patch;

import mapsite.spark.websocket.WebSocketUpdateService;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.level.maps.SurfaceLevel;
import necesse.level.maps.regionSystem.Region;
import net.bytebuddy.asm.Advice;

public class SurfaceLevelPatch {
    @ModMethodPatch(target = SurfaceLevel.class, name = "generateRegion", arguments = {Region.class})
    public static class GenerateRegionPatch {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Argument(0) Region region) {
            WebSocketUpdateService.setFullRegionUpdating(region);
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(0) Region region) {
            WebSocketUpdateService.setFullRegionUpdated(region);
        }
    }
}
