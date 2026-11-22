package com.example.unbreakableleashes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class UnbreakableLeashesPlugin extends JavaPlugin implements Listener {

    // Max distance before elastic pull kicks in
    private static final double PULL_START_DISTANCE = 8.0;
    // Distance at which the entity is teleported directly to the holder (e.g., fast elytra flight)
    private static final double TELEPORT_DISTANCE = 45.0;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Run every 2 ticks (0.1 seconds) to keep leashed entities pulled smoothly
        Bukkit.getScheduler().runTaskTimer(this, this::tickLeashedEntities, 1L, 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityUnleash(EntityUnleashEvent event) {
        // Only cancel if the lead snapped due to distance
        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE) {
            event.setCancelled(true);
        }
    }

    private void tickLeashedEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (!entity.isLeashed()) {
                    continue;
                }

                Entity holder = entity.getLeashHolder();

                // Handle cross-dimension teleports (e.g., player goes through a Nether portal)
                if (!entity.getWorld().equals(holder.getWorld())) {
                    entity.teleport(holder.getLocation());
                    continue;
                }

                Location entityLoc = entity.getLocation();
                Location holderLoc = holder.getLocation();
                double distance = entityLoc.distance(holderLoc);

                if (distance > TELEPORT_DISTANCE) {
                    entity.teleport(holderLoc);
                } else if (distance > PULL_START_DISTANCE) {
                    // Calculate normalized direction vector toward the leash holder
                    Vector direction = holderLoc.toVector().subtract(entityLoc.toVector()).normalize();
                    double pullStrength = Math.min((distance - PULL_START_DISTANCE) * 0.15, 1.5);
                    
                    Vector velocity = direction.multiply(pullStrength);
                    // Add a slight upward boost so mobs don't get stuck on 1-block ledges
                    velocity.setY(Math.max(velocity.getY(), 0.25));
                    
                    entity.setVelocity(velocity);
                    entity.setFallDistance(0f); // Prevent fall damage while being dragged
                }
            }
        }
    }
}