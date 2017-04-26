package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import java.util.List;

public class BookMenu extends Menu {
	
	// "----" denotes a page break.
	private static final String book =
"Antidious Venomi\n"+
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
"\n"+
"----\n"+
"// this is page 1\n"+
"There was once a creature who\n"+
"lived in his small home in the\n"+
"valley. Only a few people know\n"+
"of his existence. A being like\n"+
"us, but not the same. He can\n"+
"fight, talk, and solve problems.\n"+
"But he is dangerous also. I can\n"+
"say that people who went looking\n"+
"for him, never came back.\n"+
"\n"+
"So I set myself to find the\n"+
"creature and record my data in\n"+
"this journal.\n"+
"----\n"+
"// page 2\n"+
"His valley is supposedly some-\n"+
"where in the Golden Mountains.\n"+
"It is a place forbidden for\n"+
"anyone to enter. It has said\n"+
"that the legendary golden fruit\n"+
"grows there and that it can heal\n"+
"any wound with one bite. That\n"+
"is just a legend though, and I\n"+
"am not here for it.\n"+
"\n"+
"Many people have came here for\n"+
"the fruit, and died trying. If\n"+
"it is true, I think the creature\n"+
"----\n"+
"// page 3\n"+
"would have gotten to it by now.\n"+
"\n"+
"I enter the mountains with my\n"+
"supplies and said my goodbyes.\n"+
"The trees have pale green leaves\n"+
"with thick vines around them. I\n"+
"have crossed into the forbidden\n"+
"zone, and this is my last chance\n"+
"for going back. I kept my spirit\n"+
"high and senses also. I don\'t\n"+
"know what I will run into here.\n"+
"\n"+
"It looks like someone left a\n"+
"----\n"+
"// page 4\n"+
"bunch of rocks on the ground to\n"+
"make a trail, but since the\n"+
"ground is to mossy it\'s hard to\n"+
"make out where the trail leads.\n"+
"I come up to what looks like a\n"+
"camp site. It seems to have been\n"+
"deserted for a long time. I have\n"+
"found a book that is dated from\n"+
"many years ago. The person who\n"+
"wrote this was after the golden\n"+
"fruit. It says about how he\'ll\n"+
"be famous for his journey. I put\n"+
"down the book as the sky was\n"+
"----\n"+
"// page 5\n"+
"turning dark. I chopped some wood\n"+
"and started a campfire. I fell\n"+
"asleep a few hours later and woke\n"+
"up by dawn. The clouds must have\n"+
"disappeared and I felt a short\n"+
"but cold breeze in the air.\n"+
"\n"+
"The lustrous sunlight came down\n"+
"as I set off to continue my jou-\n"+
"rney. I came across a apple tree\n"+
"Nothing special about it, but\n"+
"there was something behind it.\n"+
"I couldn\'t believe my eyes, A\n"+
"----\n"+
"// page 6\n"+
"large garden of fruits! I can\n"+
"see Apples, Bananas, Grapes,\n"+
"Pears, Cherries, Blueberries, and\n"+
"many others. This garden of eden\n"+
"seems to be endless and somehow\n"+
"like a maze. I can feed my kin\n"+
"for generations with all of this\n"+
"food. I hear footsteps behind me\n"+
"I found some bushes to take some\n"+
"cover. What is that thing? I\'ve\n"+
"never seen it before. Is it the\n"+
"creature I\'m searching for?\n"+
"It\'s eyes have this cold dark\n"+
"----\n"+
"// page 7\n"+
"brown color, his hide is only\n"+
"partly visible on his head, the\n"+
"rest seems to be bare. He also\n"+
"has this strange amulet on his\n"+
"chest. It looks fancy, it\'s made\n"+
"from iron, gold, and Amethyst. I\n"+
"can\'t seem to stop looking at it\n"+
"What is happening to me? It feels\n"+
"as though I want to move closer\n"+
"to it. I can\'t, I cannot blow my\n"+
"cover like this. It would be the\n"+
"end of me for sure. But as soon\n"+
"my leg lifted, the creature left\n"+
"----\n"+
"// page 8\n"+
"with some grapefruit. I caught\n"+
"my breath and sat down for a\n"+
"minute. I don\'t know why I did\n"+
"not leave right there and then,\n"+
"I might have lived. But this\n"+
"sudden urge for that amulet made\n"+
"me continue. I caught up to the\n"+
"creature, following closely but\n"+
"silently. It looks like his home\n"+
"is ahead. As he enters I go to\n"+
"the side and peak in from a win-\n"+
"dow. He is in the kitchen with\n"+
"the fruit, It\'s not too big of\n"+
"----\n"+
"// page 9\n"+
"a room. Some light coming from\n"+
"the ceiling from a mysterious\n"+
"stone. From a large metal box he\n"+
"grabs some fish and pork. Places\n"+
"some of the fruit on his plate\n"+
"and puts the rest in the box. I\n"+
"noticed that the amulet from his\n"+
"chest is gone. Creeping to the\n"+
"front door, I saw a horrifying\n"+
"sight. On a fireplace I see a\n"+
"head as a trophy piece. I went\n"+
"back out to hurl. I don\'t know\n"+
"what Monster would do such a sin\n"+
"----\n"+
"// page 10\n"+
"The beast enters from the room,\n"+
"I cannot enter from the front.\n"+
"Slowly I make my way to the back\n"+
"door. The door makes a quiet sq-\n"+
"eak, but the beast doesn\'t hear\n"+
"it. I look around for the amulet\n"+
"while not to make a sound. after\n"+
"a bit of searching I come across\n"+
"his room. He has a wood platform\n"+
"with knobs and tiny square lines\n"+
"and a Picture is on it. It seems\n"+
"to be a female, probably from\n"+
"the same race. And a small paper\n"+
"----\n"+
"// page 11\n"+
"at the back of it. Its his name,\n"+
"\'An-\' before I could say it he\n"+
"comes into the room with a puzz-\n"+
"led look on his face. Drawing a\n"+
"blade made of Iron from his side\n"+
"and charges. I run for my life,\n"+
"as fast as can down the hallway,\n"+
"and out the door.\n"+
"\n"+
"This is the final minute for me\n"+
"By the tenth step of grass I co-\n"+
"llapsed, Bleeding to death as my\n"+
"cow brothers did before me. With\n"+
"----\n"+
"// page 12\n"+
"my last ounce of strength I drew\n"+
"his name out with my blood in\n"+
"the hopes that somebody can know\n"+
"this terror.\n"+
"\n"+
"\'You can only make out a little.\'\n"+
"\n"+
"\n"+
"\'Ant----- Venom-\'\n"+
"\n"+
"\n"+
"\n"+
"The end";
	
	public static final String[] antVenomPages = book.split("\\n----\\n(//.*\\n)?");
	// this sets the start page "0" also is the Title page
	
	public static final String[] defaultPages = {
		'\n'+
"\n"+
"\n"+
" \n"+
"\n"+
"\n"+
"\n"+
" \n"+
"\n"+
"\n"+
" \n"+
"",
	"There is nothing of use."
};
	
	public String[][] lines;
	public int page;
	
	public BookMenu() {this(defaultPages);}
	public BookMenu(String[] pages) {
		page = 0;
		lines = new String[pages.length][];
		for(int i = 0; i < pages.length; i++)
			lines[i] = pages[i].split("\\n");
	}
	
	public void tick() {
		if (input.getKey("menu").clicked || input.getKey("escape").clicked)
			game.setMenu(null); // this is what closes the book
		if (input.getKey("left").clicked && page > 0) page--; // this is what turns the page back
		if (input.getKey("right").clicked && page < lines.length-1) page++; // this is what turns the page forward
	}
	
	public void render(Screen screen) {
		// These draw out the screen.
		Font.renderFrameBook(screen, "", 14, 0, 21, 3);
		Font.renderFrameBook(screen, "", 1, 4, 34, 20);

		// Don't need to mess with this
		int xe = 11 * 11;
		int xo = 12 * 11;
		int xa = 11 * 12 - 3;
		int xu = 11 * 12 - 7;

		// This draws the text "Page" at the top of the screen
		Font.draw("Page", screen, 8 * 15 + 8, 1 * 8 - 2, Color.get(-1, 0));
		
		// This is makes the numbers appear below "Page"
		Font.draw(page==0?"Title": page+"", screen, 11*11 + (page==0 ? 4 : 21-3*digits(page)), 2 * 8, Color.get(-1, 0));
		
		String[] text = lines[page];
		for(int i = 0; i < text.length; i++) {
			if(text[i].length() == 0) continue;
			if(page == 0) // center; otherwise, don't center.
				Font.draw(text[i], screen, screen.centerText(text[i]), 8*(5+i)+(i==0?0:4), Color.get(-1, 0));
			else
				Font.draw(text[i], screen, 15, 8*(4+i)+4, Color.get(-1, 0));
		}
	}
	
	private int digits(int num) {
		int d = 1;
		while(num / Math.pow(10, d) > 0)
			d++;
		return d;
	}
}
