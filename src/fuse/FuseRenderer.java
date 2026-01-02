package fuse;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;

public class FuseRenderer {
    
    private float worldRotation = 0f;
    
    public FuseRenderer() {
        Events.run(Trigger.draw, this::render);
    }
    
    private void render() {
        if (!FuseMod.enabled || Vars.player == null) return;
        
        Unit unit = Vars.player.unit();
        if (unit == null) return;
        
        worldRotation = -unit.rotation + 90f;
        
        Draw.trans().rotate(worldRotation * Mathf.degRad, Core.camera.position.x, Core.camera.position.y);
    }
    
    public void applyRotation() {
        if (!FuseMod.enabled) return;
        
        Unit unit = Vars.player.unit();
        if (unit == null) return;
        
        float cx = Core.camera.position.x;
        float cy = Core.camera.position.y;
        
        Draw.trans().translate(cx, cy);
        Draw.trans().rotate(worldRotation * Mathf.degRad);
        Draw.trans().translate(-cx, -cy);
    }
    
    public void reset() {
        Draw.reset();
    }
}