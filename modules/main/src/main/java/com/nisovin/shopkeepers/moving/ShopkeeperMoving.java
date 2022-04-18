package com.nisovin.shopkeepers.moving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.events.ShopkeeperEditedEvent;
import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.input.InputRequest;
import com.nisovin.shopkeepers.input.interaction.InteractionInput;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.shopcreation.ShopkeeperPlacement;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopType;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.AbstractShopObjectType;
import com.nisovin.shopkeepers.util.bukkit.TextUtils;
import com.nisovin.shopkeepers.util.java.Validate;
import com.nisovin.shopkeepers.util.logging.Log;

public class ShopkeeperMoving {

	private class ShopkeeperLocationRequest implements InteractionInput.Request {

		private final ShopkeeperPlacement shopkeeperPlacement;
		private final Player player;
		private final Shopkeeper shopkeeper;

		ShopkeeperLocationRequest(
				ShopkeeperPlacement shopkeeperPlacement,
				Player player,
				Shopkeeper shopkeeper
		) {
			assert shopkeeperPlacement != null;
			assert player != null;
			assert shopkeeper != null;
			this.shopkeeperPlacement = shopkeeperPlacement;
			this.player = player;
			this.shopkeeper = shopkeeper;
		}

		private boolean isAbortAction(Action action) {
			// Note: Right-click air is not called if the player is not holding any item in their
			// hand. We therefore completely ignore it.
			return action == Action.LEFT_CLICK_AIR
					|| action == Action.LEFT_CLICK_BLOCK;
		}

		@Override
		public boolean accepts(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (this.isAbortAction(action)) {
				return true;
			}

			if (action != Action.RIGHT_CLICK_BLOCK) {
				return false;
			}

			// Ignore interactions with certain types of blocks:
			Block clickedBlock = Unsafe.assertNonNull(event.getClickedBlock());
			if (shopkeeperPlacement.isInteractionIgnored(clickedBlock.getType())) {
				Log.debug(() -> "Shopkeeper moving: Ignoring interaction with block of type "
						+ clickedBlock.getType());
				return false;
			}

			return true;
		}

		@Override
		public void onInteract(PlayerInteractEvent event) {
			Player player = event.getPlayer();
			if (this.isAbortAction(event.getAction())) {
				this.onAborted();
				return;
			}

			event.setCancelled(true);

			Block clickedBlock = Unsafe.assertNonNull(event.getClickedBlock());
			BlockFace clickedBlockFace = event.getBlockFace();

			ShopkeeperPlacement shopkeeperPlacement = SKShopkeepersPlugin.getInstance()
					.getShopkeeperCreation()
					.getShopkeeperPlacement();
			Location spawnLocation = shopkeeperPlacement.determineSpawnLocation(
					player,
					clickedBlock,
					clickedBlockFace
			);

			requestMove(player, shopkeeper, spawnLocation, clickedBlockFace);
		}

		@Override
		public void onAborted() {
			TextUtils.sendMessage(player, Messages.shopkeeperMoveAborted);
		}
	}

	private final InteractionInput interactionInput;
	private final ShopkeeperPlacement shopkeeperPlacement;

	public ShopkeeperMoving(
			InteractionInput interactionInput,
			ShopkeeperPlacement shopkeeperPlacement
	) {
		Validate.notNull(interactionInput, "interactionInput is null");
		Validate.notNull(shopkeeperPlacement, "shopkeeperPlacement is null");
		this.interactionInput = interactionInput;
		this.shopkeeperPlacement = shopkeeperPlacement;
	}

	public void onEnable() {
	}

	public void onDisable() {
	}

	public void startMoving(Player player, Shopkeeper shopkeeper) {
		Validate.notNull(player, "player is null");
		Validate.notNull(shopkeeper, "shopkeeper is null");
		interactionInput.request(
				player,
				new ShopkeeperLocationRequest(shopkeeperPlacement, player, shopkeeper)
		);
	}

	public void abortMoving(Player player) {
		Validate.notNull(player, "player is null");
		InputRequest<@NonNull Event> request = interactionInput.getRequest(player);
		if (request instanceof ShopkeeperLocationRequest) {
			interactionInput.abortRequest(player, request);
		}
	}

	public boolean requestMove(
			Player player,
			Shopkeeper shopkeeper,
			Location newLocation,
			BlockFace blockFace
	) {
		Validate.notNull(player, "player is null");
		Validate.notNull(shopkeeper, "shopkeeper is null");
		Validate.notNull(newLocation, "newLocation is null");
		if (!shopkeeper.isValid()) return false;

		// Validate the new spawn location:
		boolean isSpawnLocationValid = shopkeeperPlacement.validateSpawnLocation(
				player,
				(AbstractShopType<?>) shopkeeper.getType(),
				(AbstractShopObjectType<?>) shopkeeper.getShopObject().getType(),
				newLocation,
				blockFace,
				null,
				(AbstractShopkeeper) shopkeeper
		);
		if (!isSpawnLocationValid) {
			TextUtils.sendMessage(player, Messages.shopkeeperMoveAborted);
			return false;
		}

		// Move the shopkeeper:
		((AbstractShopkeeper) shopkeeper).teleport(newLocation);

		// Inform the player:
		TextUtils.sendMessage(player, Messages.shopkeeperMoved);

		// Call an event:
		Bukkit.getPluginManager().callEvent(new ShopkeeperEditedEvent(shopkeeper, player));

		// Save the shopkeeper:
		shopkeeper.save();
		return true;
	}
}
