package com.github.thedwellers.fyreplugin.events;

import com.github.thedwellers.fyreplugin.Attributes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

/**
 * EntitySpawn
 */
public class EntitySpawn implements Listener {

	@EventHandler()
	private void onEntitySpawn(EntitySpawnEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			Attributes.applyMob(event.getEntity());
		}
	}

}
