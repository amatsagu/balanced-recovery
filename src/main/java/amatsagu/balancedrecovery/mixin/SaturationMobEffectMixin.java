package amatsagu.balancedrecovery.mixin;

import amatsagu.balancedrecovery.common.init.BalancedRecoveryEntityComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.SaturationMobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SaturationMobEffect.class)
public class SaturationMobEffectMixin {
	@Inject(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
	private void balancedrecovery$saturationEffect(ServerLevel level, LivingEntity mob, int amplification, CallbackInfoReturnable<Boolean> cir) {
		BalancedRecoveryEntityComponents.FOOD_HEALING.get(mob).setFromSaturation(true);
	}
}
