package net.easecation.ghostypaperrecorder.recording;

import net.easecation.ghostypaperrecorder.model.GhostyItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumMap;
import java.util.Map;

public final class PaperItemMapper {
    private final Map<Material, String> identifiers = new EnumMap<>(Material.class);

    public PaperItemMapper() {
        registerTools();
        registerArmor();
        registerConsumables();
        registerProjectilesAndUtility();
    }

    public GhostyItem map(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) {
            return GhostyItem.AIR;
        }
        String identifier = identifiers.get(stack.getType());
        if (identifier == null) {
            return GhostyItem.AIR;
        }
        return new GhostyItem(identifier, damage(stack), stack.getAmount());
    }

    private static int damage(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof Damageable damageable) {
            return damageable.getDamage();
        }
        return 0;
    }

    private void registerTools() {
        register("wooden_sword", Material.WOODEN_SWORD);
        register("stone_sword", Material.STONE_SWORD);
        register("iron_sword", Material.IRON_SWORD);
        register("golden_sword", Material.GOLDEN_SWORD);
        register("diamond_sword", Material.DIAMOND_SWORD);
        register("netherite_sword", Material.NETHERITE_SWORD);
        register("wooden_axe", Material.WOODEN_AXE);
        register("stone_axe", Material.STONE_AXE);
        register("iron_axe", Material.IRON_AXE);
        register("golden_axe", Material.GOLDEN_AXE);
        register("diamond_axe", Material.DIAMOND_AXE);
        register("netherite_axe", Material.NETHERITE_AXE);
        register("bow", Material.BOW);
        register("crossbow", Material.CROSSBOW);
        register("trident", Material.TRIDENT);
        register("fishing_rod", Material.FISHING_ROD);
        register("shield", Material.SHIELD);
    }

    private void registerArmor() {
        String[] materials = {"leather", "chainmail", "iron", "golden", "diamond", "netherite"};
        String[] pieces = {"helmet", "chestplate", "leggings", "boots"};
        for (String material : materials) {
            for (String piece : pieces) {
                Material bukkitMaterial = Material.matchMaterial((material + "_" + piece).toUpperCase());
                if (bukkitMaterial != null) {
                    register(material + "_" + piece, bukkitMaterial);
                }
            }
        }
        register("turtle_helmet", Material.TURTLE_HELMET);
    }

    private void registerConsumables() {
        register("apple", Material.APPLE);
        register("golden_apple", Material.GOLDEN_APPLE);
        register("enchanted_golden_apple", Material.ENCHANTED_GOLDEN_APPLE);
        register("bread", Material.BREAD);
        register("cooked_beef", Material.COOKED_BEEF);
        register("cooked_porkchop", Material.COOKED_PORKCHOP);
        register("cooked_chicken", Material.COOKED_CHICKEN);
        register("cooked_mutton", Material.COOKED_MUTTON);
        register("cooked_cod", Material.COOKED_COD);
        register("cooked_salmon", Material.COOKED_SALMON);
        register("mushroom_stew", Material.MUSHROOM_STEW);
        register("potion", Material.POTION);
        register("splash_potion", Material.SPLASH_POTION);
        register("lingering_potion", Material.LINGERING_POTION);
    }

    private void registerProjectilesAndUtility() {
        register("arrow", Material.ARROW);
        register("spectral_arrow", Material.SPECTRAL_ARROW);
        register("ender_pearl", Material.ENDER_PEARL);
        register("snowball", Material.SNOWBALL);
        register("egg", Material.EGG);
        register("firework_rocket", Material.FIREWORK_ROCKET);
        register("totem_of_undying", Material.TOTEM_OF_UNDYING);
        register("water_bucket", Material.WATER_BUCKET);
        register("lava_bucket", Material.LAVA_BUCKET);
        register("milk_bucket", Material.MILK_BUCKET);
    }

    private void register(String identifier, Material material) {
        identifiers.put(material, "minecraft:" + identifier);
    }
}
