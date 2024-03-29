package minicraft.screen.entry;

/** This specifies that the entry content is mutable by user, and not handled by {@link SelectEntry}. */
public interface UserMutable {
	void setChangeListener(ChangeListener listener);
}
