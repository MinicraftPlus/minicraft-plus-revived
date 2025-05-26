package minicraft.item.component.type;

import minicraft.item.BookItem;

public class BookContentComponent {
	private final BookItem.BookContent content;
	private final boolean hasTitlePage;

	public BookContentComponent(BookItem.BookContent content, boolean hasTitlePage) {
		this.content = content;
		this.hasTitlePage = hasTitlePage;
	}

	public BookItem.BookContent content() {
		return this.content;
	}

	public boolean hasTitlePage() {
		return this.hasTitlePage;
	}
}
