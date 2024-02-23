package minicraft.screen.entry;

import minicraft.core.io.Localization;

public class BooleanEntry extends ArrayEntry<Boolean> {

	public BooleanEntry(Localization.LocalizationString label, boolean initial) {
		super(label, true, new Boolean[]{true, false});

		setSelection(initial ? 0 : 1);
	}

	@Override
	protected String getLocalizedOption(Boolean option) {
		return Localization.getLocalized(option ? "minicraft.display.entries.boolean.true" :
			"minicraft.display.entries.boolean.false");
	}
}
