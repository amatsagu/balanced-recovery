package amatsagu.balancedrecovery.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import amatsagu.balancedrecovery.common.event.IncreaseSaturationEvent;
import amatsagu.balancedrecovery.common.util.StewHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin implements StewHolder {
	@Unique
	private boolean isStew = false;

	@Override
	public boolean balancedrecovery$isStew() {
		return isStew;
	}

	@Override
	public void balancedrecovery$setStew(boolean stew) {
		isStew = stew;
	}

	@WrapOperation(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
	private void balancedrecovery$modifyFoodProperties(FoodData instance, FoodProperties foodProperties, Operation<Void> original, Level level, LivingEntity entity, ItemStack stack, Consumable consumable) {
		if (entity instanceof Player player) {
			int nutrition = IncreaseSaturationEvent.modifyNutrition(foodProperties.nutrition(), level, player, stack);
			float saturation = IncreaseSaturationEvent.modifySaturation(foodProperties.saturation(), level, player, stack);
			FoodProperties modified = new FoodProperties(nutrition, saturation, foodProperties.canAlwaysEat());
			((StewHolder) (Object) modified).balancedrecovery$setStew(isStew);
			original.call(instance, modified);
		} else {
			original.call(instance, foodProperties);
		}
	}
}
