package fr.heneria.cosmetics.manager;

import fr.heneria.cosmetics.HeneriaCosmetics;
import fr.heneria.cosmetics.model.ActiveCosmetic;
import fr.heneria.cosmetics.model.Hat;
import fr.heneria.cosmetics.model.ParticleEffect;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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
    private final Map<String, Hat> hats = new LinkedHashMap<>();
    private final Map<String, ParticleEffect> particles = new LinkedHashMap<>();
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

        // Load Particles
        ConfigurationSection particlesSection = cosmeticConfig.getConfigurationSection("particles");
        if (particlesSection != null) {
            for (String key : particlesSection.getKeys(false)) {
                ConfigurationSection section = particlesSection.getConfigurationSection(key);
                if (section == null) continue;
                String name = section.getString("name");
                String permission = section.getString("permission");
                Material icon = Material.valueOf(section.getString("icon"));
                Particle particleType = Particle.valueOf(section.getString("particle_type"));
                int count = section.getInt("count");

                particles.put(key, new ParticleEffect(key, name, permission, icon, null, particleType, count));
            }
        }

        // Load Hats
        ConfigurationSection hatsSection = cosmeticConfig.getConfigurationSection("hats");
        if (hatsSection != null) {
            for (String key : hatsSection.getKeys(false)) {
                ConfigurationSection section = hatsSection.getConfigurationSection(key);
                if (section == null) continue;
                String name = section.getString("name");
                String permission = section.getString("permission");
                String hdbId = section.getString("hdb_id");
                String materialName = section.getString("material");
                List<String> lore = section.getStringList("lore");

                Material material = null;
                if (materialName != null) {
                    material = Material.valueOf(materialName);
                }

                Material icon = material != null ? material : Material.PLAYER_HEAD; // Default to player head if HDB or generic

                 // If it's a HDB head, we might want to fetch the itemStack for icon, but we store HDB ID.
                 // The icon material is just for fallback or simple representation if needed,
                 // but typically we want the actual item in the GUI.
                 // For now, let's keep icon as Material, but when generating GUI item, we check hdbId.

                 if (hdbId != null && material == null) {
                     // It is a head
                     icon = Material.PLAYER_HEAD;
                 }

                hats.put(key, new Hat(key, name, permission, icon, lore, hdbId, material));
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
                        ParticleEffect effect = particles.get(active.getCurrentParticleId());
                        if (effect != null) {
                            player.spawnParticle(effect.getParticleType(), player.getLocation(), effect.getCount());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Every 2 ticks
    }

    public ActiveCosmetic getActiveCosmetic(UUID uuid) {
        return activeCosmetics.computeIfAbsent(uuid, k -> new ActiveCosmetic());
    }

    public void equipHat(Player player, String hatId) {
        Hat hat = hats.get(hatId);
        if (hat == null) return;

        ActiveCosmetic active = getActiveCosmetic(player.getUniqueId());

        // If already wearing a cosmetic hat, restore original helmet first?
        // No, we should only restore original if we haven't already saved it?
        // Wait, if I am wearing CosmeticHat A, and I switch to CosmeticHat B.
        // My current helmet slot contains CosmeticHat A.
        // My real helmet is saved in `active.realHelmet`.
        // So I just need to replace the helmet slot with CosmeticHat B. I don't need to update `realHelmet`.

        // But if I am wearing NO cosmetic hat (just real helmet or air), I need to save the real helmet.
        if (!active.hasHat()) {
            ItemStack currentHelmet = player.getInventory().getHelmet();
            active.setRealHelmet(currentHelmet != null ? currentHelmet.clone() : null);
        }

        active.setCurrentHatId(hatId);

        ItemStack itemToWear;
        if (hat.getHdbId() != null && hdbApi != null) {
            itemToWear = hdbApi.getItemHead(hat.getHdbId());
        } else if (hat.getMaterial() != null) {
            itemToWear = new ItemStack(hat.getMaterial());
        } else {
            // Fallback
            itemToWear = new ItemStack(Material.STONE_BUTTON);
        }

        ItemMeta meta = itemToWear.getItemMeta();
        meta.displayName(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(hat.getName()));
        meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "cosmetic_hat"), org.bukkit.persistence.PersistentDataType.STRING, hatId);
        itemToWear.setItemMeta(meta);
        player.getInventory().setHelmet(itemToWear);

        // Send message
        String msg = cosmeticConfig.getString("messages.equipped");
        if (msg != null) {
             // Replace placeholder
             // Use LegacyComponentSerializer for the cosmetic name to preserve legacy colors inside MiniMessage
             String cosmeticName = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().serialize(
                 net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(hat.getName())
             );

             // Or better, construct a component
             net.kyori.adventure.text.Component cosmeticComp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(hat.getName());

             // Since we have a MiniMessage string with a placeholder, we should use a TagResolver or replacement.
             // But for simplicity given the current dependencies, we can use simple replacement if we are careful.
             // However, mixing is tricky.
             // Let's assume the user config for "equipped" is a MiniMessage string like "<green>You equipped <white>%cosmetic%".
             // We want %cosmetic% to be the colored name of the cosmetic.
             // If we just replace %cosmetic% with "§6Fire", MiniMessage might strip it or handle it weirdly.
             // Correct way is using placeholders.

             player.sendMessage(MiniMessage.miniMessage().deserialize(msg, net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component("cosmetic", cosmeticComp)));
        }
    }

    public void unequipHat(Player player) {
        ActiveCosmetic active = activeCosmetics.get(player.getUniqueId());
        if (active != null && active.hasHat()) {
            player.getInventory().setHelmet(active.getRealHelmet());
            active.setCurrentHatId(null);
            active.setRealHelmet(null);
        }
    }

    public void equipParticle(Player player, String particleId) {
        ParticleEffect effect = particles.get(particleId);
        if (effect == null) return;

        ActiveCosmetic active = getActiveCosmetic(player.getUniqueId());
        active.setCurrentParticleId(particleId);

        String msg = cosmeticConfig.getString("messages.equipped");
        if (msg != null) {
             net.kyori.adventure.text.Component cosmeticComp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(effect.getName());
             player.sendMessage(MiniMessage.miniMessage().deserialize(msg, net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component("cosmetic", cosmeticComp)));
        }
    }

    public void unequipParticle(Player player) {
        ActiveCosmetic active = activeCosmetics.get(player.getUniqueId());
        if (active != null) {
            active.setCurrentParticleId(null);
        }
    }

    public void unequipAll(Player player) {
        unequipHat(player);
        unequipParticle(player);
        String msg = cosmeticConfig.getString("messages.unequipped");
        if (msg != null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
        }
    }

    public void onPlayerQuit(Player player) {
        unequipHat(player); // Restore real helmet so it saves correctly to player file by server
        activeCosmetics.remove(player.getUniqueId());
    }

    public Map<String, Hat> getHats() {
        return hats;
    }

    public Map<String, ParticleEffect> getParticles() {
        return particles;
    }
}
