/*
 * SPDX-FileCopyrightText: 2011-2024 Minicraft+ contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.item;

import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.SpriteType;
import org.jetbrains.annotations.NotNull;

public class UnknownItem extends StackableItem {

	protected UnknownItem(String reqName) {
		super(reqName, SpriteLinker.missingTexture(SpriteType.Item));
	}

	public @NotNull UnknownItem copy() {
		return new UnknownItem(getName());
	}
}
