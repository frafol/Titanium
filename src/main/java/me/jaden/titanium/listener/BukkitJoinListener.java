package me.jaden.titanium.listener;

import me.jaden.titanium.Titanium;
import me.jaden.titanium.data.DataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;


public class BukkitJoinListener implements Listener {
    private final Titanium titanium = Titanium.getPlugin();

    @EventHandler(ignoreCancelled = true)
    void onJoin(PlayerJoinEvent event) {

        DataManager dataManager = titanium.getDataManager();

        if (event.getPlayer().hasPermission(this.titanium.getTitaniumConfig().getPermissionsConfig().getNotificationPermission()) || event.getPlayer().isOp()) {
            dataManager.getPlayerData().keySet().stream()
                    .filter(user -> user.getUUID().equals(event.getPlayer().getUniqueId())).findFirst()
                    .ifPresent(user -> dataManager.getPlayerData(user).setReceivingAlerts(true));
        }

    }

    @EventHandler(ignoreCancelled = true)
    void onPreJoin(AsyncPlayerPreLoginEvent event) {

        if (titanium.isDone()) {
            return;
        }

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        event.setKickMessage("§cTitanium is still loading, please try again in a few seconds.");
    }
}
