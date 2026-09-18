package amatsagu.balancedrecovery.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import amatsagu.balancedrecovery.client.event.RenderFoodHealingEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
	@Unique
	private static boolean setValues = false;

	@Unique
	private static int heartIndex = -1;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Nullable
	protected abstract Player getCameraPlayer();

	@Inject(method = "extractHearts", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/player/Player;level()Lnet/minecraft/world/level/Level;"))
	private void balancedrecovery$displayHealthGained(GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int healthRowHeight, int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, CallbackInfo ci, @Local(name = "type") Hud.HeartType type) {
		int size = Mth.ceil(maxHealth / 2F);
		RenderFoodHealingEvent.Hearts.prepareBuffers(size);
		RenderFoodHealingEvent.Hearts.heartType = type;
		heartIndex = size - 1;
		setValues = true;
	}

	@Inject(method = "extractHearts", at = @At("TAIL"))
	private void balancedrecovery$displayHealthGained(GuiGraphicsExtractor graphics, Player player, int xLeft, int yLineBase, int healthRowHeight, int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, CallbackInfo ci) {
		RenderFoodHealingEvent.Hearts.displayHealthGained(minecraft, graphics, player, maxHealth);
	}

	@Inject(method = "extractHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractHeart(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Hud$HeartType;IIZZZ)V", ordinal = 0))
	private void balancedrecovery$disableAbsorptionValueSetting(CallbackInfo ci, @Local(name = "healthContainerCount") int healthContainerCount, @Local(name = "containerIndex") int containerIndex) {
		setValues = containerIndex < healthContainerCount;
	}

	@Inject(method = "extractHeart", at = @At("HEAD"))
	private void balancedrecovery$displayHealthGained(GuiGraphicsExtractor graphics, Hud.HeartType type, int xo, int yo, boolean isHardcore, boolean blinks, boolean half, CallbackInfo ci) {
		if (setValues && type == Hud.HeartType.CONTAINER) {
			RenderFoodHealingEvent.Hearts.xPoses[heartIndex] = xo;
			RenderFoodHealingEvent.Hearts.yPoses[heartIndex] = yo;
			heartIndex--;
		}
	}

	@WrapOperation(method = "extractHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud$HeartType;getSprite(ZZZ)Lnet/minecraft/resources/Identifier;"))
	private Identifier balancedrecovery$displayHealthGained(Hud.HeartType instance, boolean isHardcore, boolean isHalf, boolean isBlink, Operation<Identifier> original) {
		Identifier value = original.call(instance, isHardcore, isHalf, isBlink);
		Identifier other = original.call(instance, isHardcore, !isHalf, isBlink);
		if (isHalf) {
			RenderFoodHealingEvent.Hearts.fullTexture = other;
			RenderFoodHealingEvent.Hearts.halfTexture = value;
		} else {
			RenderFoodHealingEvent.Hearts.fullTexture = value;
			RenderFoodHealingEvent.Hearts.halfTexture = other;
		}
		return value;
	}

	@ModifyVariable(method = "getAirBubbleYLine", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private int balancedrecovery$lowerAirBubbles(int yLineAir) {
		if (getCameraPlayer().getArmorValue() == 0) {
			return yLineAir + 10;
		}
		return yLineAir;
	}

	@Inject(method = "extractFood", at = @At("HEAD"), cancellable = true)
	private void balancedrecovery$hideHungerBar(GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight, CallbackInfo ci) {
		ci.cancel();
	}

	@ModifyVariable(method = "extractArmor", at = @At("HEAD"), argsOnly = true, ordinal = 2)
	private static int balancedrecovery$zeroHealthRowHeight(int healthRowHeight) {
		return 0;
	}

	@WrapOperation(method = "extractArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
	private static void balancedrecovery$moveArmorBar(GuiGraphicsExtractor instance, RenderPipeline renderPipeline, Identifier location, int x, int y, int width, int height, Operation<Void> original) {
		original.call(instance, renderPipeline, location, adjustArmorX(x), adjustArmorY(y), width, height);
	}

	@Unique
	private static int adjustArmorX(int value) {
		return value + 101;
	}

	@Unique
	private static int adjustArmorY(int value) {
		if (Minecraft.getInstance().gui.hud.getPlayerVehicleWithHealth() != null) {
			return Integer.MIN_VALUE;
		}
		return value + 10;
	}
}
