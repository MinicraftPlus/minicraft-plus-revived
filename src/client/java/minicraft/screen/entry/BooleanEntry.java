package minicraft.screen.entry;

import minicraft.core.io.Localization;

public class BooleanEntry extends ArrayEntry<Boolean> {

	public BooleanEntry(String label, boolean initial) {
		super(label, true, new Boolean[] { true, false });

		setSelection(initial ? 0 : 1);
	}

	@Override
	protected String getLocalizationOption(Boolean option) {
		return option ?
			"minicraft.display.entries.boolean.true" :
			"minicraft.display.entries.boolean.false";
	}

	@Override
	public String toString() {
		return Localization.getLocalized(getLabel()) + ": " + (getValue() ?
			Localization.getLocalized("minicraft.display.entries.boolean.true") :
			Localization.getLocalized("minicraft.display.entries.boolean.false")
		);
	}
}
