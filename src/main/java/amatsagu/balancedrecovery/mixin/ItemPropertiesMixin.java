package amatsagu.balancedrecovery.mixin;

import amatsagu.balancedrecovery.common.BalancedRecoveryConfig;
import amatsagu.balancedrecovery.common.util.StewHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Item.Properties.class)
public class ItemPropertiesMixin {
	@SuppressWarnings("unchecked")
	@ModifyVariable(method = "component", at = @At("HEAD"), argsOnly = true)
	private <T> T balancedrecovery$fasterFluidConsumption(T value, DataComponentType<T> type) {
		if (BalancedRecoveryConfig.fasterFluidConsumption && type == DataComponents.CONSUMABLE) {
			Consumable consumable = (Consumable) value;
			if (consumable.animation() == ItemUseAnimation.DRINK) {
				return (T) new Consumable(consumable.consumeSeconds() / 2, consumable.animation(), consumable.sound(), consumable.hasConsumeParticles(), consumable.onConsumeEffects());
			}
		}
		return value;
	}

	@SuppressWarnings("ConstantValue")
	@ModifyVariable(method = "food(Lnet/minecraft/world/food/FoodProperties;Lnet/minecraft/world/item/component/Consumable;)Lnet/minecraft/world/item/Item$Properties;", at = @At("HEAD"), argsOnly = true)
	private Consumable balancedrecovery$fasterFluidConsumption(Consumable consumable, FoodProperties foodProperties) {
		if (BalancedRecoveryConfig.fasterFluidConsumption && ((StewHolder) (Object) foodProperties).balancedrecovery$isStew()) {
			return new Consumable(consumable.consumeSeconds() / 2, consumable.animation(), consumable.sound(), consumable.hasConsumeParticles(), consumable.onConsumeEffects());
		}
		return consumable;
	}
}
