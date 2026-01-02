package fuse;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.gen.*;

public class FuseMod extends Mod {
    
    public static boolean enabled = false;
    public static FuseCamera camera;
    public static FuseRenderer renderer;
    public static FuseControls controls;
    public static FuseHUD hud;
    
    private Unit lastControlledUnit = null;
    
    public FuseMod() {
        Log.info("Fuse mod initializing...");
        
        Events.on(ClientLoadEvent.class, e -> {
            initialize();
        });
    }
    
    private void initialize() {
        Log.info("Fuse systems starting...");
        
        camera = new FuseCamera();
        renderer = new FuseRenderer();
        controls = new FuseControls();
        hud = new FuseHUD();
        
        Events.run(Trigger.update, () -> {
            checkUnitControl();
        });
        
        Log.info("Fuse mod loaded successfully!");
    }
    
    private void checkUnitControl() {
        if (Vars.player == null) return;
        
        Unit currentUnit = Vars.player.unit();
        
        if (currentUnit != null && !currentUnit.isPlayer() && currentUnit != lastControlledUnit) {
            lastControlledUnit = currentUnit;
            enableFuse();
        }
        
        if (enabled && (currentUnit == null || currentUnit.isPlayer())) {
            disableFuse();
            lastControlledUnit = null;
        }
    }
    
    public static void enableFuse() {
        if (enabled) return;
        enabled = true;
        controls.show();
        hud.show();
        Log.info("Fuse mode: ACTIVE");
    }
    
    public static void disableFuse() {
        if (!enabled) return;
        enabled = false;
        controls.hide();
        hud.hide();
        camera.reset();
        Log.info("Fuse mode: INACTIVE");
    }
}