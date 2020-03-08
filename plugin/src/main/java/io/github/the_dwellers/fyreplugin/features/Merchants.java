package io.github.the_dwellers.fyreplugin.features;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Villager.Type;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import io.github.the_dwellers.fyreplugin.FyrePlugin;
import io.github.the_dwellers.fyreplugin.Reflected;
import io.github.the_dwellers.fyreplugin.Feature;
import io.github.the_dwellers.fyreplugin.commands.AbstractCommand;
import io.github.the_dwellers.fyreplugin.configuration.MerchantRecipes;
import io.github.the_dwellers.fyreplugin.configuration.SupportedVersions;
import io.github.the_dwellers.fyreplugin.exceptions.ReflectionFailedException;
import io.github.the_dwellers.fyreplugin.util.MinecraftVersion;

/**
 * Merchants
 */
public class Merchants extends Feature implements Listener {

	public static MinecraftVersion minVersion = SupportedVersions.MC1144;

	protected boolean enabled = false;
	protected static String name = "Merchants";
	private static Merchants instance;

	private static boolean merchantShowXP;

	public static Merchants getInstance() {
		if (instance == null) {
			instance = new Merchants();
		}
		return instance;
	}

	@Override
	public MinecraftVersion getMinecraftVersion() {
		return minVersion;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean setup(FyrePlugin plugin) {
		if (!ItemFeatures.getInstance().isEnabled()) {
			return false;
		}

		if (Reflected.getClass("IMaterial") == null) {
			if (!Reflected.cacheClass(Reflected.nmsClass+"IMaterial")) {
				plugin.getLogger().warning("Unable to Cache IMaterial");
				return false;
			}
		}

		if (Reflected.getClass("IMerchant") == null) {
			if (!Reflected.cacheClass(Reflected.nmsClass+"IMerchant")) {
				plugin.getLogger().warning("Unable to Cache IMerchant");
				return false;
			}
		}

		if (Reflected.getClass("CraftMerchantCustom") == null) {
			if (!Reflected.cacheClass(Reflected.obcClass + "inventory.CraftMerchantCustom")) {
				plugin.getLogger().warning("Unable to Cache CraftMerchantCustom");
				return false;
			}
		}
		if (Reflected.getClass("MinecraftMerchant") == null) {
			if (!Reflected.cacheClass(Reflected.obcClass + "inventory.CraftMerchantCustom$MinecraftMerchant")) {
				plugin.getLogger().warning("Unable to Cache CraftMerchantCustom$MinecraftMerchant");
				return false;
			}
		}
		if (Reflected.getClass("CraftHumanEntity") == null) {
			if (!Reflected.cacheClass(Reflected.obcClass + "entity.CraftHumanEntity")) {
				plugin.getLogger().warning("Unable to Cache CraftHumanEntity");
				return false;
			}
		}

		if (Reflected.getClass("EntityHuman") == null) {
			if (!Reflected.cacheClass(Reflected.nmsClass + "EntityHuman")) {
				plugin.getLogger().warning("Unable to Cache EntityHuman");
				return false;
			}
		}

		if (Reflected.getClass("CraftEntity") == null) {
			if (!Reflected.cacheClass(Reflected.obcClass + "entity.CraftEntity")) {
				plugin.getLogger().warning("Unable to Cache CraftEntity");
				return false;
			}
		}

		if (Reflected.getClass("IChatBaseComponent") == null) {
			if (!Reflected.cacheClass(Reflected.nmsClass + "IChatBaseComponent")) {
				plugin.getLogger().warning("Unable to Cache IChatBaseComponent");
				return false;
			}
		}

		if (Reflected.getMethod("IMerchant#openTrade") == null) {
			if (!Reflected.cacheClassMethod("IMerchant#openTrade", Reflected.getClass("EntityHuman"), Reflected.getClass("IChatBaseComponent"), int.class)) {;
				plugin.getLogger().warning("Unable to Cache IMerchant#openTrade");
				return false;
			}
		}

		if (Reflected.getMethod("IMerchant#setTradingPlayer") == null) {
			if (!Reflected.cacheClassMethod("IMerchant#setTradingPlayer", Reflected.getClass("EntityHuman"))) {;
				plugin.getLogger().warning("Unable to Cache IMerchant#setTradingPlayer");
				return false;
			}
		}

		if (Reflected.getMethod("MinecraftMerchant#getScoreboardDisplayName") == null) {
			if (!Reflected.cacheClassMethod("MinecraftMerchant#getScoreboardDisplayName")) {;
				plugin.getLogger().warning("Unable to Cache MinecraftMerchant#getScoreboardDisplayName");
				return false;
			}
		}

		if (Reflected.getMethod("CraftEntity#getHandle") == null) {
			if (!Reflected.cacheClassMethod("CraftEntity#getHandle")) {;
				plugin.getLogger().warning("Unable to Cache CraftEntity#getHandle");
				return false;
			}
		}

		if (Reflected.getMethod("CraftMerchantCustom#getMerchant") == null) {
			if (!Reflected.cacheClassMethod("CraftMerchantCustom#getMerchant")) {;
				plugin.getLogger().warning("Unable to Cache CraftMerchantCustom#getMerchant");
				return false;
			}
		}

		// Check patch status
		try {
			Reflected.getClass("MinecraftMerchant").getDeclaredField("experience");
			Reflected.getClass("MinecraftMerchant").getDeclaredField("regularVillager");
			merchantShowXP = true;
		} catch (NoSuchFieldException e) {
			plugin.getLogger().warning("Server jar is not patched with merchant fixes. XP and level bars will be disabled on traders.");
			if (Development.getInstance().isEnabled()) {
				plugin.getLogger().warning("Unable to find experience and regularVillager fields on " + Reflected.obcClass
					+ "inventory.CraftMerchantCustom$MinecraftMerchant");
			}
			merchantShowXP = false;
		}

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getCommand("list").setExecutor(new TraderCommand());

		enabled = true;
		return isEnabled();
	}

	public static void showMerchantUI(Player player, Profession profession, Type type) {
		Merchant merchant = Bukkit.getServer().createMerchant(getName(profession));

		// TODO: Replace with player's levels
		int level = 1;
		int xp = 0;

		merchant.setRecipes(get(profession, level, type));

		try {
			showTraderUI(player, merchant, level, xp);
		} catch (ReflectionFailedException e) {
			e.printStackTrace();
		}
	}

	public static String getName(Profession profession) {
		switch (profession) {
			case ARMORER:
				return "Armorer";
			case BUTCHER:
				return "Butcher";
			case CARTOGRAPHER:
				return "Antique Seller";
			case CLERIC:
				return "Potioneer";
			case FARMER:
				return "Farming";
			case FISHERMAN:
				return "Fisherman";
			case FLETCHER:
				return "Archer";
			case LEATHERWORKER:
				return "";
			case LIBRARIAN:
				return "Researcher";
			case MASON:
				return "Stone Mason";
			case NITWIT:
				return "NitWit";
			case NONE:
				return "Villager";
			case SHEPHERD:
				return "Stableman";
			case TOOLSMITH:
				return "Toolsmith";
			case WEAPONSMITH:
				return "Swordsman";
			default:
				return "";
		}
	}

	public static ArrayList<MerchantRecipe> get(Profession profession, int level, Type type) {
		ArrayList<MerchantRecipe> recipes = new ArrayList<MerchantRecipe>();

		switch (profession) {
			case TOOLSMITH:
				switch (level) {
					case 1:
						recipes.add(MerchantRecipes.buyWoodAxe());
						recipes.add(MerchantRecipes.buyWoodSpade());
						recipes.add(MerchantRecipes.buyWoodPickaxe());
						recipes.add(MerchantRecipes.learnWoodenTools());
						break;
					default:
						break;
				}
				break;
			case MASON:
				switch (level) {
					case 2:
						switch (type) {
							case JUNGLE:
								recipes.add(MerchantRecipes.buyJungleLog());
								break;
							case PLAINS:
								recipes.add(MerchantRecipes.buyBirchLog());
								recipes.add(MerchantRecipes.buyDarkOakLog());
							case SWAMP:
								recipes.add(MerchantRecipes.buyOakLog());
								break;
							case SAVANNA:
								recipes.add(MerchantRecipes.buyAcaciaLog());
								break;
							case TAIGA:
								recipes.add(MerchantRecipes.buySpruceLog());
								break;
							case DESERT:
							case SNOW:
								break;
							default:
								break;
						}
					default:
						switch (type) {
							case JUNGLE:
								recipes.add(MerchantRecipes.sellJungleLog());
								break;
							case PLAINS:
								recipes.add(MerchantRecipes.sellBirchLog());
								recipes.add(MerchantRecipes.sellDarkOakLog());
							case SWAMP:
								recipes.add(MerchantRecipes.sellOakLog());
								break;
							case SAVANNA:
								recipes.add(MerchantRecipes.sellAcaciaLog());
								break;
							case TAIGA:
								recipes.add(MerchantRecipes.sellSpruceLog());
								break;
							case DESERT:
							case SNOW:
								break;
							default:
								break;
						}
				}
				break;
			case WEAPONSMITH:
				switch (level) {
					case 1:
						recipes.add(MerchantRecipes.buyWoodSword());
						recipes.add(MerchantRecipes.learnWoodenSword());
						break;
					default:
						break;
				}
			case NONE:
				recipes.add(MerchantRecipes.sellSplinters());
				recipes.add(MerchantRecipes.learnCraftingTable());
				break;
			case ARMORER:
				recipes.add(MerchantRecipes.sellCoal());
				recipes.add(MerchantRecipes.buyLeatherHelmet());
				recipes.add(MerchantRecipes.buyLeatherChestPlate());
				recipes.add(MerchantRecipes.buyLeatherLeggings());
				recipes.add(MerchantRecipes.buyLeatherBoots());

				recipes.add(MerchantRecipes.buyChainmailHelmet());
				recipes.add(MerchantRecipes.buyChainmailChestPlate());
				recipes.add(MerchantRecipes.buyChainmailLeggings());
				recipes.add(MerchantRecipes.buyChainmailBoots());

				recipes.add(MerchantRecipes.buyIronHelmet());
				recipes.add(MerchantRecipes.buyIronChestPlate());
				recipes.add(MerchantRecipes.buyIronLeggings());
				recipes.add(MerchantRecipes.buyIronBoots());

				break;
			default:
				break;
		}
		return recipes;
	}

	/**
	 * Open a merchant trade window using custom xp and level amounts.
	 * <p>
	 * <b> ! Displaying xp and level amounts currently relies on a patch to the
	 * server jar! </b> If {@link Reflected#setupCache(Logger)} failed to find
	 * patched fields, no alterations are made.
	 * <p>
	 * Due to the dependence on reflection, this method may fail entirely depending
	 * on the Minecraft version. Please check {@link Reflected} for supported
	 * versions.
	 *
	 * <pre>
	 * org.bukkit.craftbukkit.CraftMerchantCustom mcMerchant = ((org.bukkit.craftbukkit.inventory.CraftMerchantCustom) merchant).getMerchant();
	 * org.bukkit.craftbukkit.inventory.CraftMerchantCustom.MinecraftMerchant mcMinecraftMerchant = (org.bukkit.craftbukkit.inventory.CraftMerchantCustom.MinecraftMerchant) mcMerchant
	 * net.minecraft.server.IChatBaseComponent name = mcMerchant.getScoreboardDisplayName();
	 * org.bukkit.entity.CraftHumanEntity playerEntity = (org.bukkit.entity.CraftHumanEntity) player
	 * mcMerchant.setTradingPlayer(playerEntity.getHandle());
	 * mcMerchant.openTrade(playerEntity, name, level);
	 * </pre>
	 *
	 * @param player   Player to display interface to
	 * @param merchant Merchant inventory with trades
	 * @param level    Level of merchant (1 - 5)
	 * @param xp       Experience of trader
	 * @throws ReflectionFailedException thrown when any exception is encountered
	 *                                   during reflection
	 */
	public static void showTraderUI(Player player, Merchant merchant, int level, int xp)
			throws ReflectionFailedException {
		try {
			Class<?> bukkitCraftMerchantCustom = Reflected.getClass("CraftMerchantCustom");
			Class<?> bukkitCraftMinecraftMerchant = Reflected.getClass("MinecraftMerchant");
			Class<?> bukkitCraftHumanEntity = Reflected.getClass("CraftHumanEntity");
			Method getScoreboardDisplayName = Reflected.getMethod("MinecraftMerchant#getScoreboardDisplayName");
			Method setTradingPlayer = Reflected.getMethod("IMerchant#setTradingPlayer");
			Method getHandle = Reflected.getMethod("CraftEntity#getHandle");
			Method openTrade = Reflected.getMethod("IMerchant#openTrade");
			Method getMerchant = Reflected.getMethod("CraftMerchantCustom#getMerchant");

			// org.bukkit.craftbukkit.CraftMerchantCustom mcMerchant =
			// ((org.bukkit.craftbukkit.inventory.CraftMerchantCustom)
			// merchant).getMerchant();
			Object mcMerchant = getMerchant.invoke(bukkitCraftMerchantCustom.cast(merchant));

			// org.bukkit.craftbukkit.inventory.CraftMerchantCustom.MinecraftMerchant
			// mcMinecraftMerchant =
			// (org.bukkit.craftbukkit.inventory.CraftMerchantCustom.MinecraftMerchant)
			// mcMerchant
			Object mcMinecraftMerchant = bukkitCraftMinecraftMerchant.cast(mcMerchant);

			// net.minecraft.server.IChatBaseComponent name =
			// mcMerchant.getScoreboardDisplayName();
			Object name = getScoreboardDisplayName.invoke(mcMinecraftMerchant);

			// org.bukkit.entity.CraftHumanEntity playerEntity =
			// (org.bukkit.entity.CraftHumanEntity) player
			Object playerEntity = getHandle.invoke(bukkitCraftHumanEntity.cast(player));

			if (merchantShowXP) {
				// Change CraftMerchantCustom fields to represent xp
				Field experience = bukkitCraftMinecraftMerchant.getDeclaredField("experience");
				experience.setAccessible(true);
				experience.set(mcMinecraftMerchant, xp);

				Field regularVillager = bukkitCraftMinecraftMerchant.getDeclaredField("regularVillager");
				regularVillager.setAccessible(true);
				regularVillager.set(mcMinecraftMerchant, true);
			}

			// mcMerchant.setTradingPlayer(playerEntity.getHandle());
			setTradingPlayer.invoke(mcMerchant, playerEntity);

			// mcMerchant.openTrade(playerEntity, name, level);
			openTrade.invoke(mcMerchant, playerEntity, name, level);

		} catch (LinkageError | IllegalAccessException | NoSuchFieldException | InvocationTargetException e) {
			throw new ReflectionFailedException(e);
		}
	}

	@EventHandler()
	public void onTraderClick(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() != EntityType.VILLAGER) {
			return;
		}

		event.setCancelled(true);
		Villager villager = (Villager) event.getRightClicked();

		showMerchantUI(event.getPlayer(), villager.getProfession(), villager.getVillagerType());
	}

	/**
	 * Simple command for testing
	 */
	public class TraderCommand extends AbstractCommand {
		@Override
		public String getPermission() {
			return "fyre.trader.use";
		}

		@Override
		public boolean execute(CommandSender sender, Command command, String label, String[] args) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length != 1) {
					player.sendMessage("No arg 1");
					return true;
				}
				Villager villager = (Villager) player.getLocation().getWorld().spawnEntity(player.getEyeLocation(),
						EntityType.VILLAGER);
				villager.setProfession(Profession.valueOf(args[0]));
			}
			return true;
		}
	}
}
