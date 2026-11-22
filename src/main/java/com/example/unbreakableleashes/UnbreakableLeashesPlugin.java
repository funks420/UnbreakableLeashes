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

    // Widened threshold so sharp turns don't instantly trigger distance snaps
    private static final double PULL_START_DISTANCE = 6.0;
    private static final double TELEPORT_DISTANCE = 35.0;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        // Run every single tick for ultra-responsive high-speed tracking
        Bukkit.getScheduler().runTaskTimer(this, this::tickLeashedEntities, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityUnleash(EntityUnleashEvent event) {
        // Cancel lead breaks triggered by distance or firework player movement actions
        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE || 
            event.getReason() == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
            event.setCancelled(true);
        }
    }
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

                Entity targetEntity = entity.isInsideVehicle() && entity.getVehicle() instanceof Boat ? entity.getVehicle() : entity;

                Location targetLoc = targetEntity.getLocation();
                Location holderLoc = holder.getLocation();
                
                // If worlds match but distance check fails unexpectedly, ensure same dimension check
                if (!targetLoc.getWorld().equals(holderLoc.getWorld())) {
                    continue;
                }

                double distance = targetLoc.distance(holderLoc);

                if (distance > TELEPORT_DISTANCE) {
                    // Instant catch-up for sharp whip turns that stretch the lead past safe limits
                    targetEntity.teleport(holderLoc);
                    targetEntity.setVelocity(new Vector(0, 0, 0));
                } else if (distance > PULL_START_DISTANCE) {
                    // Aggressive velocity multiplier during sharp turns to counteract centrifugal lag
                    Vector direction = holderLoc.toVector().subtract(targetLoc.toVector()).normalize();
                    double pullStrength = Math.min((distance - PULL_START_DISTANCE) * 0.35, 3.5);
                    
                    Vector velocity = direction.multiply(pullStrength);
                    velocity.setY(Math.max(velocity.getY(), 0.35));
                    
                    targetEntity.setVelocity(velocity);
                    targetEntity.setFallDistance(0f);
                }
            }
        }
    }
}
