package com.ranull.unbreakableleashes.listener;

import com.ranull.unbreakableleashes.UnbreakableLeashes;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;

public class EntityUnleashListener implements Listener {
    private final UnbreakableLeashes plugin;

    public EntityUnleashListener(UnbreakableLeashes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityUnleash(EntityUnleashEvent event) {
        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE && event.getEntity() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) event.getEntity();

            if (livingEntity.isLeashed() && livingEntity.getLeashHolder() instanceof Player
                    && livingEntity.getWorld() == livingEntity.getWorld()
                    && livingEntity.getLeashHolder().hasPermission("unbreakableleashes.use")
                    && (plugin.getWorldGuard() == null || (plugin.getWorldGuard()
                    .shouldTeleport(livingEntity.getLocation())
                    && plugin.getWorldGuard().shouldTeleport(livingEntity.getLeashHolder().getLocation())))) {
                plugin.getLeashManager().teleportLeashed((Player) livingEntity.getLeashHolder(), livingEntity);
            }
        }
    }
}
