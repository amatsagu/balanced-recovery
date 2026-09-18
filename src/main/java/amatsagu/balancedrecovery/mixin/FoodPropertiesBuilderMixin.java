package amatsagu.balancedrecovery.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import amatsagu.balancedrecovery.common.util.StewHolder;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodProperties.Builder.class)
public class FoodPropertiesBuilderMixin implements StewHolder {
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

	@ModifyReturnValue(method = "build", at = @At("RETURN"))
	private FoodProperties balancedrecovery$stewHolder(FoodProperties original) {
		((StewHolder) (Object) original).balancedrecovery$setStew(balancedrecovery$isStew());
		return original;
	}
}
