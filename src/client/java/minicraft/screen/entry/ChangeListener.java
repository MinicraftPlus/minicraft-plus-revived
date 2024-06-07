/*
 * Copyright (c) 2024 Minicraft+ Developers and Contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.screen.entry;

@FunctionalInterface
public interface ChangeListener {
	void onChange(Object newValue);
}
