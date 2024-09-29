/*
 * SPDX-FileCopyrightText: 2024 Minicraft+ contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.util;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Supplier;

/**
 * A display object that is used for rendering text in various implementations.
 * <p>
 * There are several scenarios where which kind of display string to use.
 * <ul>
 *     <li> {@link StaticString Static String}: the simplest display string where
 *          it is logically just a {@code String} (plane string)
 *     <li> {@link ArgString Argumented String}: the string is supplied with arguments and
 *          the parameterized string is being provided
 *     <li> Buffered (Argumented) String: the string where arguments may be updated
 *     <li> Dynamic (Argumented) String: the string where arguments are frequently updated and the rendered,
 *          causing buffering unnecessary
 *     <li> (Argumented) Fixed String: the string where the parameterized string is fixed
 * </ul>
 * {@link OnRequestString OnRequestString} could be used if the argument is not barely
 * simply just an object storing information.
 */
public abstract class DisplayString {
	public abstract String toString();

	public static class StaticString extends DisplayString {
		private final @NotNull String str;

		public StaticString(@NotNull String str) {
			this.str = str;
		}

		@Override
		public @NotNull String toString() {
			return str;
		}
	}

	public static abstract class ArgString extends DisplayString {
		private final @NotNull Object[] args;

		protected ArgString(Object @NotNull ...args) {
			this.args = args;
		}

		// Param string can be dynamic.
		protected abstract @NotNull String getParameterizedString();

		@Override
		public @NotNull String toString() {
			return String.format(getParameterizedString(), args);
		}
	}

	public static DisplayString staticArgString(@NotNull String str, Object @NotNull ...args) {
		return new StaticString(String.format(str, args));
	}

	public static abstract class BufArgString extends ArgString {
		private String buf = null;

		public BufArgString(Object @NotNull ...args) {
			super(args);
		}

		private @NotNull String proceed() {
			return buf = super.toString();
		}

		public void refreshBuf() {
			buf = super.toString();
		}

		@Override
		public @NotNull String toString() {
			return buf == null ? proceed() : buf;
		}

		public @NotNull String toRefreshedString() {
			return proceed();
		}
	}

	public static class BufArgFixedString extends BufArgString {
		private final @NotNull String str;

		public BufArgFixedString(@NotNull String str, Object @NotNull ...args) {
			super(args);
			this.str = str;
		}

		@Override
		protected @NotNull String getParameterizedString() {
			return str;
		}
	}

	public static abstract class DynArgString extends ArgString {
		public DynArgString(Object @NotNull ...args) {
			super(args);
		}
	}

	public static class DynArgFixedString extends DynArgString {
		private final @NotNull String str;

		public DynArgFixedString(@NotNull String str, Object @NotNull ... args) {
			super(str, args);
			this.str = str;
		}

		@Override
		protected @NotNull String getParameterizedString() {
			return str;
		}
	}

	/** A simple wrapper class in case of on-demand refresh update. */
	public static class OnRequestString {
		private final @NotNull Supplier<@NotNull String> supplier;

		public OnRequestString(@NotNull Supplier<@NotNull String> supplier) {
			this.supplier = supplier;
		}

		@Override
		public String toString() {
			return supplier.get();
		}
	}
}
