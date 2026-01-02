package studio;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import mindustry.ui.Fonts;

public class NodeCanvas extends Element {

    private NodeEditor editor;
    private Node dragging = null;
    private Vec2 dragOffset = new Vec2();
    private Vec2 panOffset = new Vec2();
    private boolean panning = false;
    private Vec2 panStart = new Vec2();
    
    private Node connecting = null;
    private Vec2 connectionEnd = new Vec2();
    
    private CanvasMode mode = CanvasMode.MOVE;
    
    private boolean isMobile = arc.Core.app.isMobile();
    
    private int touchPointers = 0;
    private Vec2 touch1Start = new Vec2();
    private Vec2 touch2Start = new Vec2();
    private float initialPinchDist = 0f;

    public NodeCanvas(NodeEditor editor) {
        this.editor = editor;
        setTouchable(Touchable.enabled);
        
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                touchPointers++;
                
                if (isMobile && touchPointers == 2) {
                    touch1Start.set(x, y);
                    touch2Start.set(x, y);
                    panning = true;
                    return true;
                }
                
                if (!isMobile && button == KeyCode.mouseRight) {
                    panning = true;
                    panStart.set(x, y);
                    return true;
                }
                
                Vec2 worldPos = screenToWorld(x, y);
                
                for (Node node : editor.getNodes()) {
                    if (node.contains(worldPos.x, worldPos.y)) {
                        handleNodeClick(node, worldPos);
                        return true;
                    }
                }
                
                if (mode == CanvasMode.CONNECT) {
                    connecting = null;
                }
                
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (isMobile && touchPointers >= 2) {
                    float dx = x - panStart.x;
                    float dy = y - panStart.y;
                    panOffset.add(dx * 0.5f, dy * 0.5f);
                    panStart.set(x, y);
                    return;
                }
                
                if (panning) {
                    panOffset.add((x - panStart.x) * 0.5f, (y - panStart.y) * 0.5f);
                    panStart.set(x, y);
                    return;
                }
                
                Vec2 worldPos = screenToWorld(x, y);
                
                if (mode == CanvasMode.MOVE && dragging != null) {
                    dragging.x = worldPos.x - dragOffset.x;
                    dragging.y = worldPos.y - dragOffset.y;
                }
                
                if (mode == CanvasMode.CONNECT && connecting != null) {
                    connectionEnd.set(worldPos);
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                touchPointers--;
                if (touchPointers < 0) touchPointers = 0;
                
                dragging = null;
                if (touchPointers == 0) {
                    panning = false;
                }
            }
            
            @Override
            public boolean keyDown(InputEvent event, KeyCode keycode) {
                if (!isMobile && keycode == KeyCode.del) {
                    editor.getNodes().each(node -> {
                        if (node.selected) {
                            editor.removeNode(node);
                        }
                    });
                    return true;
                }
                return false;
            }
        });
    }
    
    void handleNodeClick(Node node, Vec2 worldPos) {
        switch (mode) {
            case MOVE:
                dragging = node;
                dragOffset.set(worldPos.x - node.x, worldPos.y - node.y);
                break;
                
            case CONNECT:
                if (connecting == null) {
                    connecting = node;
                    connectionEnd.set(worldPos);
                    mindustry.Vars.ui.showInfoToast("[accent]Tap another node to connect", 1f);
                } else {
                    if (connecting != node) {
                        connecting.connectTo(node);
                        mindustry.Vars.ui.showInfoToast("[lime]Nodes connected!", 1f);
                    }
                    connecting = null;
                }
                break;
                
            case DELETE:
                editor.removeNode(node);
                mindustry.Vars.ui.showInfoToast("[scarlet]Node deleted", 1f);
                break;
        }
    }
    
    public void setMode(CanvasMode newMode) {
        this.mode = newMode;
        connecting = null;
        dragging = null;
        
        String modeName = newMode.name();
        mindustry.Vars.ui.showInfoToast("[accent]Mode: " + modeName, 1f);
    }

    Vec2 screenToWorld(float x, float y) {
        return new Vec2(x - panOffset.x, y - panOffset.y);
    }

    @Override
    public void draw() {
        super.draw();
        
        drawGrid();
        drawConnections();
        drawNodes();
        
        if (mode == CanvasMode.CONNECT && connecting != null) {
            drawConnectionLine(connecting.x + connecting.width / 2f + panOffset.x, 
                             connecting.y + connecting.height / 2f + panOffset.y,
                             connectionEnd.x + panOffset.x, 
                             connectionEnd.y + panOffset.y,
                             Color.white);
        }
        
        drawModeIndicator();
    }

    void drawGrid() {
        Lines.stroke(1f);
        Draw.color(Color.gray, 0.1f);
        
        float gridSize = 50f;
        float startX = (panOffset.x % gridSize);
        float startY = (panOffset.y % gridSize);
        
        for (float gx = startX; gx < width; gx += gridSize) {
            Lines.line(gx, 0, gx, height);
        }
        
        for (float gy = startY; gy < height; gy += gridSize) {
            Lines.line(0, gy, width, gy);
        }
        
        Draw.reset();
    }

    void drawNodes() {
        float nodeWidth = isMobile ? 180f : 150f;
        float nodeHeight = isMobile ? 100f : 80f;
        
        for (Node node : editor.getNodes()) {
            node.width = nodeWidth;
            node.height = nodeHeight;
            
            float screenX = node.x + panOffset.x;
            float screenY = node.y + panOffset.y;
            
            if (screenX + node.width < 0 || screenX > width) continue;
            if (screenY + node.height < 0 || screenY > height) continue;
            
            Draw.color(0, 0, 0, 0.8f);
            Fill.rect(screenX + node.width/2f + 2f, screenY + node.height/2f - 2f, node.width, node.height);
            
            Draw.color(node.color.r * 0.3f, node.color.g * 0.3f, node.color.b * 0.3f, 1f);
            Fill.rect(screenX + node.width/2f, screenY + node.height/2f, node.width, node.height);
            
            Draw.color(node.color);
            Fill.rect(screenX + node.width/2f, screenY + node.height/2f + node.height/4f, node.width, node.height/2f);
            
            Lines.stroke(isMobile ? 3f : 2f);
            Draw.color(node.selected ? Color.white : node.color.cpy().mul(1.3f));
            Lines.rect(screenX, screenY, node.width, node.height);
            
            Fonts.outline.setColor(Color.white);
            float textScale = isMobile ? 0.6f : 0.5f;
            Fonts.outline.getData().setScale(textScale);
            Fonts.outline.draw(node.name, screenX + 10f, screenY + node.height/2f + 5f);
            
            String typeLabel = "[" + node.type.name() + "]";
            Fonts.outline.getData().setScale(isMobile ? 0.45f : 0.35f);
            Fonts.outline.setColor(Color.lightGray);
            Fonts.outline.draw(typeLabel, screenX + 10f, screenY + 15f);
            
            Fonts.outline.getData().setScale(1f);
            Draw.reset();
        }
    }

    void drawConnections() {
        Lines.stroke(isMobile ? 4f : 3f);
        
        for (Node node : editor.getNodes()) {
            for (Node target : node.outputs) {
                float x1 = node.x + node.width / 2f + panOffset.x;
                float y1 = node.y + node.height / 2f + panOffset.y;
                float x2 = target.x + target.width / 2f + panOffset.x;
                float y2 = target.y + target.height / 2f + panOffset.y;
                
                drawConnectionLine(x1, y1, x2, y2, node.color);
            }
        }
        
        Draw.reset();
    }

    void drawConnectionLine(float x1, float y1, float x2, float y2, Color color) {
        Draw.color(color, 0.8f);
        
        float midX = (x1 + x2) / 2f;
        
        Lines.line(x1, y1, midX, y1);
        Lines.line(midX, y1, midX, y2);
        Lines.line(midX, y2, x2, y2);
        
        float dotSize = isMobile ? 7f : 5f;
        Fill.circle(x2, y2, dotSize);
    }
    
    void drawModeIndicator() {
        if (!isMobile) return;
        
        String modeText = "Mode: " + mode.name();
        Color modeColor = Color.white;
        
        switch (mode) {
            case MOVE: modeColor = Color.sky; break;
            case CONNECT: modeColor = Color.lime; break;
            case DELETE: modeColor = Color.scarlet; break;
        }
        
        Fonts.outline.getData().setScale(0.5f);
        Fonts.outline.setColor(modeColor);
        Fonts.outline.draw(modeText, 10f, height - 10f);
        Fonts.outline.getData().setScale(1f);
        Draw.reset();
    }

    public void rebuild() {
    }
}