package amatsagu.balancedrecovery.client.event;

import amatsagu.balancedrecovery.client.BalancedRecoveryClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class ResetValuesEvent implements ClientPlayConnectionEvents.Disconnect {
	public static void init() {
		ClientPlayConnectionEvents.DISCONNECT.register(new ResetValuesEvent());
	}

	@Override
	public void onPlayDisconnect(ClientPacketListener listener, Minecraft client) {
		BalancedRecoveryClient.naturalHealthRegeneration = true;
	}
}
