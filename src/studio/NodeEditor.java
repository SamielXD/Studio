package studio;

import arc.graphics.Color;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class NodeEditor extends BaseDialog {

    private NodeCanvas canvas;
    private Seq<Node> nodes = new Seq<>();
    private Table toolbox;
    private Table modeBar;
    
    public CanvasMode currentMode = CanvasMode.MOVE;

    public NodeEditor() {
        super("Studio - Node Editor");
        
        setup();
    }

    void setup() {
        canvas = new NodeCanvas(this);
        
        cont.clear();
        
        boolean isMobile = arc.Core.app.isMobile();
        
        if (!isMobile) {
            cont.table(t -> {
                t.left();
                buildToolbox(t);
            }).width(200f).growY().left();
        }
        
        cont.table(t -> {
            t.background(Styles.black6);
            
            if (isMobile) {
                buildModeBar(t);
            }
            
            t.row();
            t.add(canvas).grow();
        }).grow();
        
        if (isMobile) {
            buttons.button("Add Node", Icon.add, () -> {
                showMobileToolbox();
            }).size(120f, 50f);
        }
        
        buttons.button("Clear", Icon.trash, () -> {
            clearAllNodes();
        }).size(120f, 50f);
        
        buttons.button("Run", Icon.play, () -> {
            runScript();
        }).size(120f, 50f);
        
        buttons.button("Close", Icon.cancel, this::hide).size(120f, 50f);
    }

    void buildModeBar(Table t) {
        t.table(bar -> {
            bar.defaults().size(80f, 50f).pad(2f);
            
            bar.button("Move", Icon.move, Styles.flatTogglet, () -> {
                currentMode = CanvasMode.MOVE;
                updateModeButtons();
            }).checked(b -> currentMode == CanvasMode.MOVE);
            
            bar.button("Connect", Icon.link, Styles.flatTogglet, () -> {
                currentMode = CanvasMode.CONNECT;
                updateModeButtons();
            }).checked(b -> currentMode == CanvasMode.CONNECT);
            
            bar.button("Delete", Icon.cancel, Styles.flatTogglet, () -> {
                currentMode = CanvasMode.DELETE;
                updateModeButtons();
            }).checked(b -> currentMode == CanvasMode.DELETE);
            
        }).fillX().height(60f).row();
    }
    
    void updateModeButtons() {
        canvas.setMode(currentMode);
    }

    void buildToolbox(Table t) {
        t.add("[accent]Toolbox").row();
        t.image().color(Color.accent).fillX().height(3f).pad(5f).row();
        
        t.add("[lightgray]Events").left().padTop(10f).row();
        t.button("Wave Start", Styles.flatToggleMenut, () -> {
            addNode(new Node("Wave Start", NodeType.EVENT, Color.sky));
        }).growX().height(40f).left().row();
        
        t.button("Building Built", Styles.flatToggleMenut, () -> {
            addNode(new Node("Building Built", NodeType.EVENT, Color.sky));
        }).growX().height(40f).left().row();
        
        t.add("[lightgray]Actions").left().padTop(10f).row();
        t.button("Spawn Unit", Styles.flatToggleMenut, () -> {
            addNode(new Node("Spawn Unit", NodeType.ACTION, Color.green));
        }).growX().height(40f).left().row();
        
        t.button("Show Message", Styles.flatToggleMenut, () -> {
            addNode(new Node("Show Message", NodeType.ACTION, Color.green));
        }).growX().height(40f).left().row();
        
        t.button("Set Variable", Styles.flatToggleMenut, () -> {
            addNode(new Node("Set Variable", NodeType.ACTION, Color.green));
        }).growX().height(40f).left().row();
        
        t.add("[lightgray]Conditions").left().padTop(10f).row();
        t.button("If", Styles.flatToggleMenut, () -> {
            addNode(new Node("If", NodeType.CONDITION, Color.orange));
        }).growX().height(40f).left().row();
        
        t.button("Compare", Styles.flatToggleMenut, () -> {
            addNode(new Node("Compare", NodeType.CONDITION, Color.orange));
        }).growX().height(40f).left().row();
        
        t.add("[lightgray]Values").left().padTop(10f).row();
        t.button("Number", Styles.flatToggleMenut, () -> {
            addNode(new Node("Number", NodeType.VALUE, Color.violet));
        }).growX().height(40f).left().row();
        
        t.button("Text", Styles.flatToggleMenut, () -> {
            addNode(new Node("Text", NodeType.VALUE, Color.violet));
        }).growX().height(40f).left().row();
        
        t.button("Variable", Styles.flatToggleMenut, () -> {
            addNode(new Node("Variable", NodeType.VALUE, Color.violet));
        }).growX().height(40f).left().row();
    }
    
    void showMobileToolbox() {
        BaseDialog toolboxDialog = new BaseDialog("Add Node");
        
        toolboxDialog.cont.pane(p -> {
            p.defaults().size(250f, 60f).pad(5f);
            
            p.add("[accent]Events").colspan(1).left().row();
            p.button("Wave Start", () -> {
                addNode(new Node("Wave Start", NodeType.EVENT, Color.sky));
                toolboxDialog.hide();
            }).row();
            
            p.button("Building Built", () -> {
                addNode(new Node("Building Built", NodeType.EVENT, Color.sky));
                toolboxDialog.hide();
            }).row();
            
            p.add("[accent]Actions").colspan(1).left().padTop(10f).row();
            p.button("Spawn Unit", () -> {
                addNode(new Node("Spawn Unit", NodeType.ACTION, Color.green));
                toolboxDialog.hide();
            }).row();
            
            p.button("Show Message", () -> {
                addNode(new Node("Show Message", NodeType.ACTION, Color.green));
                toolboxDialog.hide();
            }).row();
            
            p.button("Set Variable", () -> {
                addNode(new Node("Set Variable", NodeType.ACTION, Color.green));
                toolboxDialog.hide();
            }).row();
            
            p.add("[accent]Conditions").colspan(1).left().padTop(10f).row();
            p.button("If", () -> {
                addNode(new Node("If", NodeType.CONDITION, Color.orange));
                toolboxDialog.hide();
            }).row();
            
            p.button("Compare", () -> {
                addNode(new Node("Compare", NodeType.CONDITION, Color.orange));
                toolboxDialog.hide();
            }).row();
            
            p.add("[accent]Values").colspan(1).left().padTop(10f).row();
            p.button("Number", () -> {
                addNode(new Node("Number", NodeType.VALUE, Color.violet));
                toolboxDialog.hide();
            }).row();
            
            p.button("Text", () -> {
                addNode(new Node("Text", NodeType.VALUE, Color.violet));
                toolboxDialog.hide();
            }).row();
            
            p.button("Variable", () -> {
                addNode(new Node("Variable", NodeType.VALUE, Color.violet));
                toolboxDialog.hide();
            }).row();
            
        }).grow();
        
        toolboxDialog.addCloseButton();
        toolboxDialog.show();
    }

    public void addNode(Node node) {
        node.x = 400f;
        node.y = 300f;
        nodes.add(node);
        canvas.rebuild();
    }

    public void removeNode(Node node) {
        nodes.remove(node);
        canvas.rebuild();
    }

    void clearAllNodes() {
        nodes.clear();
        canvas.rebuild();
    }

    void runScript() {
        if (nodes.isEmpty()) {
            arc.Core.app.post(() -> {
                mindustry.Vars.ui.showInfo("[scarlet]No nodes to run!");
            });
            return;
        }
        
        arc.Core.app.post(() -> {
            mindustry.Vars.ui.showInfo("[lime]Script executed!\n[lightgray](" + nodes.size + " nodes)");
        });
    }

    public Seq<Node> getNodes() {
        return nodes;
    }
}

enum CanvasMode {
    MOVE,
    CONNECT,
    DELETE
}