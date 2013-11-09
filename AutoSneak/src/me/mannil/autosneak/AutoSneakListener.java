package me.mannil.autosneak;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

public class AutoSneakListener
  implements Listener
{
private final AutoSneak plugin;

  public AutoSneakListener(AutoSneak plugin)
  {
    this.plugin = AutoSneak.Instance;
    Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) { 
	  if (event.getPlayer().hasPermission("autosneak.auto")) {
	      AutoSneak.sneakingPlayers.add(event.getPlayer().getName());
	      TagAPI.refreshPlayer(event.getPlayer());
	  }
    } 

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (AutoSneak.sneakingPlayers.contains(event.getPlayer().getName())) {
      AutoSneak.sneakingPlayers.remove(event.getPlayer().getName());
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    if ((!AutoSneak.sneakingPlayers.isEmpty()) && (AutoSneak.sneakingPlayers.contains(event.getPlayer().getName()))) {
      AutoSneak.sneakingPlayers.remove(event.getPlayer().getName());
      TagAPI.refreshPlayer(event.getPlayer());
    }
  }
  @EventHandler
  public void onTagChange(PlayerReceiveNameTagEvent event) {
    Player player = event.getNamedPlayer();
    if ((this.plugin.getConfig().getBoolean("opdefault")) && (player.isOp()))
      event.setTag("§§§§");
    else if (AutoSneak.sneakingPlayers.contains(player.getName()))
      event.setTag("§§§§");
    else
      event.setTag(player.getDisplayName());
  }
}