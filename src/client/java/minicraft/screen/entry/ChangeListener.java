/*
 * SPDX-FileCopyrightText: 2011-2024 Minicraft+ contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.screen.entry;

@FunctionalInterface
public interface ChangeListener {
	void onChange(Object newValue);
}
