package com.nisovin.shopkeepers.shopobjects.entity;

import java.util.List;

import org.bukkit.entity.Entity;

import com.nisovin.shopkeepers.api.shopobjects.entity.EntityShopObjectType;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.AbstractShopObjectType;

/**
 * Base class for {@link EntityShopObjectType}s.
 * 
 * @param <T>
 *            the type of the shop objects this represents
 */
public abstract class AbstractEntityShopObjectType<T extends AbstractEntityShopObject> extends AbstractShopObjectType<T> implements EntityShopObjectType<T> {

	protected AbstractEntityShopObjectType(String identifier, List<String> aliases, String permission, Class<T> shopObjectType) {
		super(identifier, aliases, permission, shopObjectType);
	}

	@Override
	public AbstractShopkeeper getShopkeeper(Entity entity) {
		Object objectId = EntityShopObjectIds.getObjectId(entity);
		return this.getShopkeeperByObjectId(objectId);
	}

	@Override
	public boolean isShopkeeper(Entity entity) {
		return (this.getShopkeeper(entity) != null);
	}
}
