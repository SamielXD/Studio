package fuse;

import arc.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;

public class FuseCamera {
    
    private float smoothRotation = 0f;
    private Vec2 offset = new Vec2();
    private float targetZoom = 2f;
    private float currentZoom = 4f;
    
    public FuseCamera() {
        Events.run(Trigger.update, this::update);
    }
    
    private void update() {
        if (!FuseMod.enabled || Vars.player == null) return;
        
        Unit unit = Vars.player.unit();
        if (unit == null) return;
        
        float lerpSpeed = 0.15f;
        float rotLerpSpeed = 0.1f;
        
        smoothRotation = Mathf.lerpDelta(smoothRotation, unit.rotation, rotLerpSpeed);
        
        float offsetDist = 8f;
        offset.set(0, offsetDist).rotate(smoothRotation);
        
        Vec2 targetPos = new Vec2(unit.x + offset.x, unit.y + offset.y);
        
        Core.camera.position.lerpDelta(targetPos, lerpSpeed);
        
        currentZoom = Mathf.lerpDelta(currentZoom, targetZoom, 0.05f);
        Vars.renderer.minZoom = currentZoom;
        Vars.renderer.maxZoom = currentZoom;
    }
    
    public float getRotation() {
        return smoothRotation;
    }
    
    public void reset() {
        Vars.renderer.minZoom = 1.5f;
        Vars.renderer.maxZoom = 6f;
    }
}