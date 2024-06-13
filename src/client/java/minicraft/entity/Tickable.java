/*
 * SPDX-FileCopyrightText: 2011-2024 Minicraft+ Developers and Contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.entity;

public interface Tickable {

	/**
	 * Called every frame before Render() is called. Most game functionality in the game is based on this method.
	 */
	void tick();

}
