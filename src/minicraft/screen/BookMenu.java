package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class BookMenu extends Menu {
	
	// null characters "\0" denote page breaks.
	public static String antVenomBook = "Antidious Venomi\n"+
	"\n"+
	"\n"+
	"A short story by David.B\n"+
	"\n"+
	"\n"+
	"\n"+
	"12 Pages\n"+
	"\n"+
	"\n"+
	"2012\n"+
	"\0"+
"There was once a creature who lived in his small home in the valley. Only a few people know of his existence. A being like us, but not the same. He can fight, talk, and solve problems. But he is dangerous also. I can say that people who went looking for him, never came back.\n"
+"\n"+
"So I set myself to find the creature and record my data in this journal. His valley is supposedly somewhere in the Golden Mountains. It is a place forbidden for anyone to enter. It has said that the legendary golden fruit grows there and that it can heal any wound with one bite. That is just a legend though, and I am not here for it.\n"
+"\n"+
"Many people have came here for the fruit, and died trying. If it is true, I think the creature would have gotten to it by now.\n"
+"\n"+
"I enter the mountains with my supplies and said my goodbyes. The trees have pale green leaves with thick vines around them. I have crossed into the forbidden zone, and this is my last chance for going back. I kept my spirit high and senses also. I don't know what I will run into here.\n"
+"\n"+
"It looks like someone left a bunch of rocks on the ground to make a trail, but since the ground is to mossy it's hard to make out where the trail leads. I come up to what looks like a camp site. It seems to have been deserted for a long time. I have found a book that is dated from many years ago. The person who wrote this was after the golden fruit. It says about how he'll be famous for his journey. I put down the book as the sky was turning dark. I chopped some wood and started a campfire. I fell asleep a few hours later and woke up by dawn. The clouds must have disappeared and I felt a short but cold breeze in the air.\n"
+"\n"+
"The lustrous sunlight came down as I set off to continue my journey. I came across a apple tree Nothing special about it, but there was something behind it. I couldn't believe my eyes, A large garden of fruits! I can see Apples, Bananas, Grapes, Pears, Cherries, Blueberries, and many others. This garden of eden seems to be endless and somehow like a maze. I can feed my kin for generations with all of this food. I hear footsteps behind me I found some bushes to take some cover. What is that thing? I've never seen it before. Is it the creature I'm searching for? It's eyes have this cold dark brown color, his hide is only partly visible on his head, the rest seems to be bare. He also has this strange amulet on his chest. It looks fancy, it's made from iron, gold, and Amethyst. I can't seem to stop looking at it What is happening to me? It feels as though I want to move closer to it. I can't, I cannot blow my cover like this. It would be the end of me for sure. But as soon my leg lifted, the creature left with some grapefruit. I caught my breath and sat down for a minute. I don't know why I did not leave right there and then, I might have lived. But this sudden urge for that amulet made me continue. I caught up to the creature, following closely but silently. It looks like his home is ahead. As he enters I go to the side and peak in from a window. He is in the kitchen with the fruit, It's not too big of a room. Some light coming from the ceiling from a mysterious stone. From a large metal box he grabs some fish and pork. Places some of the fruit on his plate and puts the rest in the box. I noticed that the amulet from his chest is gone. Creeping to the front door, I saw a horrifying sight. On a fireplace I see a head as a trophy piece. I went back out to hurl. I don't know what Monster would do such a sin The beast enters from the room, I cannot enter from the front. Slowly I make my way to the back door. The door makes a quiet sqeak, but the beast doesn't hear it. I look around for the amulet while not to make a sound. after a bit of searching I come across his room. He has a wood platform with knobs and tiny square lines and a Picture is on it. It seems to be a female, probably from the same race. And a small paper at the back of it. It's his name, 'An-' before I could say it he comes into the room with a puzzled look on his face. Drawing a blade made of Iron from his side and charges. I run for my life, as fast as can down the hallway, and out the door.\n"
+"\n"+
"This is the final minute for me By the tenth step of grass I collapsed, Bleeding to death as my cow brothers did before me. With my last ounce of strength I drew his name out with my blood in the hopes that somebody can know this terror.\n"
+"\n"+
"\'You can only make out a little.\'\n"
+"\n"
+"\n"+
"\'Ant----- Venom-\'\n"
+"\n"
+"\n"
+"\n"+
"The end";
	
	public static final String defaultBook = " \n \0"+"There is nothing of use.";
	
	private static int spacing = 3;
	private static java.awt.Rectangle textArea = new java.awt.Rectangle(15, 8*5, 8*32, 8*16);
	
	public String[][] lines;
	public int page;
	
	public BookMenu(String book) {
		page = 0;
		if(book == null)
			book = defaultBook;
		
		ArrayList<String[]> pages = new ArrayList<String[]>();
		String[] splitContents = book.split("\0");
		for(String content: splitContents) {
			String[] remainder = {content};
			while(remainder[remainder.length-1].length() > 0) {
				remainder = Font.getLines(remainder[remainder.length-1], textArea.width, textArea.height, spacing);
				pages.add(Arrays.copyOf(remainder, remainder.length-1)); // removes the last element of remainder, which is the leftover.
			}
		}
		
		lines = pages.toArray(new String[0][]);
	}
	
	public void tick() {
		if (input.getKey("menu").clicked || input.getKey("escape").clicked)
			game.setMenu(null); // this is what closes the book; TODO if books were editable, I would probably remake the book here with the edited pages.
		if (input.getKey("left").clicked && page > 0) page--; // this is what turns the page back
		if (input.getKey("right").clicked && page < lines.length-1) page++; // this is what turns the page forward
	}
	
	public void render(Screen screen) {
		// These draw out the screen.
		renderFrame(screen, 14, 0, 21, 3); // renders the tiny, page number display frame.
		renderFrame(screen, 1, 4, 34, 20); // renders the big text content display frame.
		
		// This draws the text "Page" at the top of the screen
		Font.draw("Page", screen, 8 * 15 + 8, 1 * 8 - 2, Color.get(-1, 0));
		
		// This is makes the numbers appear below "Page" // ...but it doesn't work...
		String pagenum = page==0?"Title": page+"";
		Font.drawCentered(pagenum, screen, /*11*11 + (page==0 ? 4 : 21-3*digits(page)), */2 * 8, Color.get(-1, 0));
		
		Font.drawParagraph(lines[page], screen, textArea.x, textArea.y, textArea.width, textArea.height, page == 0, spacing, Color.get(-1, 0));
	}
	
	protected void renderFrame(Screen screen, int x0, int y0, int x1, int y1) {
		renderMenuFrame(screen, "", x0, y0, x1, y1, Color.get(-1, 1, 554, 554), Color.get(554, 554), Color.get(-1, 222));
	}
}
