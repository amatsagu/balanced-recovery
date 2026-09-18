<div align="center">
  <h1>Balanced Recovery</h1>
  <h3>A modern Fabric mod that removes hunger and makes food recover health over time.</h3>
</div>

## List of features
- Replaces hunger / saturation system for healing over time after eating food.
  * Health recovery value is based on food's nutrition value.
  * Recovery speed depends from food's saturation value.
    * *Super strong, modded foods will have diminishing returns in recovery speed.*
  * Complex foods crafted from multiple unique ingredients receive bonus nutrition.
- Adds a tooltip to all food items that shows how much it can heal over time.
- Adds an overlay to the health bar while holding a food item (or while recovering health).
- Moves the armor bar to where the hunger bar used to be.
- Improves fluid consumption: fluids such as Honey, Stews, Milk or Potions are now consumed at 2x animation speed.
- Overhauls some status effects related to replaced hunger / saturation system:
  * `Hunger`: prevents you from healing using food, and can even cause damage at higher levels.
  * `Saturation`: instant heals you based on the amplifier.
  * `Nourishment`: slowly heals you every 10 seconds. This effect requires **Farmer's Delight** to be installed.
- Adds new warmth effect: (lit) furnaces & campfires will give you weak regeneration effect when nearby (by default 3 blocks range).

## Configuration

Settings can be changed in `.minecraft/config/balanced_recovery.json`:

```json
{
  "warmthHealing": true,
  "warmthDetectionRange": 3,
  "fasterFluidConsumption": true,
  "healthGainMultiplier": 1.0,
  "regenerationTimeMultiplier": 1.0,
  "displayHealthGained": true,
  "warmthBlocks": [
    "minecraft:campfire[lit=true]",
    "minecraft:furnace[lit=true]",
    "minecraft:smoker[lit=true]",
    "minecraft:blast_furnace[lit=true]"
  ],
  "foodModifiers": [
    {
      "target": "minecraft:honey_bottle",
      "nutrition": 1.0,
      "saturation": 6.0
    },
    {
      "target": "minecraft:pumpkin_pie",
      "nutrition": 1.0,
      "saturation": 3.0
    },
    {
      "target": "minecraft:bread",
      "nutrition": 1.0,
      "saturation": 0.75
    }
  ]
}
```

## Mod compatibility
**Balanced Recovery** has dedicated support for **Farmer’s Delight**. Other food mods should work, but custom food effects or buffs may not be fully supported.

Mods that modify the player HUD - specifically health, armor, or hunger bars are likely to conflict with **Balanced Recovery**.