package amatsagu.balancedrecovery.common.event;

import amatsagu.balancedrecovery.client.payload.SyncNaturalHealthRegenerationPayload;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.gamerules.GameRules;

public class SyncValuesEvent implements ServerPlayConnectionEvents.Join {
	public static void init() {
		ServerPlayConnectionEvents.JOIN.register(new SyncValuesEvent());
	}

	@Override
	public void onPlayReady(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
		if (!listener.getPlayer().level().getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION)) {
			SyncNaturalHealthRegenerationPayload.send(listener.getPlayer(), false);
		}
	}
}
