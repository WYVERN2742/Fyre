package com.github.thedwellers.fyreplugin.events;

import com.github.thedwellers.fyreplugin.configuration.PlayerOperations;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
	@EventHandler()
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
<<<<<<< HEAD

		if(!PlayerOperations.playerFileExists(uuid)){
			PlayerOperations.configurePlayerData(uuid);
		}
=======
		
		PlayerOperations.initializePlayerConfiguration(uuid);
>>>>>>> 4a0c3a57af020de911603851cab42fab7d2c2cf1
	}
}
