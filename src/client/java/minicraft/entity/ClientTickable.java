/*
 * SPDX-FileCopyrightText: 2011-2024 Minicraft+ Developers and Contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.entity;

public interface ClientTickable extends Tickable {

	default void clientTick() {
		tick();
	}

}
