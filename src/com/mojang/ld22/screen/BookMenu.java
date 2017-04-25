package com.mojang.ld22.screen;

//import com.mojang.ld22.crafting.Recipe;
//import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
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
 

 
 
 
 
 
 

 
 
 //'
	};
	
	//public int pages = 0;
	// this sets the last page
	//public int lastpage = 12;
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
		/*
		if (pages <= 9 && pages >= 1) {
			Font.draw("" + pages, screen, xo + 7, 2 * 8, Color.get(-1, 0, 0, 0));

			// This makes it that "0" becomes "Title" instead.
		} else if (page == 0) {
			Font.draw("Title", screen, xe + 4, 2 * 8, Color.get(-1, 0, 0, 0));

			//This makes it so that the 2 digit numbers pose properly when the pages reach 10.
		} else if (pages > 9 && pages < 100) {
			Font.draw("" + pages, screen, xa + 7, 2 * 8, Color.get(-1, 0, 0, 0));

			//This makes it so that the 3 digit numbers pose properly when the pages reach 100.
		} else if (pages > 99) {
			Font.draw("" + pages, screen, xu + 7, 2 * 8, Color.get(-1, 0, 0, 0));
		}
		*/

		// This is that there are no Negative pages
		//if (pages < 0) pages = 0;

		// This makes is that the player doesn't go past the last page.
		//if (pages > lastpage) pages = lastpage;
		
		String[] text = lines[page];
		for(int i = 0; i < text.length; i++) {
			if(text[i].length() == 0) continue;
			if(page == 0) // center; otherwise, don't center.
				Font.draw(text[i], screen, screen.centerText(text[i]), 8*(5+i)+(i==0?0:4), Color.get(-1, 0));
			else
				Font.draw(text[i], screen, 15, 8*(4+i)+4, Color.get(-1, 0));
		}
		
		/*
		// You can write anywhere in the between the ""
		// Page 0 is the Title page
		if (pages == 0) {
			// Make any edits beyond ,screen, to change the position and color.
			Font.draw("Antidious Venomi", screen, 8 * 9 + 8, 5 * 8, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("A short story by David.B", screen, 6 * 8, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("12 Pages", screen, 12 * 9, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("2012", screen, 14 * 9, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

			// this is page 1
		} else if (pages == 1) {
			Font.draw("There was once a creature who", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("lived in his small home in the", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("valley. Only a few people know", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("of his existence. A being like", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("us, but not the same. He can", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("fight, talk, and solve problems.", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("But he is dangerous also. I can", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("say that people who went looking", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("for him, never came back.", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("So I set myself to find the", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("creature and record my data in", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("this journal.", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

			// this is page 2
		} else if (pages == 2) {
			Font.draw("His valley is supposedly some-", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("where in the Golden Mountains.", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("It is a place forbidden for", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("anyone to enter. It has said", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("that the legendary golden fruit", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("grows there and that it can heal", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("any wound with one bite. That", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("is just a legend though, and I", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("am not here for it.", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("Many people have came here for", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("the fruit, and died trying. If", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("it is true, I think the creature", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

		} else if (pages == 3) {
			Font.draw("would have gotten to it by now.", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("I enter the mountains with my", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("supplies and said my goodbies.", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("The trees have pale green leaves", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("with thick vines around them. I", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("have crossed into the forbbiden", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("zone, and this is my last chance", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("for going back. I kept my spirt", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("high and senses also. I don't", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("know what I will run into here.", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("It looks like someone left a", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

		} else if (pages == 4) {
			Font.draw("bunch of rocks on the ground to", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("make a trail, but since the", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("ground is to mossy it's hard to ", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("make out where the trail leads.", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("I come up to what looks like a", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("camp site. It seems to have been", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("deserted for a long time. I have", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("found a book that is dated from", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("many years ago. The person who", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("wrote this was after the golden", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("fruit. It says about how he'll", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("be famous for his journy. I put", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("down the book as the sky was", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

		} else if (pages == 5) {
			Font.draw("turning dark. I chopped some wood", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("and started a campfire. I fell", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("asleep a few hours later and woke", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("up by dawn. The clouds must have", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("dissapeared and I felt a short", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("but cold breeze in the air.", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("The lustrous sunlight came down", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("as I set off to continue my jou-", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("rney. I came across a apple tree", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("Nothing special about it, but", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("there was something behind it.", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("I couldn't believe my eyes, A", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

		} else if (pages == 6) {
			Font.draw("large garden of fruits! I can ", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("see Apples, Bananas, Grapes,", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("Pears, Cherrys, Blueberrys, and", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("many others. This garden of eden", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("seems to be endless and somehow", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("like a maze. I can feed my kin", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("for generations with all of this", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("food. I hear footsteps behind me", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("I found some bushes to take some", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("cover. What is that thing? I've", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("never seen it before. Is it the", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("creature I'm searching for?", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("It's eyes have this cold dark", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

		} else if (pages == 7) {
			Font.draw("brown color, his hide is only", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("partly visible on his head, the", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("rest seems to be bare. He also", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("has this strange amulet on his", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("chest. It looks fancy, it's made", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("from iron, gold, and Amethyst. I", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("can't seem to stop looking at it", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("What is happing to me? It feels", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("as though I want to move closer", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("to it. I can't, I cannot blow my", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("cover like this. It would be the", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("end of me for sure. But as soon", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("my leg lifted, the creature left", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

		} else if (pages == 8) {
			Font.draw("with some grapefruit. I cought", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("my breath and sat down for a", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("minute. I don't know why I did", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("not leave right there and then,", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("I might have lived. But this", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("sudden urge for that amulet made", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("me continue. I caught up to the", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("creature, following closely but", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("silently. It looks like his home", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("is ahead. As he enters I go to", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("the side and peak in from a win-", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("dow. He is in the kitchen with", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("the fruit, It's not too big of", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

		} else if (pages == 9) {
			Font.draw("a room. Some light coming from", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("the celling from a mysetrous", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("stone. From a large metal box he", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("grabs some fish and pork. Places", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("some of the fruit on his plate", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("and puts the rest in the box. I", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("noticed that the amulet from his", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("chest is gone. Creeping to the ", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("front door, I saw a horrifing", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("sight. On a fireplace I see a", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("head as a trophy piece. I went", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("back out to hurl. I don't know", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("what Monster would do such a sin", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

		} else if (pages == 10) {
			Font.draw("The beast enters from the room,", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("I cannot enter from the front.", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("Slowly I make my way to the back", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("door. The door makes a quiet sq-", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("ueek, but the beast doesn't hear", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("it. I look around for the amulet", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("while not to make a sound. after", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("a bit of searching I come across", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("his room. He has a wood platform", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("with knobs and tiny square lines", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("and a Picture is on it. It seems", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("to be a female, probably from ", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("the same race. And a small paper", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

		} else if (pages == 11) {
			Font.draw("at the back of it. Its his name,", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("'An-' before I could say it he ", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("comes into the room with a puzz-", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("led look on his face. Drawing a", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("blade made of Iron from his side", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("and charges. I run for my life,", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("as fast as can down the hallway,", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("and out the door.", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("This is the final minute for me", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("By the tenth step of grass I co-", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("llapsed, Bleeding to death as my", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("cow brothers did before me. With", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));
		} else if (pages == 12) {
			Font.draw("my last ounce of strength I drew", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("his name out with my blood in", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("the hopes that somebody can know", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("this terror.", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("'You can only make out a little.'", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("'Ant----- Venom-'", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("												The end", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));
		}
		*/
	}
	
	private int digits(int num) {
		int d = 1;
		while(num / Math.pow(10, d) > 0)
			d++;
		return d;
	}
}
