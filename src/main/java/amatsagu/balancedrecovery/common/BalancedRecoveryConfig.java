package amatsagu.balancedrecovery.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BalancedRecoveryConfig {
	public static boolean warmthHealing = true;
	public static int warmthDetectionRange = 5;
	public static boolean fasterFluidConsumption = true;

	public static float healthGainMultiplier = 1F;
	public static float regenerationTimeMultiplier = 1F;

	public static boolean displayHealthGained = true;

	public static final List<String> warmthBlocks = new ArrayList<>();
	public static final List<FoodModifier> foodModifiers = new ArrayList<>();

	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("balanced_recovery.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static void load() {
		try {
			if (Files.exists(CONFIG_PATH)) {
				try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
					JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
					if (obj.has("warmthHealing")) warmthHealing = obj.get("warmthHealing").getAsBoolean();
					if (obj.has("warmthDetectionRange")) warmthDetectionRange = Math.max(1, obj.get("warmthDetectionRange").getAsInt());
					if (obj.has("fasterFluidConsumption")) fasterFluidConsumption = obj.get("fasterFluidConsumption").getAsBoolean();
					if (obj.has("healthGainMultiplier")) healthGainMultiplier = Math.max(0, obj.get("healthGainMultiplier").getAsFloat());
					if (obj.has("regenerationTimeMultiplier")) regenerationTimeMultiplier = Math.max(0, obj.get("regenerationTimeMultiplier").getAsFloat());
					if (obj.has("displayHealthGained")) displayHealthGained = obj.get("displayHealthGained").getAsBoolean();

					if (obj.has("warmthBlocks") && obj.get("warmthBlocks").isJsonArray()) {
						warmthBlocks.clear();
						for (JsonElement el : obj.getAsJsonArray("warmthBlocks")) {
							if (el.isJsonPrimitive()) {
								warmthBlocks.add(el.getAsString());
							}
						}
					}

					if (obj.has("foodModifiers") && obj.get("foodModifiers").isJsonArray()) {
						foodModifiers.clear();
						for (JsonElement el : obj.getAsJsonArray("foodModifiers")) {
							if (el.isJsonObject()) {
								JsonObject modObj = el.getAsJsonObject();
								String target = modObj.has("target") ? modObj.get("target").getAsString() : "";
								float nutrition = modObj.has("nutrition") ? modObj.get("nutrition").getAsFloat() : 1.0F;
								float saturation = modObj.has("saturation") ? modObj.get("saturation").getAsFloat() : 1.0F;

								if (!target.isEmpty()) {
									foodModifiers.add(new FoodModifier(target, nutrition, saturation));
								}
							}
						}
					}
				}
			}
			save();
		} catch (Exception e) {
			String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			BalancedRecovery.LOGGER.error("[Balanced Recovery] Failed to load config file ({}): {}", CONFIG_PATH.toAbsolutePath(), errorMsg, e);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			JsonObject obj = new JsonObject();
			obj.addProperty("warmthHealing", warmthHealing);
			obj.addProperty("warmthDetectionRange", warmthDetectionRange);
			obj.addProperty("fasterFluidConsumption", fasterFluidConsumption);
			obj.addProperty("healthGainMultiplier", healthGainMultiplier);
			obj.addProperty("regenerationTimeMultiplier", regenerationTimeMultiplier);
			obj.addProperty("displayHealthGained", displayHealthGained);

			JsonArray warmthArray = new JsonArray();
			for (String block : warmthBlocks) {
				warmthArray.add(block);
			}
			obj.add("warmthBlocks", warmthArray);

			JsonArray modifiersArray = new JsonArray();
			for (FoodModifier modifier : foodModifiers) {
				JsonObject modObj = new JsonObject();
				modObj.addProperty("target", modifier.target);
				modObj.addProperty("nutrition", modifier.nutrition);
				modObj.addProperty("saturation", modifier.saturation);
				modifiersArray.add(modObj);
			}
			obj.add("foodModifiers", modifiersArray);

			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(obj, writer);
			}
		} catch (Exception e) {
			String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			BalancedRecovery.LOGGER.error("[Balanced Recovery] Failed to save config file ({}): {}", CONFIG_PATH.toAbsolutePath(), errorMsg, e);
		}
	}

	public static boolean isWarmthSource(BlockState state) {
		if (state == null) return false;
		for (String target : warmthBlocks) {
			if (matchesBlock(state, target)) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesBlock(BlockState state, String spec) {
		if (spec == null || spec.isEmpty()) return false;
		String target;
		String props = null;
		int open = spec.indexOf('[');
		int close = spec.lastIndexOf(']');
		if (open != -1 && close > open) {
			target = spec.substring(0, open).trim();
			props = spec.substring(open + 1, close).trim();
		} else {
			target = spec.trim();
		}

		if (target.startsWith("#")) {
			Identifier id = Identifier.tryParse(target.substring(1));
			if (id == null || !state.is(TagKey.create(Registries.BLOCK, id))) {
				return false;
			}
		} else {
			Identifier id = Identifier.tryParse(target);
			if (id == null || !BuiltInRegistries.BLOCK.getOptional(id).map(state::is).orElse(false)) {
				return false;
			}
		}

		if (props != null && !props.isEmpty()) {
			for (String pair : props.split(",")) {
				String[] kv = pair.split("=", 2);
				if (kv.length == 2) {
					String key = kv[0].trim();
					String value = kv[1].trim();
					Property<?> prop = state.getBlock().getStateDefinition().getProperty(key);
					if (prop == null || !state.getValue(prop).toString().equalsIgnoreCase(value)) {
						return false;
					}
				}
			}
		} else {
			if (state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)) {
				return false;
			}
		}
		return true;
	}

	public static float getNutritionModifier(ItemStack stack) {
		float multiplier = 1.0F;
		for (FoodModifier modifier : foodModifiers) {
			if (modifier.matches(stack)) {
				multiplier *= modifier.nutrition;
			}
		}
		return multiplier;
	}

	public static float getSaturationModifier(ItemStack stack) {
		float multiplier = 1.0F;
		for (FoodModifier modifier : foodModifiers) {
			if (modifier.matches(stack)) {
				multiplier *= modifier.saturation;
			}
		}
		return multiplier;
	}

	public static class FoodModifier {
		public final String target;
		public final float nutrition;
		public final float saturation;

		public FoodModifier(String target, float nutrition, float saturation) {
			this.target = target;
			this.nutrition = nutrition;
			this.saturation = saturation;
		}

		public boolean matches(ItemStack stack) {
			if (stack == null || stack.isEmpty() || target == null || target.isEmpty()) {
				return false;
			}
			if (target.startsWith("#")) {
				Identifier id = Identifier.tryParse(target.substring(1));
				if (id != null) {
					return stack.is(TagKey.create(Registries.ITEM, id));
				}
			} else {
				Identifier id = Identifier.tryParse(target);
				if (id != null) {
					return BuiltInRegistries.ITEM.getOptional(id).map(stack::is).orElse(false);
				}
			}
			return false;
		}
	}

	static {
		warmthBlocks.add("minecraft:campfire[lit=true]");
		warmthBlocks.add("minecraft:furnace[lit=true]");
		warmthBlocks.add("minecraft:smoker[lit=true]");
		warmthBlocks.add("minecraft:blast_furnace[lit=true]");
		foodModifiers.add(new FoodModifier("minecraft:honey_bottle", 1.0F, 6.0F));
		foodModifiers.add(new FoodModifier("minecraft:pumpkin_pie", 1.0F, 3.0F));
		foodModifiers.add(new FoodModifier("minecraft:bread", 1.0F, 0.75F));
		load();
	}
}
