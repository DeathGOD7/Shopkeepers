package com.nisovin.shopkeepers.commands.shopkeepers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.DefaultShopTypes;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopCreationData;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopType;
import com.nisovin.shopkeepers.api.shopobjects.DefaultShopObjectTypes;
import com.nisovin.shopkeepers.api.shopobjects.ShopObjectType;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.arguments.PositiveIntegerArgument;
import com.nisovin.shopkeepers.commands.lib.commands.PlayerCommand;
import com.nisovin.shopkeepers.commands.lib.context.CommandContextView;
import com.nisovin.shopkeepers.text.Text;

class CommandDebugCreateShops extends PlayerCommand {

	private static final String ARGUMENT_SHOP_COUNT = "shopCount";

	private final SKShopkeepersPlugin plugin;

	CommandDebugCreateShops(SKShopkeepersPlugin plugin) {
		super("debugCreateShops");
		this.plugin = plugin;

		// Set permission:
		this.setPermission(ShopkeepersPlugin.DEBUG_PERMISSION);

		// Set description:
		this.setDescription(Text.of("Creates lots of shopkeepers for stress testing."));

		// Hidden debugging command:
		this.setHiddenInParentHelp(true);

		// Arguments:
		this.addArgument(new PositiveIntegerArgument(ARGUMENT_SHOP_COUNT).orDefaultValue(10));
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		assert (input.getSender() instanceof Player);
		Player player = (Player) input.getSender();
		int shopCount = context.get(ARGUMENT_SHOP_COUNT);
		// Not using BoundedIntegerArgument for now due to missing descriptive error messages.
		// TODO Use this in the future.
		if (shopCount > 1000) {
			player.sendMessage(ChatColor.RED + "Shopkeeper count to high, limiting to 1000!");
			shopCount = 1000;
		}

		player.sendMessage(ChatColor.GREEN + "Creating up to " + shopCount
				+ " shopkeepers, starting here!");

		AdminShopType<?> shopType = DefaultShopTypes.ADMIN_REGULAR();
		ShopObjectType<?> shopObjectType = Unsafe.assertNonNull(
				DefaultShopObjectTypes.LIVING().get(EntityType.VILLAGER)
		);

		int created = 0;
		Location curSpawnLocation = player.getLocation();
		for (int i = 0; i < shopCount; i++) {
			Shopkeeper shopkeeper = plugin.handleShopkeeperCreation(AdminShopCreationData.create(
					player,
					shopType,
					shopObjectType,
					curSpawnLocation.clone(),
					null
			));
			curSpawnLocation.add(2, 0, 0);
			if (shopkeeper != null) {
				created++;
			}
		}
		player.sendMessage(ChatColor.GREEN + "Done! Created " + ChatColor.YELLOW + created
				+ ChatColor.GREEN + " shopkeepers!");
	}
}
