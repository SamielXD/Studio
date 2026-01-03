package studio;

import arc.*;
import arc.files.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.ui.*;

public class StudioMod extends Mod {
    public static Seq<Script> loadedScripts = new Seq<>();
    public static Fi scriptsFolder;

    private NodeEditor nodeEditor;

    public StudioMod() {
        Log.info("Studio - Visual Scripting System loading...");
    }

    @Override
    public void init() {
        Log.info("Studio initializing...");

        scriptsFolder = Core.files.local("mods/studio-scripts/");
        scriptsFolder.mkdirs();

        nodeEditor = new NodeEditor();

        Events.on(ClientLoadEvent.class, e -> {
            setupUI();
            setupSettings();
            loadAllScripts();
        });

        Events.on(WorldLoadEvent.class, e -> {
            Log.info("WorldLoadEvent - executing 'On Start' scripts");
            executeEventScripts("On Start");
        });

        Events.on(WaveEvent.class, e -> {
            Log.info("WaveEvent - executing 'On Wave' scripts");
            executeEventScripts("On Wave");
        });

        Events.on(UnitCreateEvent.class, e -> {
            Log.info("UnitCreateEvent - executing 'On Unit Spawn' scripts");
            executeEventScripts("On Unit Spawn");
        });

        Log.info("Studio loaded successfully!");
    }

    void setupUI() {
        Vars.ui.menufrag.addButton("Studio", Icon.edit, () -> {
            nodeEditor.show();
        });
    }

    void setupSettings() {
        Vars.ui.settings.addCategory("Studio", Icon.edit, table -> {
            
            table.add("[accent]═══ Display Settings ═══").padTop(10).row();
            
            // Node Label Size
            table.table(t -> {
                t.left();
                t.add("Node Label Size: ").left();
                arc.scene.ui.Slider labelSlider = new arc.scene.ui.Slider(0.8f, 2.0f, 0.1f, false);
                labelSlider.setValue(Core.settings.getFloat("studio-label-scale", 1.2f));
                t.add(labelSlider).width(200f).padLeft(10f);
                Label labelValue = new Label("" + labelSlider.getValue());
                t.add(labelValue).padLeft(10f);
                
                labelSlider.changed(() -> {
                    float newValue = labelSlider.getValue();
                    Core.settings.put("studio-label-scale", newValue);
                    labelValue.setText(String.format("%.1f", newValue));
                    NodeCanvas.nodeLabelScale = newValue;
                });
            }).left().row();
            
            // Node Value Size
            table.table(t -> {
                t.left();
                t.add("Node Value Size: ").left();
                arc.scene.ui.Slider valueSlider = new arc.scene.ui.Slider(0.6f, 1.5f, 0.1f, false);
                valueSlider.setValue(Core.settings.getFloat("studio-value-scale", 1.0f));
                t.add(valueSlider).width(200f).padLeft(10f);
                Label valueLabel = new Label("" + valueSlider.getValue());
                t.add(valueLabel).padLeft(10f);
                
                valueSlider.changed(() -> {
                    float newValue = valueSlider.getValue();
                    Core.settings.put("studio-value-scale", newValue);
                    valueLabel.setText(String.format("%.1f", newValue));
                    NodeCanvas.nodeValueScale = newValue;
                });
            }).left().row();
            
            table.add("[accent]═══ Actions ═══").padTop(20).row();
            
            table.button("[cyan]Open Studio Editor", Icon.edit, () -> {
                nodeEditor.show();
            }).size(300f, 60f).padTop(10).row();
            
            table.button("[lime]Reload All Scripts", Icon.refresh, () -> {
                loadAllScripts();
                Vars.ui.showInfoFade("[lime]Reloaded " + loadedScripts.size + " scripts!");
            }).size(300f, 60f).padTop(5).row();
            
            table.add("[accent]═══ Script Info ═══").padTop(20).row();
            
            Label scriptCountLabel = new Label("Loading...");
            scriptCountLabel.setFontScale(1.2f);
            table.add(scriptCountLabel).padTop(10).row();
            
            Timer.schedule(() -> {
                int count = loadedScripts.size;
                int nodeCount = 0;
                for(Script s : loadedScripts) {
                    nodeCount += s.nodes.size;
                }
                scriptCountLabel.setText("[cyan]" + count + " scripts loaded\n[lightgray]" + nodeCount + " total nodes");
            }, 0.5f);
            
            table.add("[accent]═══ Help ═══").padTop(20).row();
            
            table.button("[royal]Open Tutorial", Icon.book, () -> {
                Vars.ui.showInfoText("Studio Tutorial", 
                    "[cyan]Getting Started:[]\n\n" +
                    "1. Click [lime]Studio[] in main menu\n" +
                    "2. Click [lime]Add[] to create nodes\n" +
                    "3. Use [lime]Link[] to connect nodes\n" +
                    "4. Use [lime]Edit[] to change values\n" +
                    "5. Click [lime]Save[] to save your script\n\n" +
                    "[cyan]Node Types:[]\n" +
                    "[green]Events[] - When things happen\n" +
                    "[blue]Actions[] - Do something\n" +
                    "[orange]Conditions[] - Check/wait\n\n" +
                    "[cyan]Example:[]\n" +
                    "On Start → Spawn Unit (dagger)\n\n" +
                    "[scarlet]Tips:[]\n" +
                    "- Use [lime]Z-/Z+[] to zoom\n" +
                    "- Two-finger drag to pan\n" +
                    "- Scroll buttons left/right\n" +
                    "- Save before closing!"
                );
            }).size(300f, 60f).padTop(10).row();
            
            table.button("[scarlet]Delete All Scripts", Icon.trash, () -> {
                Vars.ui.showConfirm("Delete All Scripts?", 
                    "This will delete all " + loadedScripts.size + " saved scripts!\nThis cannot be undone!", 
                    () -> {
                        for(Fi file : scriptsFolder.list()) {
                            if(file.extension().equals("json")) {
                                file.delete();
                            }
                        }
                        loadedScripts.clear();
                        Vars.ui.showInfoFade("[scarlet]All scripts deleted!");
                    }
                );
            }).size(300f, 60f).padTop(5).row();
        });
    }

    public static void loadAllScripts() {
        loadedScripts.clear();

        for(Fi file : scriptsFolder.list()) {
            if(file.extension().equals("json")) {
                try {
                    String json = file.readString();
                    Json jsonParser = new Json();
                    jsonParser.setIgnoreUnknownFields(true);
                    Seq<NodeEditor.NodeData> nodeDataList = jsonParser.fromJson(Seq.class, NodeEditor.NodeData.class, json);

                    Script script = new Script();
                    script.fileName = file.name();
                    script.name = file.nameWithoutExtension();
                    script.enabled = true;
                    script.nodes = new Seq<>();

                    Seq<Node> loadedNodes = new Seq<>();

                    for(NodeEditor.NodeData data : nodeDataList) {
                        Node node = new Node();
                        node.id = data.id;
                        node.type = data.type;
                        node.label = data.label;
                        node.x = data.x;
                        node.y = data.y;
                        node.value = data.value != null ? data.value : "";
                        node.setupInputs();
                        
                        if(data.inputValues != null && data.inputValues.size > 0) {
                            for(int i = 0; i < Math.min(node.inputs.size, data.inputValues.size); i++) {
                                node.inputs.get(i).value = data.inputValues.get(i);
                            }
                        }
                        
                        loadedNodes.add(node);
                    }

                    for(int i = 0; i < nodeDataList.size; i++) {
                        NodeEditor.NodeData data = nodeDataList.get(i);
                        Node node = loadedNodes.get(i);

                        if(data.connectionIds != null) {
                            for(String connId : data.connectionIds) {
                                Node target = loadedNodes.find(n -> n.id.equals(connId));
                                if(target != null) {
                                    node.connections.add(target);
                                }
                            }
                        }
                    }

                    script.nodes = loadedNodes;
                    loadedScripts.add(script);
                    Log.info("Loaded script: " + file.name() + " (" + script.nodes.size + " nodes)");
                } catch(Exception ex) {
                    Log.err("Failed to load script: " + file.name(), ex);
                }
            }
        }
    }

    public static void executeEventScripts(String eventLabel) {
        for(Script script : loadedScripts) {
            if(!script.enabled) continue;

            for(Node node : script.nodes) {
                if(node.type.equals("event") && node.label.equalsIgnoreCase(eventLabel)) {
                    Log.info("Executing: " + node.label + " in " + script.name);
                    executeNodeChain(node, script);
                }
            }
        }
    }

    public static void executeNodeChain(Node node, Script script) {
        executeNode(node, script);

        for(Node conn : node.connections) {
            executeNode(conn, script);
        }
    }

    public static void executeNode(Node node, Script script) {
        Log.info("Node: " + node.label + " (type: " + node.type + ")");

        switch(node.type) {
            case "action":
                executeAction(node);
                for(Node conn : node.connections) {
                    executeNode(conn, script);
                }
                break;
                
            case "condition":
                if(evaluateCondition(node)) {
                    for(Node conn : node.connections) {
                        executeNode(conn, script);
                    }
                }
                break;
                
            case "event":
                for(Node conn : node.connections) {
                    executeNode(conn, script);
                }
                break;
        }
    }

    public static void executeAction(Node node) {
        String label = node.label.toLowerCase();
        Log.info("Action: " + label + " | Value: " + node.value);

        if(label.contains("message")) {
            String message = node.value.isEmpty() ? "Hello from Studio!" : node.value;
            Vars.ui.showInfoFade(message);
        }
        else if(label.contains("create mod folder")) {
            try {
                String[] parts = node.value.split("\\|");
                String folderName = parts.length > 0 ? parts[0].trim() : "mymod";
                String displayName = parts.length > 1 ? parts[1].trim() : "My Mod";
                String author = parts.length > 2 ? parts[2].trim() : "Unknown";
                String description = parts.length > 3 ? parts[3].trim() : "A custom mod";

                Fi modFolder = Core.files.local("mods/" + folderName);
                modFolder.mkdirs();

                String hjson = 
                    "name: \"" + folderName + "\"\n" +
                    "displayName: \"" + displayName + "\"\n" +
                    "author: \"" + author + "\"\n" +
                    "description: \"" + description + "\"\n" +
                    "version: \"1.0\"\n" +
                    "minGameVersion: \"146\"\n" +
                    "hidden: false\n";

                modFolder.child("mod.hjson").writeString(hjson);

                Log.info("Created mod folder: " + folderName);
                Vars.ui.showInfoFade("Created mod: " + displayName + "\nFolder: mods/" + folderName);
            } catch(Exception e) {
                Log.err("Create mod failed", e);
                Vars.ui.showInfoFade("Failed to create mod: " + e.getMessage());
            }
        }
        else if(label.contains("spawn unit")) {
            try {
                String[] parts = node.value.split("\\|");
                String unitName = parts.length > 0 ? parts[0].toLowerCase().trim() : "dagger";
                String location = parts.length > 1 ? parts[1].trim() : "core";
                float customX = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 0f;
                float customY = parts.length > 3 ? Float.parseFloat(parts[3].trim()) : 0f;
                int amount = parts.length > 4 ? Integer.parseInt(parts[4].trim()) : 1;

                UnitType type = Vars.content.units().find(u -> u.name.equals(unitName));
                if(type == null) {
                    Log.warn("Unit not found: " + unitName);
                    type = UnitTypes.dagger;
                }

                float spawnX = 0f, spawnY = 0f;
                
                if(location.equals("core")) {
                    if(Vars.player != null && Vars.player.team() != null && Vars.player.team().core() != null) {
                        spawnX = Vars.player.team().core().x;
                        spawnY = Vars.player.team().core().y;
                        Log.info("Spawning at CORE: " + spawnX + ", " + spawnY);
                    } else {
                        spawnX = Vars.world.width() * 4f;
                        spawnY = Vars.world.height() * 4f;
                        Log.warn("No core found, using world center");
                    }
                }
                else if(location.equals("player")) {
                    if(Vars.player != null && Vars.player.unit() != null) {
                        spawnX = Vars.player.x;
                        spawnY = Vars.player.y;
                        Log.info("Spawning at PLAYER: " + spawnX + ", " + spawnY);
                    } else {
                        Log.warn("Player not found, using core");
                        if(Vars.player != null && Vars.player.team() != null && Vars.player.team().core() != null) {
                            spawnX = Vars.player.team().core().x;
                            spawnY = Vars.player.team().core().y;
                        }
                    }
                }
                else if(location.equals("coordinates")) {
                    spawnX = customX * 8f;
                    spawnY = customY * 8f;
                    Log.info("Spawning at COORDINATES: " + spawnX + ", " + spawnY);
                }

                for(int i = 0; i < amount; i++) {
                    float offsetX = (float)(Math.random() * 16f - 8f);
                    float offsetY = (float)(Math.random() * 16f - 8f);
                    
                    Unit unit = type.spawn(Vars.player.team(), spawnX + offsetX, spawnY + offsetY);
                    Log.info("Spawned: " + type.name + " at (" + (spawnX + offsetX) + ", " + (spawnY + offsetY) + ")");
                }
                
                Vars.ui.showInfoFade("Spawned " + amount + "x " + type.name + " at " + location + "!");
                
            } catch(Exception e) {
                Log.err("Spawn failed", e);
                Vars.ui.showInfoFade("Spawn failed: " + e.getMessage());
            }
        }
        else if(label.contains("set block")) {
            try {
                String[] parts = node.value.split(",");
                if(parts.length >= 3) {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    String blockName = parts[2].trim().toLowerCase();

                    Block block = Vars.content.blocks().find(b -> b.name.equals(blockName));
                    if(block != null && Vars.world != null) {
                        Vars.world.tile(x, y).setNet(block, Vars.player.team(), 0);
                        Log.info("Set block at (" + x + ", " + y + ") to " + blockName);
                    }
                }
            } catch(Exception e) {
                Log.err("Set block failed", e);
            }
        }
    }

    public static boolean evaluateCondition(Node node) {
        String label = node.label.toLowerCase();

        if(label.contains("wait")) {
            try {
                float seconds = node.value.isEmpty() ? 1f : Float.parseFloat(node.value);
                Timer.schedule(() -> {
                    for(Node conn : node.connections) {
                        executeNode(conn, null);
                    }
                }, seconds);
                return false;
            } catch(Exception e) {
                return true;
            }
        }

        if(label.contains("if")) {
            return !node.value.isEmpty();
        }

        return true;
    }

    public static class Script {
        public String name = "Untitled";
        public String fileName = "";
        public boolean enabled = true;
        public Seq<Node> nodes = new Seq<>();
    }
}