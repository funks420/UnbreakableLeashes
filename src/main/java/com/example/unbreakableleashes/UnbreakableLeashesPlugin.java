package com.example.unbreakableleashes;

import io.papermc.paper.entity.Leashable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
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
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (handleSneakUnleash(event.getPlayer(), event.getRightClicked(), event.getHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (handleSneakUnleash(event.getPlayer(), event.getRightClicked(), event.getHand())) {
            event.setCancelled(true);
        }
    }

    private boolean handleSneakUnleash(Player player, Entity clicked, EquipmentSlot hand) {
        if (hand != EquipmentSlot.HAND || !player.isSneaking()) {
            return false;
        }

        boolean unleashed = false;

        // 1. Check if the clicked entity itself (e.g., Boat or Mob) is directly leashed to the player
        if (clicked instanceof Leashable leashable && leashable.isLeashed() && player.equals(leashable.getLeashHolder())) {
            leashable.setLeashHolder(null);
            clicked.getWorld().dropItemNaturally(clicked.getLocation(), new ItemStack(Material.LEAD));
            unleashed = true;
        }

        // 2. Also check if the clicked entity is a Boat carrying a leashed passenger
        if (clicked instanceof Boat boat) {
            for (Entity passenger : boat.getPassengers()) {
                if (passenger instanceof Leashable leashable && leashable.isLeashed() && player.equals(leashable.getLeashHolder())) {
                    leashable.setLeashHolder(null);
                    boat.getWorld().dropItemNaturally(boat.getLocation(), new ItemStack(Material.LEAD));
                    unleashed = true;
                }
            }
        }

        return unleashed;
    }

    private void tickLeashedEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Leashable leashable) || !leashable.isLeashed()) {
                    continue;
                }

                Entity holder = leashable.getLeashHolder();
                if (holder == null) {
                    continue;
                }

                // If a passenger is leashed, pull its boat; if the boat itself is leashed, pull the boat directly
                Entity targetEntity = entity.isInsideVehicle() && entity.getVehicle() instanceof Boat ? entity.getVehicle() : entity;

                if (!targetEntity.getWorld().equals(holder.getWorld())) {
                    targetEntity.teleport(holder.getLocation());
                    continue;
                }

                Location targetLoc = targetEntity.getLocation();
                Location holderLoc = holder.getLocation();
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
