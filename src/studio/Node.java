package studio;

import arc.graphics.Color;
import arc.struct.Seq;

public class Node {
    public String name;
    public NodeType type;
    public Color color;
    public float x, y;
    public float width = 150f;
    public float height = 80f;
    
    public Seq<Node> outputs = new Seq<>();
    public Seq<Node> inputs = new Seq<>();
    
    public Object value;
    public boolean selected = false;

    public Node(String name, NodeType type, Color color) {
        this.name = name;
        this.type = type;
        this.color = color;
    }

    public void connectTo(Node other) {
        if (!outputs.contains(other)) {
            outputs.add(other);
            other.inputs.add(this);
        }
    }

    public void disconnect(Node other) {
        outputs.remove(other);
        other.inputs.remove(this);
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}

enum NodeType {
    EVENT,
    ACTION,
    CONDITION,
    VALUE
}