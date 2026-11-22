package com.ranull.unbreakableleashes.manager;

import com.ranull.unbreakableleashes.UnbreakableLeashes;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeashManager {
    private final UnbreakableLeashes plugin;

    public LeashManager(UnbreakableLeashes plugin) {
        this.plugin = plugin;
    }

    public void teleportLeashed(LivingEntity livingEntity) {
        getLeashedEntityList(livingEntity).forEach((leashedLivingEntity) ->
                teleportLeashed(livingEntity, leashedLivingEntity));
    }

    public void teleportLeashed(LivingEntity holderLivingEntity, LivingEntity leashedLivingEntity) {
        List<Entity> passengerList = getPassengers(leashedLivingEntity);
        Chunk chunk = leashedLivingEntity.getLocation().getChunk();

        plugin.getLeashManager().addPluginChunkTicket(chunk);

        leashedLivingEntity.setLeashHolder(null);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            leashedLivingEntity.eject();
            leashedLivingEntity.teleport(holderLivingEntity);
            leashedLivingEntity.setLeashHolder(holderLivingEntity);

            for (Entity passengerEntity : passengerList) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    passengerEntity.teleport(leashedLivingEntity, PlayerTeleportEvent.TeleportCause.UNKNOWN);
                    addPassenger(leashedLivingEntity, passengerEntity);
                }, 1L);
            }

            removePluginChunkTicket(chunk);
        }, 3L);
    }

    public void addPluginChunkTicket(Chunk chunk) {
        try {
            chunk.addPluginChunkTicket(plugin);
        } catch (NoSuchMethodError ignored) {
        }
    }

    public void removePluginChunkTicket(Chunk chunk) {
        try {
            chunk.removePluginChunkTicket(plugin);
        } catch (NoSuchMethodError ignored) {
        }
    }

    public boolean isWorldValid(World world) {
        List<String> stringList = plugin.getConfig().getStringList("worlds");

        return stringList.contains("ALL") || stringList.contains(world.getName());
    }

    private List<LivingEntity> getLeashedEntityList(Entity entity) {
        List<LivingEntity> livingEntityList = new ArrayList<>();

        for (Entity nearbyEntity : entity.getWorld().getNearbyEntities(entity.getLocation(), 15, 15, 15)) {
            if (nearbyEntity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) nearbyEntity;

                if (livingEntity.isLeashed() && livingEntity.getLeashHolder().getUniqueId()
                        .equals(entity.getUniqueId())) {
                    livingEntityList.add(livingEntity);
                }
            }
        }

        return livingEntityList;
    }

    @SuppressWarnings("deprecation")
    private List<Entity> getPassengers(Entity entity) {
        try {
            return new ArrayList<>(entity.getPassengers());
        } catch (NoSuchMethodError ignored) {
            return entity.getPassenger() != null ? Collections.singletonList(entity.getPassenger()) : new ArrayList<>();
        }
    }

    @SuppressWarnings("deprecation")
    private void addPassenger(Entity entity, Entity passengerEntity) {
        try {
            entity.addPassenger(passengerEntity);
        } catch (NoSuchMethodError ignored) {
            entity.setPassenger(passengerEntity);
        }
    }
}
