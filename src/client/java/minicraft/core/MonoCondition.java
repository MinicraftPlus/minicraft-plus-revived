package minicraft.core;

/** @deprecated Not in use. Most likely this is replaced by {@link java.util.function.Predicate Predicate&lt;T&gt;}. */
@FunctionalInterface
public interface MonoCondition<T> {
	boolean check(T arg);
}
