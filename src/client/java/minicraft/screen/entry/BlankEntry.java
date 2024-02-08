package minicraft.screen.entry;

import minicraft.core.io.InputHandler;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Screen;
import org.jetbrains.annotations.Nullable;

public class BlankEntry extends ListEntry {

	public BlankEntry() {
		setSelectable(false);
	}

	@Override
	public void tick(InputHandler input) {
	}

	@Override
	public void render(Screen screen, @Nullable Screen.RenderingLimitingModel limitingModel, int x, int y, boolean isSelected) {
	}

	@Override
	public int getWidth() {
		return MinicraftImage.boxWidth;
	}

	@Override
	public String toString() {
		return " ";
	}
}
