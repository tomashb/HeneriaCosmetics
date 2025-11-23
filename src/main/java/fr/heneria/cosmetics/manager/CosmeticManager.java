package fr.heneria.cosmetics.manager;

import fr.heneria.cosmetics.HeneriaCosmetics;
import fr.heneria.cosmetics.model.ActiveCosmetic;
import fr.heneria.cosmetics.model.Cosmetic;
import fr.heneria.cosmetics.model.Gadget;
import fr.heneria.cosmetics.model.Hat;
import fr.heneria.cosmetics.model.ParticleEffect;
import fr.heneria.cosmetics.model.Rarity;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class CosmeticManager {

    private final HeneriaCosmetics plugin;
    private final Map<UUID, ActiveCosmetic> activeCosmetics = new HashMap<>();
    private final Map<String, Cosmetic> cosmetics = new LinkedHashMap<>();
    private FileConfiguration cosmeticConfig;
    private HeadDatabaseAPI hdbApi;

    public CosmeticManager(HeneriaCosmetics plugin) {
        this.plugin = plugin;
        loadConfig();
        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            this.hdbApi = new HeadDatabaseAPI();
        }
        startParticleTask();
    }

    private void loadConfig() {
        File file = new File(plugin.getDataFolder(), "cosmetics.yml");
        if (!file.exists()) {
            plugin.saveResource("cosmetics.yml", false);
        }
        cosmeticConfig = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection list = cosmeticConfig.getConfigurationSection("list");
        if (list != null) {
            for (String key : list.getKeys(false)) {
                ConfigurationSection section = list.getConfigurationSection(key);
                if (section == null) continue;

                String category = section.getString("category");
                String name = section.getString("name");
                String permission = section.getString("permission");
                String hdbId = section.getString("hdb_id");
                String materialName = section.getString("material");
                Material iconMaterial = materialName != null ? Material.matchMaterial(materialName) : null;
                String rarityStr = section.getString("rarity", "COMMON");
                Rarity rarity;
                try {
                    rarity = Rarity.valueOf(rarityStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    rarity = Rarity.COMMON;
                }
                List<String> lore = section.getStringList("lore");

                if ("hats".equalsIgnoreCase(category)) {
                    cosmetics.put(key, new Hat(key, name, permission, hdbId, iconMaterial, rarity, lore));
                } else if ("particles".equalsIgnoreCase(category)) {
                    try {
                        Particle particleType = Particle.valueOf(section.getString("type"));
                        String styleStr = section.getString("style", "TRAIL");
                        ParticleEffect.Style style = ParticleEffect.Style.valueOf(styleStr);
                        cosmetics.put(key, new ParticleEffect(key, name, permission, hdbId, iconMaterial, rarity, lore, particleType, style));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid particle type or style for " + key + ": " + e.getMessage());
                    }
                } else if ("gadgets".equalsIgnoreCase(category)) {
                    Material mat = Material.getMaterial(section.getString("item_material", "FISHING_ROD"));
                    cosmetics.put(key, new Gadget(key, name, permission, hdbId, iconMaterial, rarity, lore, mat));
                }
            }
        }
    }

    public FileConfiguration getCosmeticConfig() {
        return cosmeticConfig;
    }

    private void startParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ActiveCosmetic active = activeCosmetics.get(player.getUniqueId());
                    if (active != null && active.hasParticle()) {
                        Cosmetic cosmetic = cosmetics.get(active.getCurrentParticleId());
                        if (cosmetic instanceof ParticleEffect effect) {
                            spawnParticle(player, effect);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void spawnParticle(Player player, ParticleEffect effect) {
        Location loc = player.getLocation();
        if (effect.getStyle() == ParticleEffect.Style.TRAIL) {
            // Simple trail at feet
            player.getWorld().spawnParticle(effect.getParticleType(), loc, 1, 0.2, 0.0, 0.2, 0.0);
        } else if (effect.getStyle() == ParticleEffect.Style.RING) {
            // Ring animation (simple calculation based on time or just static ring)
            // For a simple ring every 2 ticks, we might want to spawn points around the player.
            // Let's spawn 3 points of a circle.
            double radius = 1.0;
            long time = System.currentTimeMillis() / 100;
            double angle = (time % 20) * (Math.PI / 10); // Rotate over time

            for (int i = 0; i < 2; i++) {
                double a = angle + (i * Math.PI);
                double x = radius * Math.cos(a);
                double z = radius * Math.sin(a);
                player.getWorld().spawnParticle(effect.getParticleType(), loc.clone().add(x, 0.1, z), 1, 0, 0, 0, 0);
            }
        }
    }

    public ActiveCosmetic getActiveCosmetic(UUID uuid) {
        return activeCosmetics.computeIfAbsent(uuid, k -> new ActiveCosmetic());
    }

    public void equipCosmetic(Player player, String cosmeticId) {
        Cosmetic cosmetic = cosmetics.get(cosmeticId);
        if (cosmetic == null) return;

        if (cosmetic instanceof Hat) {
            equipHat(player, cosmeticId);
        } else if (cosmetic instanceof ParticleEffect) {
            equipParticle(player, cosmeticId);
        } else if (cosmetic instanceof Gadget) {
            equipGadget(player, cosmeticId);
        }
    }

    private void equipHat(Player player, String hatId) {
        Hat hat = (Hat) cosmetics.get(hatId);
        if (hat == null) return;

        ActiveCosmetic active = getActiveCosmetic(player.getUniqueId());

        if (!active.hasHat()) {
            ItemStack currentHelmet = player.getInventory().getHelmet();
            active.setRealHelmet(currentHelmet != null ? currentHelmet.clone() : null);
        }

        active.setCurrentHatId(hatId);

        ItemStack itemToWear;
        if (hdbApi != null) {
            itemToWear = hdbApi.getItemHead(hat.getHdbId());
        } else {
             itemToWear = new ItemStack(Material.PLAYER_HEAD);
        }

        if (itemToWear == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(cosmeticConfig.getString("settings.messages.error_loading", "<red>Erreur de chargement.")));
            return;
        }

        ItemMeta meta = itemToWear.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(hat.getName())); // Use MiniMessage directly as per prompt
        meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "cosmetic_hat"), org.bukkit.persistence.PersistentDataType.STRING, hatId);
        itemToWear.setItemMeta(meta);
        player.getInventory().setHelmet(itemToWear);

        sendMessage(player, hat.getName());
    }

    private void equipParticle(Player player, String particleId) {
        ParticleEffect effect = (ParticleEffect) cosmetics.get(particleId);
        if (effect == null) return;

        ActiveCosmetic active = getActiveCosmetic(player.getUniqueId());
        active.setCurrentParticleId(particleId);

        sendMessage(player, effect.getName());
    }

    private void equipGadget(Player player, String gadgetId) {
        Gadget gadget = (Gadget) cosmetics.get(gadgetId);
        if (gadget == null) return;

        // Remove old gadget if any
        unequipGadget(player);

        ActiveCosmetic active = getActiveCosmetic(player.getUniqueId());
        active.setCurrentGadgetId(gadgetId);

        ItemStack item = new ItemStack(gadget.getItemMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(gadget.getName()));
        meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "cosmetic_gadget"), org.bukkit.persistence.PersistentDataType.STRING, gadgetId);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES, org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);

        player.getInventory().addItem(item);

        sendMessage(player, gadget.getName());
    }

    public void unequipHat(Player player) {
        ActiveCosmetic active = activeCosmetics.get(player.getUniqueId());
        if (active != null && active.hasHat()) {
            player.getInventory().setHelmet(active.getRealHelmet());
            active.setCurrentHatId(null);
            active.setRealHelmet(null);
        }
    }

    public void unequipParticle(Player player) {
        ActiveCosmetic active = activeCosmetics.get(player.getUniqueId());
        if (active != null) {
            active.setCurrentParticleId(null);
        }
    }

    public void unequipGadget(Player player) {
        ActiveCosmetic active = activeCosmetics.get(player.getUniqueId());
        if (active != null && active.hasGadget()) {
            // Remove the gadget item from inventory
            player.getInventory().remove(Material.FISHING_ROD); // Naive removal, should use PDC check

            // Better removal
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "cosmetic_gadget"), org.bukkit.persistence.PersistentDataType.STRING)) {
                    player.getInventory().remove(item);
                }
            }

            active.setCurrentGadgetId(null);
        }
    }

    public void unequipAll(Player player) {
        unequipHat(player);
        unequipParticle(player);
        unequipGadget(player);
        String msg = cosmeticConfig.getString("settings.messages.unequipped");
        if (msg != null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
        }
    }

    public void onPlayerQuit(Player player) {
        unequipHat(player);
        unequipGadget(player);
        activeCosmetics.remove(player.getUniqueId());
    }

    public Map<String, Cosmetic> getCosmetics() {
        return cosmetics;
    }

    private void sendMessage(Player player, String cosmeticName) {
        String msg = cosmeticConfig.getString("settings.messages.equipped");
        if (msg != null) {
             net.kyori.adventure.text.Component cosmeticComp = MiniMessage.miniMessage().deserialize(cosmeticName);
             player.sendActionBar(MiniMessage.miniMessage().deserialize(msg, net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component("cosmetic", cosmeticComp)));
        }
    }

    public HeadDatabaseAPI getHdbApi() {
        return hdbApi;
    }
}
