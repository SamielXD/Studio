package studio;

import arc.Events;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.BaseDialog;

public class StudioMod extends Mod {

    private NodeEditor nodeEditor;

    public StudioMod() {
        Log.info("Studio - Visual Scripting System loading...");
    }

    @Override
    public void init() {
        Log.info("Studio initializing...");

        nodeEditor = new NodeEditor();

        Events.on(EventType.ClientLoadEvent.class, e -> {
            setupUI();
        });

        Log.info("Studio loaded successfully!");
    }

    void setupUI() {
        Vars.ui.menufrag.addButton("Studio", arc.scene.ui.layout.Table::button, () -> {
            nodeEditor.show();
        });
    }
}