package com.ranull.unbreakableleashes.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldGuard {
    private final JavaPlugin plugin;
    private final com.sk89q.worldguard.WorldGuard worldGuard;
    private final StateFlag teleportFlag;

    public WorldGuard(JavaPlugin plugin) {
        this.plugin = plugin;
        this.worldGuard = com.sk89q.worldguard.WorldGuard.getInstance();
        this.teleportFlag = registerTeleportFlag();
    }

    public boolean shouldTeleport(Location location) {
        return teleportFlag == null || worldGuard.getPlatform().getRegionContainer().createQuery()
                .testState(BukkitAdapter.adapt(location), null, teleportFlag);
    }

    private StateFlag registerTeleportFlag() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            Flag<?> flag = worldGuard.getFlagRegistry().get("unbreakableleashes");

            if (flag instanceof StateFlag) {
                return (StateFlag) flag;
            }
        } else {
            try {
                StateFlag flag = new StateFlag("unbreakableleashes", true);

                worldGuard.getFlagRegistry().register(flag);

                return flag;
            } catch (FlagConflictException exception) {
                Flag<?> flag = worldGuard.getFlagRegistry().get("unbreakableleashes");

                if (flag instanceof StateFlag) {
                    return (StateFlag) flag;
                }
            }
        }

        return null;
    }
}
