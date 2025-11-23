package fr.heneria.cosmetics.listener;

import fr.heneria.cosmetics.HeneriaCosmetics;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class GadgetListener implements Listener {

    private final HeneriaCosmetics plugin;
    private final Random random = new Random();

    public GadgetListener(HeneriaCosmetics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isGadget(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !isGadget(item)) return;

        String gadgetId = getGadgetId(item);
        if (gadgetId == null) return;

        event.setCancelled(true); // Prevent normal interaction

        Player player = event.getPlayer();

        // Cooldown check? Prompt doesn't specify cooldowns but it's good practice.
        // For simplicity I will just run the logic.

        switch (gadgetId) {
            case "grappling_hook":
                Vector dir = player.getLocation().getDirection();
                player.setVelocity(dir.multiply(1.5));
                player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1f, 1f);
                break;
            case "paintball_gun":
                Snowball snowball = player.launchProjectile(Snowball.class);
                snowball.setVelocity(player.getLocation().getDirection().multiply(2));
                player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 1f);
                break;
            case "firework_launcher":
                spawnRandomFirework(player.getLocation());
                break;
            case "tnt_toss":
                TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
                tnt.setVelocity(player.getLocation().getDirection().multiply(1));
                tnt.setFuseTicks(40);
                // Mark TNT as gadget TNT
                tnt.getPersistentDataContainer().set(new NamespacedKey(plugin, "gadget_tnt"), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                tnt.setSource(player);
                break;
            case "magic_wand":
                player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
                break;
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof TNTPrimed tnt) {
            if (tnt.getPersistentDataContainer().has(new NamespacedKey(plugin, "gadget_tnt"), org.bukkit.persistence.PersistentDataType.BYTE)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isGadget(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "cosmetic_gadget"), org.bukkit.persistence.PersistentDataType.STRING);
    }

    private String getGadgetId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "cosmetic_gadget"), org.bukkit.persistence.PersistentDataType.STRING);
    }

    private void spawnRandomFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();

        FireworkEffect.Type type = FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)];
        Color c1 = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        Color c2 = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(random.nextBoolean())
                .withColor(c1)
                .withFade(c2)
                .with(type)
                .trail(random.nextBoolean())
                .build();

        meta.addEffect(effect);
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }
}
