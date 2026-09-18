package amatsagu.balancedrecovery.common.component.entity;

import amatsagu.balancedrecovery.client.payload.SyncFoodHealingPayload;
import amatsagu.balancedrecovery.common.BalancedRecovery;
import amatsagu.balancedrecovery.common.BalancedRecoveryConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodHealingComponent {
	private final Player obj;
	private boolean fromSaturation = false;
	private int healAmount = 0, ticksPerHeal = 0;
	private int healTicks = 0;
	private int amountHealed = 0;

	public FoodHealingComponent(Player obj) {
		this.obj = obj;
	}

	public static FoodHealingComponent get(Player player) {
		return ((FoodHealingHolder) player).balancedrecovery$getFoodHealing();
	}

	public void copyFrom(FoodHealingComponent other) {
		this.fromSaturation = other.fromSaturation;
		this.healAmount = other.healAmount;
		this.ticksPerHeal = other.ticksPerHeal;
		this.healTicks = other.healTicks;
		this.amountHealed = other.amountHealed;
	}

	public void setClientValues(int healAmount, int ticksPerHeal, int healTicks, int amountHealed) {
		this.healAmount = healAmount;
		this.ticksPerHeal = ticksPerHeal;
		this.healTicks = healTicks;
		this.amountHealed = amountHealed;
	}

	public void readData(ValueInput input) {
		fromSaturation = input.getBooleanOr("BalancedRecovery_FromSaturation", input.getBooleanOr("FromSaturation", false));
		healAmount = input.getIntOr("BalancedRecovery_HealAmount", input.getIntOr("HealAmount", 0));
		ticksPerHeal = input.getIntOr("BalancedRecovery_TicksPerHeal", input.getIntOr("TicksPerHeal", 0));
		healTicks = input.getIntOr("BalancedRecovery_HealTicks", input.getIntOr("HealTicks", 0));
		amountHealed = input.getIntOr("BalancedRecovery_AmountHealed", input.getIntOr("AmountHealed", 0));
	}

	public void writeData(ValueOutput output) {
		output.putBoolean("BalancedRecovery_FromSaturation", fromSaturation);
		output.putInt("BalancedRecovery_HealAmount", healAmount);
		output.putInt("BalancedRecovery_TicksPerHeal", ticksPerHeal);
		output.putInt("BalancedRecovery_HealTicks", healTicks);
		output.putInt("BalancedRecovery_AmountHealed", amountHealed);
	}

	public void tick() {
		tickFoodHealing();
		tickWarmthSources();
		tickNourishment();
	}

	public void sync() {
		if (obj instanceof ServerPlayer serverPlayer) {
			SyncFoodHealingPayload.send(serverPlayer, this);
		}
	}

	public void setFromSaturation(boolean fromSaturation) {
		this.fromSaturation = fromSaturation;
	}

	public int getHealAmount() {
		return healAmount;
	}

	public int getAmountHealed() {
		return amountHealed;
	}

	public int getHealTicks() {
		return healTicks;
	}

	public int getTicksPerHeal() {
		return ticksPerHeal;
	}

	public int getMaximumHealTicks() {
		return healAmount * ticksPerHeal;
	}

	public boolean canEat() {
		return healAmount == 0;
	}

	public void startHealing(int food, float saturation) {
		if (fromSaturation) {
			fromSaturation = false;
			int duration = obj.getEffect(MobEffects.SATURATION).getDuration();
			if (duration == MobEffectInstance.INFINITE_DURATION) {
				duration = obj.tickCount;
			}
			if (duration % 2 == 0) {
				obj.heal(food);
			}
		} else if (food > 0) {
			healAmount = food;
			ticksPerHeal = getTicksPerHeal(saturation);
			int maxHealTicks = getMaximumHealTicks();
			for (Identifier id : getFoodItemIds()) {
				obj.getCooldowns().addCooldown(id, maxHealTicks);
			}
			for (int i = 0; i < obj.getInventory().getContainerSize(); i++) {
				ItemStack stack = obj.getInventory().getItem(i);
				if (stack.has(DataComponents.FOOD)) {
					obj.getCooldowns().addCooldown(stack, maxHealTicks);
				}
			}
		}
	}

	private static List<Identifier> FOOD_ITEM_IDS = null;

	private static List<Identifier> getFoodItemIds() {
		if (FOOD_ITEM_IDS == null) {
			List<Identifier> list = new ArrayList<>();
			for (Item item : BuiltInRegistries.ITEM) {
				if (item.components().has(DataComponents.FOOD)) {
					list.add(BuiltInRegistries.ITEM.getKey(item));
				}
			}
			FOOD_ITEM_IDS = list;
		}
		return FOOD_ITEM_IDS;
	}

	public static int getTicksPerHeal(float saturation) {
		float base = Math.max(5, Mth.lerp(saturation / 20, 60, 0));
		return Math.max(1, Mth.floor(base * BalancedRecoveryConfig.regenerationTimeMultiplier));
	}

	private void tickFoodHealing() {
		if (healAmount > 0) {
			healTicks++;
			if (healTicks % ticksPerHeal == 0) {
				if (obj.level() instanceof ServerLevel level && !obj.hasEffect(MobEffects.HUNGER) && level.getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION)) {
					obj.heal(1);
				}
				amountHealed++;
			}
			if (healTicks == getMaximumHealTicks()) {
				healAmount = ticksPerHeal = healTicks = amountHealed = 0;
			}
		}
	}

	private void tickWarmthSources() {
		if (BalancedRecoveryConfig.warmthHealing && obj.level() instanceof ServerLevel && (obj.tickCount + obj.getId()) % 80 == 0) {
			if (BalancedRecoveryConfig.warmthBlocks.isEmpty()) {
				return;
			}
			Optional<BlockPos> closestSource = obj.level().findBlocksInBoxByManhattanDistance(obj.blockPosition(), BalancedRecoveryConfig.warmthDetectionRange).filterState(BalancedRecoveryConfig::isWarmthSource).findFirst();
			if (closestSource.isPresent()) {
				obj.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, true, true));
			}
		}
	}

	private void tickNourishment() {
		if (BalancedRecovery.farmersDelightLoaded && obj.level() instanceof ServerLevel && obj.hasEffect(ModEffects.NOURISHMENT)) {
			MobEffectInstance effect = obj.getEffect(ModEffects.NOURISHMENT);
			int duration = effect.getDuration();
			if (duration == MobEffectInstance.INFINITE_DURATION) {
				duration = obj.tickCount;
			}
			if (duration % 200 == 0) {
				obj.heal(effect.getAmplifier() + 1);
			}
		}
	}
}
