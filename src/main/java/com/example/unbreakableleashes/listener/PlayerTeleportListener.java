package com.ranull.unbreakableleashes.listener;

import com.ranull.unbreakableleashes.UnbreakableLeashes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements Listener {
    private final UnbreakableLeashes plugin;

    public PlayerTeleportListener(UnbreakableLeashes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() != null && event.getTo() != null && event.getTo().getWorld() != null
                && plugin.getLeashManager().isWorldValid(event.getFrom().getWorld())
                && plugin.getLeashManager().isWorldValid(event.getTo().getWorld())
                && event.getPlayer().hasPermission("unbreakableleashes.use")
                && (plugin.getWorldGuard() == null || (plugin.getWorldGuard().shouldTeleport(event.getTo())
                && plugin.getWorldGuard().shouldTeleport(event.getFrom())))) {
            plugin.getLeashManager().teleportLeashed(event.getPlayer());
        }
    }
}
