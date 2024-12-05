/*
 * SPDX-FileCopyrightText: 2024 Minicraft+ contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.util;

import minicraft.core.Game;
import minicraft.entity.Entity;
import minicraft.gfx.Point;
import minicraft.item.Item;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageSource {
	/*
	 * If the source with a particular type has certain data info available, then the instances must have
	 * such data info available, may be not null, except item, but other info must be null, otherwise there could
	 * be unexpected errors.
	 * The position is either of tile or directEntity, but all in entity coordinates (center of tile for tiles).
	 * Available data info:
	 * - tile: the tile type as the source
	 * - causingEntity: the entity causing the damage, aiming the target
	 * - directEntity: the entity directly causes damage to the target
	 * - item: the item the direct entity was holding to cause damage
	 */

	public enum DamageType {
		/** Data: tile or (item, causingEntity and directEntity) */
		GENERIC,
		/** Fire tile; Data: tile */
		IN_FIRE,
		/** When on fire, original source must refer to last attack source; Data: (causingEntity+directEntity) or tile */
		ON_FIRE,
		/** Lava tile; Data: tile */
		LAVA,
		/** Data: causingEntity and directEntity */
		EXPLOSION,
		/** Data: N/A */
		DROWN,
		/** Data: N/A */
		STARVE,
		/** Data: (causingEntity or tile) and directEntity */
		ARROW,
		/** Data: causingEntity and directEntity */
		SPARK,
		/** Heat damage by floor tile; Data: tile */
		HOT_FLOOR,
		/** Data: tile */
		CACTUS;

		public boolean isFireRelated() {
			return this == IN_FIRE || this == ON_FIRE || this == LAVA || this == HOT_FLOOR;
		}
	}

	private final @NotNull DamageType damageType;
	private final @NotNull Level level;
	private final int x, y;
	private final @Nullable Tile tile;
	private final @Nullable Entity causingEntity, directEntity;
	private final @Nullable Item item;

	public DamageSource(@NotNull DamageType damageType, @NotNull Level level, int x, int y, @Nullable Tile tile,
	                    @Nullable Entity causingEntity, @Nullable Entity directEntity, @Nullable Item item) {
		this.damageType = damageType;
		this.level = level;
		this.x = x;
		this.y = y;
		this.tile = tile;
		this.causingEntity = causingEntity;
		this.directEntity = directEntity;
		this.item = item;
	}

	public DamageSource(DamageType damageType, @NotNull Level level, int x, int y,
	                    @NotNull Tile tile, @NotNull Entity directEntity, @Nullable Item item) {
		this(damageType, level, x, y, tile, null, directEntity, item);
	}

	public DamageSource(@NotNull DamageType damageType, @NotNull Level level, int x, int y, @NotNull Tile tile) {
		this(damageType, level, x, y, tile, null, null, null);
	}

	public DamageSource(@NotNull DamageType damageType, @NotNull Entity causingEntity,
	                    @NotNull Entity directEntity, @Nullable Item item) {
		this(damageType, directEntity.getLevel(), directEntity.x, directEntity.y, null, causingEntity, directEntity, item);
	}

	public DamageSource(@NotNull DamageType damageType, @NotNull Entity entity, @Nullable Item item) {
		this(damageType, entity, entity, item);
	}

	public DamageSource(@NotNull DamageType damageType) {
		this(damageType, Game.player, null); // placeholder
	}

	public @NotNull DamageType getDamageType() {
		return damageType;
	}

	@NotNull
	public Level getLevel() {
		return level;
	};

	public Point getTilePosition() {
		return new Point(x >> 4, y >> 4);
	}

	/** Get position of damage source based on entity coordinates. */
	@NotNull
	public Point getPosition() {
		return new Point(x, y);
	};

	public @Nullable Tile getTile() {
		return tile;
	}

	public @Nullable Entity getCausingEntity() {
		return causingEntity;
	}

	public @Nullable Entity getDirectEntity() {
		return directEntity;
	}

	public @Nullable Item getItem() {
		return item;
	}
}
