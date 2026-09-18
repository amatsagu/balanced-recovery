package amatsagu.balancedrecovery.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import amatsagu.balancedrecovery.common.util.StewHolder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Foods.class)
public class FoodsMixin {
	@ModifyReturnValue(method = "stew", at = @At("RETURN"))
	private static FoodProperties.Builder balancedrecovery$stewHolder(FoodProperties.Builder original) {
		((StewHolder) original).balancedrecovery$setStew(true);
		return original;
	}
}
