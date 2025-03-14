package minicraft.screen.entry.commands;

import minicraft.screen.entry.InputEntry;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A type of input entry that focuses on returning a deserved type of value with the user input.
 */
public class TargetedInputEntry<T> extends InputEntry {
	protected static TargetedValidator<?> NOOP = new TargetedValidator<>(null, i -> null);

	@SuppressWarnings("unchecked")
	protected static <T> TargetedValidator<T> noOpValidator() {
		return (TargetedValidator<T>) NOOP;
	}

	public static class TargetedValidator<T> {
		public final @Nullable Predicate<String> validator;
		public final @NotNull Function<String, T> parser;

		/**
		 * In order to function precisely, the {@code validator} function must be compatible with the {@code parser} function;
		 * for all {@code string}, the following must hold:
		 * <pre>{@code
		 *     (parser.apply(string) != null) == validator.test(string)
		 * }</pre>
		 *
		 * @param validator A validator to validate user input with the desired input.
		 *                  If {@code null}, it checks whether the given {@code parser} returns {@code null} instead.
		 * @param parser    A parser to parse and may validate the input.
		 */
		public TargetedValidator(@Nullable Predicate<String> validator, @NotNull Function<String, T> parser) {
			this.validator = validator;
			this.parser = parser;
		}
	}

	private final TargetedValidator<T> validator;

	public TargetedInputEntry(String prompt, @NotNull TargetedValidator<T> validator) {
		this(prompt, null, validator);
	}

	public TargetedInputEntry(String prompt, @RegExp String regex, @NotNull TargetedValidator<T> validator) {
		this(prompt, regex, validator, "");
	}

	public TargetedInputEntry(String prompt, @RegExp String regex, @NotNull TargetedValidator<T> validator, @NotNull String initValue) {
		super(prompt, regex, 0, initValue); // maxLen is not important
		this.validator = validator;
	}

	protected boolean hasValidator() {
		checkForValidator();
		return validator.validator != null;
	}

	protected boolean validate(String input) {
		checkForValidator();
		return validator.validator == null ? parse(input) != null : validator.validator.test(input);
	}

	protected @Nullable T parse(String input) {
		checkForValidator();
		return validator.parser.apply(input);
	}

	@Override
	public boolean isValid() {
		return super.isValid() && validate(getUserInput());
	}

	private void checkForValidator() {
		if (validator == NOOP)
			throw new SecurityException("NOOP is used without method overrides");
	}

	public T getValue() throws IllegalArgumentException {
		String input = getUserInput();
		boolean valid = false;
		if (!hasValidator() || (valid = validate(input))) {
			T value = parse(input);
			if (value != null) return value;
			else if (valid)
				throw new SecurityException("corrupted validator parser");
		}

		throw new IllegalArgumentException("value is invalid");
	}
}
