package me.mannil.autosneak;

import net.h31ix.anticheat.api.AnticheatAPI;
import net.h31ix.anticheat.manage.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class AutoSneakListener
  implements Listener
{
  @SuppressWarnings("unused")
private final AutoSneak plugin;

  public AutoSneakListener(AutoSneak plugin)
  {
    this.plugin = AutoSneak.Instance;
    Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) { if (event.getPlayer().hasPermission("autosneak.auto")) {
      if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
        AnticheatAPI.exemptPlayer(event.getPlayer(), CheckType.SNEAK);
      }
      event.getPlayer().setSneaking(true);
      AutoSneak.sneakingPlayers.add(event.getPlayer().getName());
    } }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (AutoSneak.sneakingPlayers.contains(event.getPlayer().getName())) {
      AutoSneak.sneakingPlayers.remove(event.getPlayer().getName());
      if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null)
        AnticheatAPI.unexemptPlayer(event.getPlayer(), CheckType.SNEAK); 
    }
  }

  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    if (!AutoSneak.sneakingPlayers.isEmpty()) {
      if (AutoSneak.debugging.booleanValue()) {
        AutoSneak.debug("sneakingPlayers Count: " + AutoSneak.sneakingPlayers.size());
      }
      if (AutoSneak.sneakingPlayers.contains(event.getPlayer().getName())) {
        if (AutoSneak.debugging.booleanValue()) {
          AutoSneak.debug("sneakingPlayers contains name: " + event.getPlayer().getName());
        }
        if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
          AnticheatAPI.exemptPlayer(event.getPlayer(), CheckType.SNEAK);
        }
        event.getPlayer().setSneaking(true);
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    if ((!AutoSneak.sneakingPlayers.isEmpty()) && (AutoSneak.sneakingPlayers.contains(event.getPlayer().getName()))) {
      AutoSneak.sneakingPlayers.remove(event.getPlayer().getName());
      event.getPlayer().setSneaking(false);
      if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null)
        AnticheatAPI.unexemptPlayer(event.getPlayer(), CheckType.SNEAK);
    }
  }
}