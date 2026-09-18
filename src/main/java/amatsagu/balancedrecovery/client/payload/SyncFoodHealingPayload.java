package amatsagu.balancedrecovery.client.payload;

import amatsagu.balancedrecovery.common.BalancedRecovery;
import amatsagu.balancedrecovery.common.component.entity.FoodHealingComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record SyncFoodHealingPayload(int healAmount, int ticksPerHeal, int healTicks, int amountHealed) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SyncFoodHealingPayload> TYPE = new Type<>(BalancedRecovery.id("sync_food_healing"));
	public static final StreamCodec<FriendlyByteBuf, SyncFoodHealingPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SyncFoodHealingPayload::healAmount,
			ByteBufCodecs.VAR_INT, SyncFoodHealingPayload::ticksPerHeal,
			ByteBufCodecs.VAR_INT, SyncFoodHealingPayload::healTicks,
			ByteBufCodecs.VAR_INT, SyncFoodHealingPayload::amountHealed,
			SyncFoodHealingPayload::new
	);

	@Override
	public Type<SyncFoodHealingPayload> type() {
		return TYPE;
	}

	public static void send(ServerPlayer receiver, FoodHealingComponent component) {
		ServerPlayNetworking.send(receiver, new SyncFoodHealingPayload(
				component.getHealAmount(),
				component.getTicksPerHeal(),
				component.getHealTicks(),
				component.getAmountHealed()
		));
	}

	public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<SyncFoodHealingPayload> {
		@Override
		public void receive(SyncFoodHealingPayload payload, ClientPlayNetworking.Context context) {
			FoodHealingComponent component = FoodHealingComponent.get(context.player());
			if (component != null) {
				component.setClientValues(payload.healAmount(), payload.ticksPerHeal(), payload.healTicks(), payload.amountHealed());
			}
		}
	}
}
