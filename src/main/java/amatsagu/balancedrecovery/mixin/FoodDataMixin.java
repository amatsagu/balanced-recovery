package amatsagu.balancedrecovery.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import amatsagu.balancedrecovery.common.BalancedRecoveryConfig;
import amatsagu.balancedrecovery.common.component.entity.FoodHealingComponent;
import amatsagu.balancedrecovery.common.init.BalancedRecoveryEntityComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FoodData.class, priority = 1001)
public abstract class FoodDataMixin {
	@Unique
	private ServerPlayer cachedPlayer = null;

	@Shadow
	public abstract boolean needsFood();

	@Shadow
	public abstract void setFoodLevel(int food);

	@Shadow
	public abstract void setSaturation(float saturation);

	@Shadow
	private float exhaustionLevel;

	@Inject(method = "tick", at = @At("HEAD"))
	private void balancedrecovery$forceFullHunger(ServerPlayer player, CallbackInfo ci) {
		cachedPlayer = player;
		if (needsFood()) {
			setFoodLevel(20);
			setSaturation(20);
			exhaustionLevel = 0;
		}
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isHurt()Z"))
	private boolean balancedrecovery$preventNaturalHealing(boolean original) {
		return false;
	}

	@Inject(method = "getFoodLevel", at = @At("HEAD"), cancellable = true)
	private void balancedrecovery$treatAsHealth(CallbackInfoReturnable<Integer> cir) {
		if (cachedPlayer != null) {
			cir.setReturnValue((int) (cachedPlayer.getHealth() / cachedPlayer.getMaxHealth() * 20));
		}
	}

	@Inject(method = "hasEnoughFood", at = @At("HEAD"), cancellable = true)
	private void balancedrecovery$allowSprinting(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

	@Inject(method = "add", at = @At("HEAD"))
	private void balancedrecovery$treatAsHealth(int food, float saturation, CallbackInfo ci) {
		if (cachedPlayer != null) {
			food = Mth.floor(food * BalancedRecoveryConfig.healthGainMultiplier);
			FoodHealingComponent foodHealing = BalancedRecoveryEntityComponents.FOOD_HEALING.get(cachedPlayer);
			foodHealing.startHealing(food, saturation);
			foodHealing.sync();
		}
	}
}
