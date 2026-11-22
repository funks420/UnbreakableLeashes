package com.ranull.unbreakableleashes.listener;

import com.ranull.unbreakableleashes.UnbreakableLeashes;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;

public class EntityPortalListener implements Listener {
    private final UnbreakableLeashes plugin;

    public EntityPortalListener(UnbreakableLeashes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) event.getEntity();

            event.setCancelled(plugin.getLeashManager().isWorldValid(livingEntity.getWorld())
                    && livingEntity.isLeashed() && livingEntity.getLeashHolder() instanceof Player
                    && livingEntity.getLeashHolder().hasPermission("unbreakableleashes.use"));
        }
    }
}
