package fr.heneria.cosmetics;

import fr.heneria.cosmetics.gui.CosmeticsGUI;
import fr.heneria.cosmetics.manager.CosmeticManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class HeneriaCosmetics extends JavaPlugin implements Listener, CommandExecutor {

    private CosmeticManager cosmeticManager;
    private CosmeticsGUI cosmeticsGUI;

    @Override
    public void onEnable() {
        // Initialize Manager
        this.cosmeticManager = new CosmeticManager(this);

        // Initialize GUI
        this.cosmeticsGUI = new CosmeticsGUI(this);

        // Register Command
        getCommand("cosmetics").setExecutor(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new fr.heneria.cosmetics.listener.PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new fr.heneria.cosmetics.listener.GadgetListener(this), this);

        getLogger().info("HeneriaCosmetics enabled!");
    }

    @Override
    public void onDisable() {
        // Unequip everyone's hats to prevent item loss/duplication issues on reload
        for (Player player : getServer().getOnlinePlayers()) {
            cosmeticManager.onPlayerQuit(player);
        }
        getLogger().info("HeneriaCosmetics disabled!");
    }

    public CosmeticManager getCosmeticManager() {
        return cosmeticManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Seul un joueur peut utiliser cette commande.");
            return true;
        }

        cosmeticsGUI.openMainMenu(player);
        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cosmeticManager.onPlayerQuit(event.getPlayer());
    }
}
