package minicraft.core;

/** @deprecated Not in use. Most likely this is replaced by {@link java.util.function.Supplier Supplier&lt;Boolean&gt;}. */
@FunctionalInterface
public interface Condition {
	boolean check();
}
