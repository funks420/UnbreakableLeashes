package com.example.unbreakableleashes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class UnbreakableLeashesPlugin extends JavaPlugin implements Listener {

    private static final double PULL_START_DISTANCE = 8.0;
    private static final double TELEPORT_DISTANCE = 45.0;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        // Run every tick to instantly catch high-speed movement like elytra flight with boats
        Bukkit.getScheduler().runTaskTimer(this, this::tickLeashedEntities, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityUnleash(EntityUnleashEvent event) {
        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE || 
            event.getReason() == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
            // Keep leashed unless explicitly un-leashed by right-clicking with shears/lead
            // For distance snapping, always cancel it
            if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE) {
                event.setCancelled(true);
            }
        }
    }

    private void tickLeashedEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (!entity.isLeashed()) {
                    continue;
                }

                Entity holder = entity.getLeashHolder();
                if (holder == null) {
                    continue;
                }

                // Handle cross-dimension tracking
                if (!entity.getWorld().equals(holder.getWorld())) {
                    entity.teleport(holder.getLocation());
                    continue;
                }

                // If the entity is riding a boat (like a chicken in a boat), target the boat for physics/pulling
                Entity targetEntity = entity.isInsideVehicle() && entity.getVehicle() instanceof Boat ? entity.getVehicle() : entity;

                Location targetLoc = targetEntity.getLocation();
                Location holderLoc = holder.getLocation();
                double distance = targetLoc.distance(holderLoc);

                if (distance > TELEPORT_DISTANCE) {
                    targetEntity.teleport(holderLoc);
                } else if (distance > PULL_START_DISTANCE) {
                    Vector direction = holderLoc.toVector().subtract(targetLoc.toVector()).normalize();
                    double pullStrength = Math.min((distance - PULL_START_DISTANCE) * 0.2, 2.0);
                    
                    Vector velocity = direction.multiply(pullStrength);
                    velocity.setY(Math.max(velocity.getY(), 0.3));
                    
                    targetEntity.setVelocity(velocity);
                    targetEntity.setFallDistance(0f);
                }
            }
        }
    }
}
