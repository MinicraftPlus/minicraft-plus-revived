/*
 * SPDX-FileCopyrightText: 2011-2024 Minicraft+ Developers and Contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.item;

import org.jetbrains.annotations.NotNull;

public class PowerGloveItem extends Item {

	public PowerGloveItem() {
		super("Power Glove");
	}

	public @NotNull PowerGloveItem copy() {
		return new PowerGloveItem();
	}
}
