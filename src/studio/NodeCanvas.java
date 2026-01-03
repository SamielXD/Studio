package studio;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class NodeCanvas extends Element {
    public Seq<Node> nodes = new Seq<>();
    public Vec2 offset = new Vec2(0, 0);
    public float zoom = 0.5f;

    public String mode = "move";

    private Node dragNode = null;
    private Vec2 dragStart = new Vec2();
    private Vec2 panStart = new Vec2();
    private boolean panning = false;

    private Node connectStart = null;

    public Runnable onNodeEdit;
    public Node selectedNode = null;

    private static final float HIT_BOX_EXPAND = 50f;

    public NodeCanvas() {
        setFillParent(true);

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                Vec2 worldPos = screenToWorld(x, y);

                if(Core.input.keyDown(KeyCode.mouseRight) || pointer == 1) {
                    panning = true;
                    panStart.set(x, y);
                    return true;
                }

                if(mode.equals("move")) {
                    for(Node node : nodes) {
                        if(isInHitBox(worldPos.x, worldPos.y, node)) {
                            dragNode = node;
                            dragStart.set(worldPos.x - node.x, worldPos.y - node.y);
                            return true;
                        }
                    }
                }

                if(mode.equals("edit")) {
                    for(Node node : nodes) {
                        if(isInHitBox(worldPos.x, worldPos.y, node)) {
                            selectedNode = node;
                            if(onNodeEdit != null) {
                                onNodeEdit.run();
                            }
                            return true;
                        }
                    }
                }

                if(mode.equals("connect")) {
                    for(Node node : nodes) {
                        if(isInHitBox(worldPos.x, worldPos.y, node)) {
                            if(connectStart == null) {
                                connectStart = node;
                            } else {
                                if(connectStart != node && !connectStart.connections.contains(node)) {
                                    connectStart.connections.add(node);
                                }
                                connectStart = null;
                            }
                            return true;
                        }
                    }
                    connectStart = null;
                }

                if(mode.equals("delete")) {
                    for(Node node : nodes) {
                        if(isInHitBox(worldPos.x, worldPos.y, node)) {
                            nodes.remove(node);
                            for(Node n : nodes) {
                                n.connections.remove(node);
                            }
                            return true;
                        }
                    }
                }

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(panning) {
                    offset.x += (x - panStart.x) / zoom;
                    offset.y += (y - panStart.y) / zoom;
                    panStart.set(x, y);
                    return;
                }

                if(dragNode != null && mode.equals("move")) {
                    Vec2 worldPos = screenToWorld(x, y);
                    dragNode.x = worldPos.x - dragStart.x;
                    dragNode.y = worldPos.y - dragStart.y;
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                dragNode = null;
                panning = false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                Vec2 worldPosBefore = screenToWorld(x, y);

                zoom = arc.math.Mathf.clamp(zoom - amountY * 0.15f, 0.2f, 3f);

                Vec2 worldPosAfter = screenToWorld(x, y);
                offset.add(worldPosBefore).sub(worldPosAfter);

                return true;
            }
        });
    }

    private boolean isInHitBox(float worldX, float worldY, Node node) {
        return worldX >= node.x - HIT_BOX_EXPAND && 
               worldX <= node.x + node.width + HIT_BOX_EXPAND &&
               worldY >= node.y - HIT_BOX_EXPAND && 
               worldY <= node.y + node.height + HIT_BOX_EXPAND;
    }

    public Vec2 screenToWorld(float x, float y) {
        return new Vec2(
            (x - width/2f) / zoom - offset.x,
            (y - height/2f) / zoom - offset.y
        );
    }

    public Vec2 worldToScreen(float x, float y) {
        return new Vec2(
            (x + offset.x) * zoom + width/2f,
            (y + offset.y) * zoom + height/2f
        );
    }

    public void addNode(String type, String label, Color color) {
        Node node = new Node(type, label, -offset.x, -offset.y, color);
        node.width = 400f;
        node.height = 200f;
        nodes.add(node);
    }

    @Override
    public void draw() {
        validate();

        Draw.color(0.15f, 0.15f, 0.2f, 1f);
        Fill.rect(x + width/2f, y + height/2f, width, height);

        Lines.stroke(2f);
        Draw.color(0.25f, 0.25f, 0.3f, 1f);
        for(float gx = -2000f; gx < 2000f; gx += 50f) {
            Vec2 start = worldToScreen(gx, -2000f);
            if(start.x >= x && start.x <= x + width) {
                Lines.line(start.x, y, start.x, y + height);
            }
        }
        for(float gy = -2000f; gy < 2000f; gy += 50f) {
            Vec2 start = worldToScreen(-2000f, gy);
            if(start.y >= y && start.y <= y + height) {
                Lines.line(x, start.y, x + width, start.y);
            }
        }

        for(Node node : nodes) {
            for(Node target : node.connections) {
                Vec2 start = worldToScreen(node.getOutputPoint().x, node.getOutputPoint().y);
                Vec2 end = worldToScreen(target.getInputPoint().x, target.getInputPoint().y);

                Draw.color(Color.white);
                Lines.stroke(6f);
                Lines.line(start.x, start.y, end.x, end.y);
            }
        }

        for(Node node : nodes) {
            Vec2 screenPos = worldToScreen(node.x, node.y);
            float screenWidth = node.width * zoom;
            float screenHeight = node.height * zoom;

            Draw.color(node.color.r * 0.3f, node.color.g * 0.3f, node.color.b * 0.3f, 1f);
            Fill.rect(screenPos.x, screenPos.y, screenWidth, screenHeight);

            Draw.color(node.color);
            Lines.stroke(5f);
            Lines.rect(screenPos.x, screenPos.y, screenWidth, screenHeight);

            Draw.color(node.color.r * 0.5f, node.color.g * 0.5f, node.color.b * 0.5f, 1f);
            Fill.rect(screenPos.x, screenPos.y + screenHeight - 50f * zoom, screenWidth, 50f * zoom);

            Draw.color(Color.white);
            Fonts.outline.getData().setScale(1.0f * zoom);
            Fonts.outline.draw(node.label, screenPos.x + 20f * zoom, screenPos.y + screenHeight - 15f * zoom);

            if(!node.value.isEmpty()) {
                Draw.color(Color.lightGray);
                Fonts.outline.getData().setScale(0.8f * zoom);
                String displayValue = node.value.length() > 20 ? node.value.substring(0, 20) + "..." : node.value;
                Fonts.outline.draw("Value: " + displayValue, screenPos.x + 20f * zoom, screenPos.y + screenHeight/2f);
            }

            Fonts.outline.getData().setScale(1f);

            Vec2 inputScreen = worldToScreen(node.getInputPoint().x, node.getInputPoint().y);
            Draw.color(Color.green);
            Fill.circle(inputScreen.x, inputScreen.y, 15f);
            Draw.color(Color.darkGray);
            Lines.stroke(3f);
            Lines.circle(inputScreen.x, inputScreen.y, 15f);

            Vec2 outputScreen = worldToScreen(node.getOutputPoint().x, node.getOutputPoint().y);
            Draw.color(Color.red);
            Fill.circle(outputScreen.x, outputScreen.y, 15f);
            Draw.color(Color.darkGray);
            Lines.stroke(3f);
            Lines.circle(outputScreen.x, outputScreen.y, 15f);
        }

        if(connectStart != null) {
            Vec2 start = worldToScreen(connectStart.getOutputPoint().x, connectStart.getOutputPoint().y);
            Draw.color(Color.yellow);
            Lines.stroke(6f);
            Lines.circle(start.x, start.y, 25f);
        }

        Draw.reset();
    }
}