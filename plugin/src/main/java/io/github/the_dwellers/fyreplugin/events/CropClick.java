package io.github.the_dwellers.fyreplugin.events;

import java.util.Collection;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.the_dwellers.fyreplugin.Reflected;

/**
 * CropClick
 */
public class CropClick implements Listener {

	@EventHandler()
	public void onCropClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}
		Material blockMaterial = block.getType();

		if (blockMaterial == Material.WHEAT || blockMaterial == Material.BEETROOTS || blockMaterial == Material.CARROTS
				|| blockMaterial == Material.POTATOES) {

			Ageable Blockdata = (Ageable) block.getBlockData();
			if (Blockdata.getAge() != Blockdata.getMaximumAge()) {
				return;
			}

			ItemStack right = player.getInventory().getItemInMainHand();
			ItemStack left = player.getInventory().getItemInOffHand();

			Material rightMat = right.getType();
			Material leftMat = left.getType();
			Collection<ItemStack> drops;

			if (rightMat == Material.WOODEN_HOE || rightMat == Material.STONE_HOE || rightMat == Material.IRON_HOE
					|| rightMat == Material.GOLDEN_HOE || rightMat == Material.DIAMOND_HOE) {

				Damageable itemMeta = (Damageable) right.getItemMeta();
				itemMeta.setDamage(itemMeta.getDamage() + 1);

				// ? Why is there no api method to break items?
				// ? Why do items not naturally break?
				if (itemMeta.getDamage() > right.getType().getMaxDurability()) {
					Reflected.breakEquipment((LivingEntity) player, EquipmentSlot.HAND);
				}

				right.setItemMeta((ItemMeta) itemMeta);
				drops = block.getDrops(right);

			} else if (leftMat == Material.WOODEN_HOE || leftMat == Material.STONE_HOE || leftMat == Material.IRON_HOE
					|| leftMat == Material.GOLDEN_HOE || leftMat == Material.DIAMOND_HOE) {

				Damageable itemMeta = (Damageable) left.getItemMeta();
				itemMeta.setDamage(itemMeta.getDamage() + 1);

				if (itemMeta.getDamage() > left.getType().getMaxDurability()) {
					Reflected.breakEquipment((LivingEntity) player, EquipmentSlot.OFF_HAND);
				}

				left.setItemMeta((ItemMeta) itemMeta);
				drops = block.getDrops(left);

			} else {
				return;
			}

			// Spawn Drops
			for (ItemStack item : drops) {
				block.getLocation().getWorld().dropItemNaturally(block.getLocation(), item);
			}

			// Reset block age
			Blockdata.setAge(1);
			block.setBlockData(Blockdata);
		}
	}
}