package amatsagu.balancedrecovery.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import amatsagu.balancedrecovery.common.init.BalancedRecoveryEntityComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	@Shadow
	public abstract boolean isHurt();

	protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	@ModifyExpressionValue(method = "canEat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;needsFood()Z"))
	private boolean balancedrecovery$treatAsHealth(boolean original) {
		return isHurt() && Mth.ceil(getHealth()) < getMaxHealth() && BalancedRecoveryEntityComponents.FOOD_HEALING.get(this).canEat();
	}
}
