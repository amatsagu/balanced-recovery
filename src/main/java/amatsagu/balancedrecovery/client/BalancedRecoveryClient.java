package amatsagu.balancedrecovery.client;

import amatsagu.balancedrecovery.client.event.RenderFoodHealingEvent;
import amatsagu.balancedrecovery.client.event.ResetValuesEvent;
import amatsagu.balancedrecovery.client.payload.SyncFoodHealingPayload;
import amatsagu.balancedrecovery.client.payload.SyncNaturalHealthRegenerationPayload;
import amatsagu.balancedrecovery.client.payload.SyncUniqueIngredientsPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class BalancedRecoveryClient implements ClientModInitializer {
	public static boolean naturalHealthRegeneration = true;

	@Override
	public void onInitializeClient() {
		initPayloads();
		initEvents();
	}

	private void initPayloads() {
		ClientPlayNetworking.registerGlobalReceiver(SyncNaturalHealthRegenerationPayload.TYPE, new SyncNaturalHealthRegenerationPayload.Receiver());
		ClientPlayNetworking.registerGlobalReceiver(SyncUniqueIngredientsPayload.TYPE, new SyncUniqueIngredientsPayload.Receiver());
		ClientPlayNetworking.registerGlobalReceiver(SyncFoodHealingPayload.TYPE, new SyncFoodHealingPayload.Receiver());
	}

	private void initEvents() {
		RenderFoodHealingEvent.init();
		ResetValuesEvent.init();
	}
}
