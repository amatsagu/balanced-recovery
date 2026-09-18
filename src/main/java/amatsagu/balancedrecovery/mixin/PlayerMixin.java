package amatsagu.balancedrecovery.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import amatsagu.balancedrecovery.common.component.entity.FoodHealingComponent;
import amatsagu.balancedrecovery.common.component.entity.FoodHealingHolder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements FoodHealingHolder {
	@Shadow
	public abstract boolean isHurt();

	@Unique
	private final FoodHealingComponent balancedrecovery$foodHealing = new FoodHealingComponent((Player) (Object) this);

	protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	@Override
	public FoodHealingComponent balancedrecovery$getFoodHealing() {
		return balancedrecovery$foodHealing;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void balancedrecovery$tickFoodHealing(CallbackInfo ci) {
		balancedrecovery$foodHealing.tick();
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void balancedrecovery$readFoodHealing(ValueInput input, CallbackInfo ci) {
		balancedrecovery$foodHealing.readData(input);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void balancedrecovery$writeFoodHealing(ValueOutput output, CallbackInfo ci) {
		balancedrecovery$foodHealing.writeData(output);
	}

	@ModifyExpressionValue(method = "canEat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;needsFood()Z"))
	private boolean balancedrecovery$treatAsHealth(boolean original) {
		return isHurt() && Mth.ceil(getHealth()) < getMaxHealth() && balancedrecovery$foodHealing.canEat();
	}
}
