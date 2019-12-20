package io.github.the_dwellers.fyreplugin.events;

import io.github.the_dwellers.fyreplugin.util.RandomUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {
	@EventHandler()
	public void onPlayerMove(PlayerMoveEvent event) {

		if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
			// Early exit if blocks were not changed.
			return;
		}

		Player player = event.getPlayer();

		if (player.isOnGround()) {
			// Player is walking
			Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

			if (block.getType() == Material.GRASS_BLOCK) {
				int r = RandomUtil.integer(100);

				if (r <= 5) {
					// 5% Chance
					block.setType(Material.COARSE_DIRT);
				}
			}
		} else if (player.getVehicle() instanceof AbstractHorse) {
			// Player is in a horse
			// instanceof returns false if player.getVehicle() is null or not a horse
			Entity vehicle = player.getVehicle();

			if (vehicle.isOnGround()) {

				Block block = vehicle.getLocation().getBlock().getRelative(BlockFace.DOWN);

				if (block.getType() == Material.GRASS_BLOCK) {
					int r = RandomUtil.integer(100);

					if (r <= 10) {
						// 10% Chance
						block.setType(Material.COARSE_DIRT);
					}
				}
			}
		}
	}
}