package amatsagu.balancedrecovery.common.event;

import amatsagu.balancedrecovery.common.BalancedRecoveryConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IncreaseSaturationEvent {
	private static final float MAX_SATURATION = 14.4F;

	public static int modifyNutrition(int nutrition, Level level, Player user, ItemStack stack) {
		int uniqueIngredientBonus = Mth.floor(UniqueIngredientsEvent.getUniqueIngredients(stack.getItem()) / 2F);
		float modifier = BalancedRecoveryConfig.getNutritionModifier(stack);
		int total = nutrition + uniqueIngredientBonus;
		return total > 0 ? Math.max(1, Math.round(total * modifier)) : 0;
	}

	public static float modifySaturation(float saturation, Level level, Player user, ItemStack stack) {
		float modifier = BalancedRecoveryConfig.getSaturationModifier(stack);
		saturation *= modifier;

		float cappedSaturation = Math.min(MAX_SATURATION, saturation);
		float extraSaturation = Math.max(0, saturation - MAX_SATURATION);
		float extraSaturationBonus = Math.max(0, (float) Math.log(extraSaturation));
		return cappedSaturation + extraSaturationBonus;
	}
}
