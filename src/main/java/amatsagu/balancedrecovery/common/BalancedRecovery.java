package amatsagu.balancedrecovery.common;

import amatsagu.balancedrecovery.client.payload.SyncFoodHealingPayload;
import amatsagu.balancedrecovery.client.payload.SyncNaturalHealthRegenerationPayload;
import amatsagu.balancedrecovery.client.payload.SyncUniqueIngredientsPayload;
import amatsagu.balancedrecovery.common.component.entity.FoodHealingComponent;
import amatsagu.balancedrecovery.common.event.SyncValuesEvent;
import amatsagu.balancedrecovery.common.event.UniqueIngredientsEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalancedRecovery implements ModInitializer {
	public static final String MOD_ID = "balanced_recovery";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final boolean farmersDelightLoaded = FabricLoader.getInstance().isModLoaded("farmersdelight");

	@Override
	public void onInitialize() {
		initPayloads();
		initEvents();
	}

	public static Identifier id(String value) {
		return Identifier.fromNamespaceAndPath(MOD_ID, value);
	}

	private void initPayloads() {
		PayloadTypeRegistry.clientboundPlay().register(SyncNaturalHealthRegenerationPayload.TYPE, SyncNaturalHealthRegenerationPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SyncUniqueIngredientsPayload.TYPE, SyncUniqueIngredientsPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SyncFoodHealingPayload.TYPE, SyncFoodHealingPayload.CODEC);
	}

	private void initEvents() {
		SyncValuesEvent.init();
		UniqueIngredientsEvent.init();
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (alive) {
				FoodHealingComponent.get(newPlayer).copyFrom(FoodHealingComponent.get(oldPlayer));
			}
			
			FoodHealingComponent.get(newPlayer).sync();
		});
	}
}
