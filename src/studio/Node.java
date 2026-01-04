package studio;

import arc.graphics.*;
import arc.math.geom.*;
import arc.struct.*;

public class Node {
    public String id;
    public String type;
    public String label;
    public float x, y;
    public float width = 400f;
    public float height = 200f;
    public String value = "";
    public Color color = Color.gray;

    public Seq<Node> connections = new Seq<>();
    public Seq<NodeInput> inputs = new Seq<>();

    public Node() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public Node(String type, String label, float x, float y, Color color) {
        this();
        this.type = type;
        this.label = label;
        this.x = x;
        this.y = y;
        this.color = color;
        setupInputs();
    }

    public void setupInputs() {
        inputs.clear();

        if(label.equals("Message")) {
            inputs.add(new NodeInput("Text", "Hello!"));
        }
        else if(label.equals("Spawn Unit")) {
            inputs.add(new NodeInput("Unit Type", "dagger"));
            inputs.add(new NodeInput("Amount", "1"));
            inputs.add(new NodeInput("Spawn Location", "At Player"));
            inputs.add(new NodeInput("X Coordinate", "0"));
            inputs.add(new NodeInput("Y Coordinate", "0"));
        }
        else if(label.equals("Set Block")) {
            inputs.add(new NodeInput("X", "10"));
            inputs.add(new NodeInput("Y", "10"));
            inputs.add(new NodeInput("Block", "copper-wall"));
        }
        else if(label.equals("Create Mod Folder")) {
            inputs.add(new NodeInput("Folder Name", "mymod"));
        }
        else if(label.equals("Create Folder")) {
            inputs.add(new NodeInput("Folder Name", "content"));
        }
        else if(label.equals("Create Block File")) {
            inputs.add(new NodeInput("Block Name", "my-wall"));
            inputs.add(new NodeInput("Type", "Wall"));
            inputs.add(new NodeInput("Health", "500"));
            inputs.add(new NodeInput("Size", "1"));
        }
        else if(label.equals("Create Unit File")) {
            inputs.add(new NodeInput("Unit Name", "my-mech"));
            inputs.add(new NodeInput("Type", "mech"));
            inputs.add(new NodeInput("Health", "200"));
            inputs.add(new NodeInput("Speed", "0.5"));
        }
        else if(label.equals("Create Item File")) {
            inputs.add(new NodeInput("Item Name", "my-resource"));
            inputs.add(new NodeInput("Color", "ff0000"));
            inputs.add(new NodeInput("Cost", "1.0"));
        }
        else if(label.equals("Add Sprite")) {
            inputs.add(new NodeInput("Sprite Name", "my-sprite"));
            inputs.add(new NodeInput("Path", "sprites/blocks/my-sprite.png"));
        }
        else if(label.equals("Create mod.hjson")) {
            inputs.add(new NodeInput("Mod Name", "mymod"));
            inputs.add(new NodeInput("Display Name", "My Mod"));
            inputs.add(new NodeInput("Author", "YourName"));
        }
        else if(label.equals("Wait")) {
            inputs.add(new NodeInput("Seconds", "1"));
        }
        else if(label.equals("If")) {
            inputs.add(new NodeInput("Condition", "true"));
        }
        else if(label.equals("Set Variable")) {
            inputs.add(new NodeInput("Variable Name", "myVar"));
            inputs.add(new NodeInput("Value", "0"));
        }
        else if(label.equals("Get Variable")) {
            inputs.add(new NodeInput("Variable Name", "myVar"));
        }
        else if(label.equals("Loop")) {
            inputs.add(new NodeInput("Count", "10"));
        }
        else if(label.equals("On Start")) {
            value = "On Start";
        }
        else if(label.equals("On Wave")) {
            value = "On Wave";
        }
        else if(label.equals("On Build")) {
            value = "On Build";
        }
    }

    public Vec2 getInputPoint() {
        return new Vec2(x, y + height / 2);
    }

    public Vec2 getOutputPoint() {
        return new Vec2(x + width, y + height / 2);
    }

    public static class NodeInput {
        public String label;
        public String value;

        public NodeInput(String label, String defaultValue) {
            this.label = label;
            this.value = defaultValue;
        }
    }
}