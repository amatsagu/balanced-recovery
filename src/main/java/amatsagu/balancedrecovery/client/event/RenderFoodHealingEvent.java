package amatsagu.balancedrecovery.client.event;

import amatsagu.balancedrecovery.client.BalancedRecoveryClient;
import amatsagu.balancedrecovery.common.BalancedRecovery;
import amatsagu.balancedrecovery.common.BalancedRecoveryConfig;
import amatsagu.balancedrecovery.common.component.entity.FoodHealingComponent;
import amatsagu.balancedrecovery.common.event.IncreaseSaturationEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import vectorwing.farmersdelight.common.block.PieBlock;

import java.text.NumberFormat;
import java.util.List;

import static amatsagu.balancedrecovery.common.component.entity.FoodHealingComponent.getTicksPerHeal;

public class RenderFoodHealingEvent {
	private static ItemStack CAKE_STACK = null;

	public static void init() {
		ClientTickEvents.END_LEVEL_TICK.register(new Tick());
		ItemTooltipCallback.EVENT.register(new Tooltip());
	}

	public static class Hearts {
		public static Identifier fullTexture = null, halfTexture = null;
		public static Hud.HeartType heartType;
		public static int[] xPoses = null, yPoses = null;
		public static int color = -1;

		public static void prepareBuffers(int size) {
			if (xPoses == null || xPoses.length < size) {
				xPoses = new int[size];
				yPoses = new int[size];
			}
		}

		public static void displayHealthGained(Minecraft client, GuiGraphicsExtractor graphics, Player player, float maxHealth) {
			try {
				if (!BalancedRecoveryConfig.displayHealthGained || !BalancedRecoveryClient.naturalHealthRegeneration) {
					return;
				}

				if (player == null) {
					return;
				}

				int health = Mth.ceil(player.getHealth());
				if (health >= maxHealth) {
					return;
				}

				int toHeal = getHealAmount(client, player, client.level);
				if (toHeal <= 0) {
					return;
				}

				color = ARGB.colorFromFloat((Mth.sin(Tick.renderTicks / 4F) + 1) / 3F, 1, 1, 1);
				int targetHealth = Math.min((int) maxHealth, health + toHeal);
				for (int i = health; i < targetHealth; i++) {
					int index = i / 2;
					int currentHealth = i - health;
					int xOffset = (i % 2 == 1) ? 5 : 0;
					boolean currentlyHalf = (health % 2 != currentHealth % 2);

					graphics.blitSprite(
							RenderPipelines.GUI_TEXTURED, currentlyHalf ? fullTexture : halfTexture,
							9, 9,
							currentlyHalf ? 5 : 0, 0,
							xPoses[index] + xOffset, yPoses[index],
							5, 9);
				}
			} finally {
				fullTexture = halfTexture = null;
				heartType = null;
				color = -1;
			}
		}
	}

	private static class Tick implements ClientTickEvents.EndLevelTick {
		private final Minecraft client = Minecraft.getInstance();
		private static int renderTicks = 0;

		@Override
		public void onEndTick(ClientLevel level) {
			if (client.player == null || getHealAmount(client, client.player, level) == 0) {
				renderTicks = (int) -Math.TAU;
				return;
			}

			renderTicks++;
		}
	}

	private static class Tooltip implements ItemTooltipCallback {
		private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
		private final Minecraft client = Minecraft.getInstance();

		@Override
		public void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> lines) {
			if (!BalancedRecoveryConfig.displayHealthGained || !BalancedRecoveryClient.naturalHealthRegeneration) {
				return;
			}

			if (stack == null || !stack.has(DataComponents.FOOD)) {
				return;
			}

			int healAmount = getItemHealAmount(client.player, client.level, stack);
			if (healAmount <= 0) {
				return;
			}

			float seconds = getMaximumHealTicks(healAmount, client.player, client.level, stack) / 20F;
			MutableComponent text = Component.literal(NUMBER_FORMAT.format(healAmount / 2F) + " ").withStyle(ChatFormatting.GRAY);
			text.append(Component.literal("❤ ").withStyle(ChatFormatting.RED));
			text.append(Component.literal("/ " + NUMBER_FORMAT.format(seconds) + "s").withStyle(ChatFormatting.GRAY));
			lines.add(1, text);
		}

		private static int getMaximumHealTicks(int healAmount, Player player, Level level, ItemStack stack) {
			FoodProperties food = stack.get(DataComponents.FOOD);
			if (food == null) {
				return 0;
			}

			float saturation = IncreaseSaturationEvent.modifySaturation(food.saturation(), level, player, stack);
			return healAmount * getTicksPerHeal(saturation);
		}
	}

	private static int getHealAmount(Minecraft client, Player player, Level level) {
		if (player == null || level == null) {
			return 0;
		}

		FoodHealingComponent foodHealing = FoodHealingComponent.get(player);
		if (foodHealing.getHealAmount() > 0) {
			return foodHealing.getHealAmount() - foodHealing.getAmountHealed();
		}

		int toHeal = getItemHealAmount(player, level, player.getUseItem());
		if (toHeal > 0) {
			return toHeal;
		}

		if (client != null && client.hitResult instanceof BlockHitResult blockHitResult) {
			toHeal = getBlockHealAmount(player, level, level.getBlockState(blockHitResult.getBlockPos()));
			if (toHeal > 0) {
				return toHeal;
			}
		}

		toHeal = getItemHealAmount(player, level, player.getMainHandItem());
		if (toHeal > 0) {
			return toHeal;
		}

		return getItemHealAmount(player, level, player.getOffhandItem());
	}

	private static ItemStack getCakeStack() {
		if (CAKE_STACK == null) {
			CAKE_STACK = Items.CAKE.getDefaultInstance();
		}

		return CAKE_STACK;
	}

	private static int getBlockHealAmount(Player player, Level level, BlockState state) {
		if (state == null) {
			return 0;
		}
		
		if (state.getBlock() instanceof CakeBlock) {
			return getItemHealAmount(player, level, getCakeStack());
		}

		if (BalancedRecovery.farmersDelightLoaded && state.getBlock() instanceof PieBlock pieBlock) {
			return getItemHealAmount(player, level, pieBlock.getPieSliceItem());
		}
		
		return 0;
	}

	private static int getItemHealAmount(Player player, Level level, ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}

		int nutrition = 0;
		FoodProperties food = stack.get(DataComponents.FOOD);
		if (food != null) {
			nutrition = food.nutrition();
		} else if (stack.is(Items.CAKE)) {
			nutrition = 2;
		}

		if (nutrition <= 0) {
			return 0;
		}

		nutrition = IncreaseSaturationEvent.modifyNutrition(nutrition, level, player, stack);
		return Mth.floor(nutrition * BalancedRecoveryConfig.healthGainMultiplier);
	}
}
