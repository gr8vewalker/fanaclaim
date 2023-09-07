package me.ahmetflix.claim.listener;

import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.data.Flag;
import me.ahmetflix.claim.data.Flags;
import me.ahmetflix.claim.gui.MenuType;
import me.ahmetflix.claim.message.Messages;
import me.ahmetflix.claim.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;

public class ClaimListener implements Listener {

    @EventHandler(priority= EventPriority.HIGH, ignoreCancelled=true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().isOp()) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getBlock().getLocation(), false, null);
        if (claim == null) return;
        if (claim.ownerID.equals(event.getPlayer().getUniqueId())) return;
        long id = claim.getID();
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(id);
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(id);
        }
        Flags flags = claimData.getFlags(event.getPlayer().getUniqueId());
        boolean could = flags.getFlag(Flag.BUILD);
        Block block = event.getBlock();
        if (block.getType() == Material.BEACON) could = flags.getFlag(Flag.BREAK_BEACON);
        else if (block.getType() == Material.SPAWNER) could = flags.getFlag(Flag.BREAK_SPAWNER);
        else if (block.getType() == Material.HOPPER) could = flags.getFlag(Flag.BUILD_HOPPERS);
        if (!could) {
            Messages.CANT_BREAK.send(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Player damager = Utils.getDamager(event.getDamager());
        if (damager == null) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(player.getLocation(), false, null);
        if (claim == null) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        Messages.CANT_PVP.send(damager);
        event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onClaimCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
        String command = msg.split(" ")[0];
        if (!command.equalsIgnoreCase("/claim")) return;
        Player player = event.getPlayer();
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(player.getLocation(), false, null);
        if (claim == null) return;
        event.setCancelled(true);
        if (!claim.ownerID.equals(player.getUniqueId())) {
            Messages.NOT_OWNER.send(player);
            return;
        }
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        claimData.openMenu(player, MenuType.MAIN_CLAIM);
    }

    @EventHandler
    public void onMonsterSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getEntity().getLocation(), false, null);
        if (claim == null) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        if (!claimData.isMobs()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnimalSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Animals)) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getEntity().getLocation(), false, null);
        if (claim == null) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        if (!claimData.isAnimals()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer().isOp()) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getBlock().getLocation(), false, null);
        if (claim == null) return;
        if (claim.ownerID.equals(event.getPlayer().getUniqueId())) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        Flags flags = claimData.getFlags(event.getPlayer().getUniqueId());
        boolean could = flags.getFlag(Flag.BUILD);
        Block block = event.getBlock();
        if (block.getType() == Material.SPAWNER) could = flags.getFlag(Flag.PLACE_SPAWNER);
        else if (block.getType() == Material.HOPPER) could = flags.getFlag(Flag.BUILD_HOPPERS);
        if (!could) {
            Messages.CANT_PLACE.send(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onFish(PlayerFishEvent event) {
        if (event.getPlayer().isOp()) return;
        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY || event.getCaught() == null) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getCaught().getLocation(), false, null);
        if (claim == null) return;
        if (claim.ownerID.equals(event.getPlayer().getUniqueId())) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        Flags flags = claimData.getFlags(event.getPlayer().getUniqueId());
        boolean could = flags.getFlag(Flag.USE_ROD_ON_ENTITIES);
        if (!could) {
            Messages.CANT_PULL.send(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getPlayer().isOp()) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getBlock().getLocation(), false, null);
        if (claim == null) return;
        if (claim.ownerID.equals(event.getPlayer().getUniqueId())) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        Flags flags = claimData.getFlags(event.getPlayer().getUniqueId());
        boolean could = flags.getFlag(Flag.PLACE_FLUID);
        if (!could) {
            Messages.CANT_PLACE.send(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer().isOp()) return;
        if (!(event.getInventory().getHolder() instanceof BlockInventoryHolder)) return;
        BlockInventoryHolder holder = (BlockInventoryHolder) event.getInventory().getHolder();
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(holder.getBlock().getLocation(), false, null);
        if (claim == null) return;
        if (claim.ownerID.equals(event.getPlayer().getUniqueId())) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        Flags flags = claimData.getFlags(event.getPlayer().getUniqueId());
        boolean could = flags.getFlag(Flag.OPEN_CONTAINERS);
        if (!could) {
            Messages.CANT_OPEN_CONTAINER.send(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPlaceEntity(EntityPlaceEvent event) {
        if (event.getPlayer().isOp()) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getBlock().getLocation(), false, null);
        if (claim == null) return;
        if (claim.ownerID.equals(event.getPlayer().getUniqueId())) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        Flags flags = claimData.getFlags(event.getPlayer().getUniqueId());
        boolean could = flags.getFlag(Flag.PLACE_ENTITIES);
        if (!could) {
            Messages.CANT_PLACE.send(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer().isOp()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getClickedBlock().getLocation(), false, null);
        if (claim == null) return;
        if (claim.ownerID.equals(event.getPlayer().getUniqueId())) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        Flags flags = claimData.getFlags(event.getPlayer().getUniqueId());
        boolean could = true;
        Material type = event.getClickedBlock().getType();
        if (type.name().endsWith("_DOOR")) could = flags.getFlag(Flag.USE_DOORS);
        else if (type.name().endsWith("_BUTTON")
                || type == Material.LEVER
                || type == Material.DAYLIGHT_DETECTOR
                || type == Material.TRAPPED_CHEST) could = flags.getFlag(Flag.TRIGGER_REDSTONE);
        if (!could) {
            Messages.CANT_USE.send(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPhysicalInteract(PlayerInteractEvent event) {
        if (event.getPlayer().isOp()) return;
        if (event.getAction() != Action.PHYSICAL && event.getClickedBlock() != null) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getClickedBlock().getLocation(), false, null);
        if (claim == null) return;
        if (claim.ownerID.equals(event.getPlayer().getUniqueId())) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        Flags flags = claimData.getFlags(event.getPlayer().getUniqueId());
        boolean could = true;
        Material type = event.getClickedBlock().getType();
        if (type == Material.TRIPWIRE_HOOK
                || type == Material.TRIPWIRE
                || type.name().endsWith("_PRESSURE_PLATE")) could = flags.getFlag(Flag.TRIGGER_REDSTONE);
        if (!could) {
            Messages.CANT_USE.send(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitBlock() == null || event.getHitBlock().getType() != Material.TARGET) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();
        if (player.isOp()) return;
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(event.getHitBlock().getLocation(), false, null);
        if (claim == null) return;
        if (claim.ownerID.equals(player.getUniqueId())) return;
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            claimData = FanaClaim.getInstance().getClaimManager().addClaim(claim.getID());
        }
        Flags flags = claimData.getFlags(player.getUniqueId());
        boolean could = flags.getFlag(Flag.TRIGGER_REDSTONE);
        if (!could) {
            Messages.CANT_USE.send(player);
            event.setCancelled(true);
        }
    }

}
