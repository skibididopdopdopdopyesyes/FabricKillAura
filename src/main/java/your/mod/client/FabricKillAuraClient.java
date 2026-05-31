package your.mod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import your.mod.client.modules.combat.KillAura;

public class FabricKillAuraClient implements ClientModInitializer {
    
    public static KillAura killAura;
    
    @Override
    public void onInitializeClient() {
        killAura = new KillAura();
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (killAura.isEnabled() && client.player != null) {
                killAura.onUpdate();
            }
        });
    }
}
