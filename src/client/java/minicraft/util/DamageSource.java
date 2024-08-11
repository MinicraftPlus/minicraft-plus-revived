/*
 * SPDX-FileCopyrightText: 2024 Minicraft+ contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.util;

import minicraft.entity.Entity;
import minicraft.gfx.Point;
import minicraft.item.Item;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DamageSource {
	@NotNull
	public abstract Level getLevel();

	/** Get position of damage source based on entity coordinates. */
	@NotNull
	public abstract Point getPosition();

	public static class TileDamageSource extends DamageSource {
		private final @NotNull Tile tile;
		private final @NotNull Level level;
		private final int x, y;

		public TileDamageSource(@NotNull Tile tile, @NotNull Level level, int x, int y) {
			this.tile = tile;
			this.level = level;
			this.x = x;
			this.y = y;
		}

		public @NotNull Tile getTile() {
			return tile;
		}

		@Override
		public @NotNull Level getLevel() {
			return level;
		}

		@Override
		public @NotNull Point getPosition() {
			return new Point((x << 4) + 8, (y << 4) + 8);
		}
	}

	public static class EntityDamageSource extends DamageSource {
		private final @NotNull Entity entity;
		private final @Nullable Item item;

		public EntityDamageSource(@NotNull Entity entity, @Nullable Item item) {
			this.entity = entity;
			this.item = item;
		}

		public @NotNull Entity getEntity() {
			return entity;
		}

		/** Gets the item the entity is holding */
		public @Nullable Item getItem() {
			return item;
		}

		@Override
		public @NotNull Level getLevel() {
			return entity.getLevel();
		}

		@Override
		public @NotNull Point getPosition() {
			return new Point(entity.x, entity.y);
		}
	}

	public static class OtherDamageSource extends DamageSource {
		private final @NotNull Level level;
		private final int x, y;
		private final @NotNull DamageType damageType;
		private final @Nullable Entity causingEntity;

		public enum DamageType {
			IN_FIRE, ON_FIRE, LAVA, EXPLOSION, DRONE, STARVE, ARROW, SPARK, LAVA_BRICK;

			public boolean isFireRelated() {
				return this == IN_FIRE || this == ON_FIRE || this == LAVA || this == LAVA_BRICK;
			}
		}

		public OtherDamageSource(@NotNull DamageType damageType, @NotNull Level level, int x, int y) {
			this.damageType = damageType;
			this.level = level;
			this.x = x;
			this.y = y;
			this.causingEntity = null;
		}

		public OtherDamageSource(@NotNull DamageType damageType, @NotNull Entity causingEntity) {
			this.damageType = damageType;
			this.causingEntity = causingEntity;
			this.level = causingEntity.getLevel();
			this.x = causingEntity.x;
			this.y = causingEntity.y;
		}

		public @Nullable Entity getCausingEntity() {
			return causingEntity;
		}

		public @NotNull DamageType getDamageType() {
			return damageType;
		}

		@Override
		public @NotNull Level getLevel() {
			return level;
		}

		@Override
		public @NotNull Point getPosition() {
			return new Point(x, y);
		}
	}
}
