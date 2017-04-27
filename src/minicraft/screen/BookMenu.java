package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import java.util.ArrayList;
import java.util.Arrays;

public class BookMenu extends Menu {
	
	/*// "----" denotes a page break.
	private static final String book =
"Antidious Venomi\n"+
"\n"+
"\n"+
"A short story by David.B\n"+
"\n"+
"\n"+
"\n"+
"13 Pages\n"+
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
	*/
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
	/*
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
};*/
	
	private static int spacing = 3;
	private static java.awt.Rectangle textArea = new java.awt.Rectangle(15, 8*5, 8*32, 8*16);
	
	public String[][] lines;
	//public ArrayList<String> pages;
	public int page;
	
	public BookMenu() {this(defaultBook);}
	public BookMenu(String book) {
		page = 0;
		ArrayList<String[]> pages = new ArrayList<String[]>();
		// note: width of text area is 32; x is 15.
		String[] splitContents = book.split("\0");
		for(String content: splitContents) {
			//System.out.println("starting next null-sep segment...");
			String[] remainder = {content};
			//int curPos = 0;
			while(remainder[remainder.length-1].length() > 0) {
				//System.out.println("length of remainder: " + remainder[remainder.length-1].length());
				remainder = Font.getLines(remainder[remainder.length-1], textArea.width, textArea.height, spacing);
				pages.add(Arrays.copyOf(remainder, remainder.length-1)); // removes the last element of remainder, which is the leftover.
				//break;
			}
		}
		//System.out.println("book distributed successfully. final version:");
		lines = pages.toArray(new String[0][]);
		
		/*for(int i = 0; i < lines.length; i++) {
			System.out.println("page " + i);
			for(String line: lines[i])
				System.out.println(line);
			System.out.println();
		}*/
	}
	
	public void tick() {
		if (input.getKey("menu").clicked || input.getKey("escape").clicked)
			game.setMenu(null); // this is what closes the book
		if (input.getKey("left").clicked && page > 0) page--; // this is what turns the page back
		if (input.getKey("right").clicked && page < lines.length-1) page++; // this is what turns the page forward
	}
	
	public void render(Screen screen) {
		// These draw out the screen.
		renderFrame(screen, 14, 0, 21, 3); // renders the tiny, page number display frame.
		renderFrame(screen, 1, 4, 34, 20); // renders the big text content display frame.

		/*// Don't need to mess with this
		int xe = 11 * 11;
		int xo = 12 * 11;
		int xa = 11 * 12 - 3;
		int xu = 11 * 12 - 7;
		*/
		
		// This draws the text "Page" at the top of the screen
		Font.draw("Page", screen, 8 * 15 + 8, 1 * 8 - 2, Color.get(-1, 0));
		
		// This is makes the numbers appear below "Page" // ...but it doesn't work...
		String pagenum = page==0?"Title": page+"";
		Font.drawCentered(pagenum, screen, /*11*11 + (page==0 ? 4 : 21-3*digits(page)), */2 * 8, Color.get(-1, 0));
		
		Font.drawParagraph(lines[page], screen, textArea.x, textArea.y, textArea.width, textArea.height, page == 0, spacing, Color.get(-1, 0));
		/*
		String[] text = lines[page];
		for(int i = 0; i < text.length; i++) {
			if(text[i].length() == 0) continue;
			int y = 8*(5+i) + spacing*i + 4;
			if(page == 0) // center; otherwise, don't center.
				Font.drawCentered(text[i], screen, y, Color.get(-1, 0));
			else
				Font.draw(text[i], screen, 15, y, Color.get(-1, 0));
		}*/
	}
	
	protected void renderFrame(Screen screen, int x0, int y0, int x1, int y1) {
		Font.renderMenuFrame(screen, "", x0, y0, x1, y1, Color.get(-1, 1, 554, 554), Color.get(554, 554), Color.get(-1, 222));
	}
}
