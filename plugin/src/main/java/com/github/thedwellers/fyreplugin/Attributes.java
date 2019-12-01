package com.github.thedwellers.fyreplugin;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Static class for applying standard entity and item attributes.
 */
public abstract class Attributes {

	/**
	 * Update player attributes.
	 *
	 * @param player Player to update attributes of
	 */
	public static void applyPlayer(Player player) {
		// 5 hearts of health
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(10);
	}

	public static void applyMob(Entity entity) {
		if (entity instanceof Attributable) {
			switch (entity.getType()) {
				case ZOMBIE:
				case SKELETON:
				case SPIDER:
				case ENDERMAN:
					applyOverworldHostile((Attributable) entity);
					break;
				case BLAZE:
				case GHAST:
				case WITHER_SKELETON:
					applyNetherHostile((Attributable) entity);
				default:
					break;
			}
		}
	}

	public static void applyOverworldHostile(Attributable entity) {
		entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)
				.addModifier(new AttributeModifier("Fyre Spawn Health 1.5", 0.5, Operation.MULTIPLY_SCALAR_1));
	}

	public static void applyNetherHostile(Attributable entity) {
		entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)
				.addModifier(new AttributeModifier("Fyre Spawn Health 2", 1, Operation.MULTIPLY_SCALAR_1));

	}

}
