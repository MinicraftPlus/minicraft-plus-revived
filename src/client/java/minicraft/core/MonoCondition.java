/*
 * SPDX-FileCopyrightText: 2011-2024 Minicraft+ contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.core;

@FunctionalInterface
public interface MonoCondition<T> {
	boolean check(T arg);
}
