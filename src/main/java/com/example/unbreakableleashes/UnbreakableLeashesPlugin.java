package com.example.unbreakableleashes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class UnbreakableLeashesPlugin extends JavaPlugin implements Listener {

    private static final double PULL_START_DISTANCE = 6.0;
    private static final double TELEPORT_DISTANCE = 35.0;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, this::tickLeashedEntities, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityUnleash(EntityUnleashEvent event) {
        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE || 
            event.getReason() == EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        // Only unleash when Shift + Right-Clicking
        if (!player.isSneaking()) {
            return;
        }

        Entity clicked = event.getRightClicked();

        if (clicked instanceof Boat boat) {
            boolean unleashedAny = false;
            for (Entity passenger : boat.getPassengers()) {
                if (passenger instanceof LivingEntity living && living.isLeashed() && living.getLeashHolder().equals(player)) {
                    living.setLeashHolder(null);
                    boat.getWorld().dropItemNaturally(boat.getLocation(), new ItemStack(Material.LEAD));
                    unleashedAny = true;
                }
            }
            if (unleashedAny) {
                event.setCancelled(true);
            }
        } else if (clicked instanceof LivingEntity living && living.isLeashed() && living.getLeashHolder().equals(player)) {
            living.setLeashHolder(null);
            living.getWorld().dropItemNaturally(living.getLocation(), new ItemStack(Material.LEAD));
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
                if (holder == null) {
                    continue;
                }

                if (!entity.getWorld().equals(holder.getWorld())) {
                    entity.teleport(holder.getLocation());
                    continue;
                }

                Entity targetEntity = entity.isInsideVehicle() && entity.getVehicle() instanceof Boat ? entity.getVehicle() : entity;

                Location targetLoc = targetEntity.getLocation();
                Location holderLoc = holder.getLocation();
                
                if (!targetLoc.getWorld().equals(holderLoc.getWorld())) {
                    continue;
                }

                double distance = targetLoc.distance(holderLoc);

                if (distance > TELEPORT_DISTANCE) {
                    targetEntity.teleport(holderLoc);
                    targetEntity.setVelocity(new Vector(0, 0, 0));
                } else if (distance > PULL_START_DISTANCE) {
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
