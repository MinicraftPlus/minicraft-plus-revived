/*
 * SPDX-FileCopyrightText: 2024 Minicraft+ contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.item;

/**
 * A general inventory implementation with fixed size (number of slots).
 */
public class FixedInventory extends BoundedInventory {
	public static final int DEFAULT_SIZE = 27;

	protected final int maxSlots;

	public FixedInventory() { this(DEFAULT_SIZE); }
	public FixedInventory(int maxSlots) {
		this.maxSlots = maxSlots;
	}

	@Override
	public int getMaxSlots() {
		return maxSlots;
	}
}
