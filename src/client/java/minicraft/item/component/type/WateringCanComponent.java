package minicraft.item.component.type;

public class WateringCanComponent {
	private final int content;
	private final int renderingTick;

	public WateringCanComponent(int content, int renderingTick) {
		this.content = content;
		this.renderingTick = renderingTick;
	}

	public WateringCanComponent withContent(int content) {
		return new WateringCanComponent(content, this.renderingTick);
	}

	public WateringCanComponent withRenderingTick(int renderingTick) {
		return new WateringCanComponent(this.content, renderingTick);
	}

	public int content() {
		return this.content;
	}

	public int renderingTick() {
		return this.renderingTick;
	}
}
