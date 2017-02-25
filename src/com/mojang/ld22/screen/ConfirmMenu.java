//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import java.awt.event.ActionEvent;
import java.io.File;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.levelgen.LevelGen;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.sound.Sound;
import com.mojang.ld22.Game; 
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.Entity;

public class ConfirmMenu extends Menu {
    JFileChooser fc;
    JTextArea log;
	public ConfirmMenu() {
	}
	String nameText;
	private static Level level;
	private Scanner random; 
	static int [] array = {1,2,3,4};
	
	static int x = 0;
	static int y = 0;
	
	
	static Level holl;
	
    public void actionPerformed(ActionEvent e) {
    	

      File file = fc.getSelectedFile();
      //This is where a real application would save the file.
      System.out.print("Saving: " + file.getName() + ".");
    	
    }
    
    
    public static String Savemaps(String string){
    	String data = "Error in Saving";
    	if (string.equals("0")){
    		data = "G";
    	}
    	if (string.equals("1")){
    		data = "R";
    	}
    	if (string.equals("2")){
    		data = "W";
    	}
    	if (string.equals("3")){
    		data = "F";
    	}
    	if (string.equals("4")){
    		data = "T";
    	}
    	if (string.equals("5")){
    		data = "D";
    	}
    	if (string.equals("6")){
    		data = "S";
    	}
    	if (string.equals("7")){
    		data = "C";
    	}
    	if (string.equals("8")){
    		data = "H";
    	}
    	if (string.equals("9")){
    		data = "Ts";
    	}
    	if (string.equals("10")){
    		data = "Cs";
    	}
    	if (string.equals("11")){
    		data = "Fa";
    	}
       	if (string.equals("12")){
    		data = "Wh";
    	}
    	if (string.equals("13")){
    		data = "L";
    	}
    	if (string.equals("14")){
    		data = "Sd";
    	}
    	if (string.equals("15")){
    		data = "Su";
    	}
    	if (string.equals("16")){
    		data = "If";
    	}
    	if (string.equals("17")){
    		data = "Cl";
    	}
    	if (string.equals("18")){
    		data = "Hr";
    	}
    	if (string.equals("19")){
    		data = "Io";
    	}
    	if (string.equals("20")){
    		data = "Go";
    	}
    	if (string.equals("21")){
    		data = "Ge";
    	}
 
    		
    		
		return data;
    	
    	
 
    }
    
    
    
    
    public static void main(String args[]){
     int mapcountscount = 0;
     String s = " ";
     int zerozero = 0;
     
     try{
     

     // Creates file 
     FileWriter fstream = new FileWriter("Player.file");
     FileWriter mstream = new FileWriter("Map.file");
     BufferedWriter out = new BufferedWriter(fstream);
     BufferedWriter outm = new BufferedWriter(mstream);
     
     if (Game.Time == 0){
     out.write("0" + s);
     
     if (Player.SHealth == 1){
     out.write("1" + s);
     }
     if (Player.SHealth == 2){
   	 out.write("2" + s);
     }
     if (Player.SHealth == 3){
   	 out.write("3" + s);
     }
     if (Player.SHealth == 4){
   	 out.write("4" + s);
     }
     if (Player.SHealth == 5){
   	 out.write("5" + s);
     }
     if (Player.SHealth == 6){
   	 out.write("6" + s);
     }
     if (Player.SHealth == 7){
   	 out.write("7" + s);
     }
     if (Player.SHealth == 8){
   	 out.write("8" + s);
     }
     if (Player.SHealth == 9){
   	 out.write("9" + s);
     }
     if (Player.SHealth == 10){
   	 out.write("10" + s);
     }
     if (Player.SHunger < 1){
     out.write("0" + s);
     }
     if (Player.SHunger == 1){
     out.write("1" + s);
     }
     if (Player.SHunger == 2){
   	 out.write("2" + s);
     }
     if (Player.SHunger == 3){
   	 out.write("3" + s);
     }
     if (Player.SHunger == 4){
   	 out.write("4" + s);
     }
     if (Player.SHunger == 5){
   	 out.write("5" + s);
     }
     if (Player.SHunger == 6){
   	 out.write("6" + s);
     }
     if (Player.SHunger == 7){
   	 out.write("7" + s);
     }
     if (Player.SHunger == 8){
   	 out.write("8" + s);
     }
     if (Player.SHunger == 9){
   	 out.write("9" + s);
     }
     if (Player.SHunger == 10){
   	 out.write("10" + s);
     }
     
     }
     
     
     if (Game.Time == 1){
     out.write("1" + s);
     
     if (Player.SHealth == 1){
     out.write("1" + s);
     }
     if (Player.SHealth == 2){
   	 out.write("2" + s);
     }
     if (Player.SHealth == 3){
   	 out.write("3" + s);
     }
     if (Player.SHealth == 4){
   	 out.write("4" + s);
     }
     if (Player.SHealth == 5){
   	 out.write("5" + s);
     }
     if (Player.SHealth == 6){
   	 out.write("6" + s);
     }
     if (Player.SHealth == 7){
   	 out.write("7" + s);
     }
     if (Player.SHealth == 8){
   	 out.write("8" + s);
     }
     if (Player.SHealth == 9){
   	 out.write("9" + s);
     }
     if (Player.SHealth == 10){
   	 out.write("10" + s);
     }
     if (Player.SHunger < 1){
     out.write("0" + s);
     }
     if (Player.SHunger == 1){
     out.write("1" + s);
     }
     if (Player.SHunger == 2){
   	 out.write("2" + s);
     }
     if (Player.SHunger == 3){
   	 out.write("3" + s);
     }
     if (Player.SHunger == 4){
   	 out.write("4" + s);
     }
     if (Player.SHunger == 5){
   	 out.write("5" + s);
     }
     if (Player.SHunger == 6){
   	 out.write("6" + s);
     }
     if (Player.SHunger == 7){
   	 out.write("7" + s);
     }
     if (Player.SHunger == 8){
   	 out.write("8" + s);
     }
     if (Player.SHunger == 9){
   	 out.write("9" + s);
     }
     if (Player.SHunger == 10){
   	 out.write("10" + s);
     }
     }
     
     
     if (Game.Time == 2){
     out.write("2" + s);
     
     if (Player.SHealth == 1){
     out.write("1" + s);
     }
     if (Player.SHealth == 2){
   	 out.write("2" + s);
     }
     if (Player.SHealth == 3){
   	 out.write("3" + s);
     }
     if (Player.SHealth == 4){
   	 out.write("4" + s);
     }
     if (Player.SHealth == 5){
   	 out.write("5" + s);
     }
     if (Player.SHealth == 6){
   	 out.write("6" + s);
     }
     if (Player.SHealth == 7){
   	 out.write("7" + s);
     }
     if (Player.SHealth == 8){
   	 out.write("8" + s);
     }
     if (Player.SHealth == 9){
   	 out.write("9" + s);
     }
     if (Player.SHealth == 10){
   	 out.write("10" + s);
     }
     if (Player.SHunger < 1){
     out.write("0" + s);
     }
     if (Player.SHunger == 1){
     out.write("1" + s);
     }
     if (Player.SHunger == 2){
   	 out.write("2" + s);
     }
     if (Player.SHunger == 3){
   	 out.write("3" + s);
     }
     if (Player.SHunger == 4){
   	 out.write("4" + s);
     }
     if (Player.SHunger == 5){
   	 out.write("5" + s);
     }
     if (Player.SHunger == 6){
   	 out.write("6" + s);
     }
     if (Player.SHunger == 7){
   	 out.write("7" + s);
     }
     if (Player.SHunger == 8){
   	 out.write("8" + s);
     }
     if (Player.SHunger == 9){
   	 out.write("9" + s);
     }
     if (Player.SHunger == 10){
   	 out.write("10" + s);
     }
     }
     
     
     if (Game.Time == 3){
     out.write("3" + s);
     
     if (Player.SHealth == 1){
     out.write("1" + s);
     }
     if (Player.SHealth == 2){
   	 out.write("2" + s);
     }
     if (Player.SHealth == 3){
   	 out.write("3" + s);
     }
     if (Player.SHealth == 4){
   	 out.write("4" + s);
     }
     if (Player.SHealth == 5){
   	 out.write("5" + s);
     }
     if (Player.SHealth == 6){
   	 out.write("6" + s);
     }
     if (Player.SHealth == 7){
   	 out.write("7" + s);
     }
     if (Player.SHealth == 8){
   	 out.write("8" + s);
     }
     if (Player.SHealth == 9){
   	 out.write("9" + s);
     }
     if (Player.SHealth == 10){
   	 out.write("10" + s);
     }
     if (Player.SHunger < 1){
     out.write("0" + s);
     }
     if (Player.SHunger == 1){
     out.write("1" + s);
     }
     if (Player.SHunger == 2){
   	 out.write("2" + s);
     }
     if (Player.SHunger == 3){
   	 out.write("3" + s);
     }
     if (Player.SHunger == 4){
   	 out.write("4" + s);
     }
     if (Player.SHunger == 5){
   	 out.write("5" + s);
     }
     if (Player.SHunger == 6){
   	 out.write("6" + s);
     }
     if (Player.SHunger == 7){
   	 out.write("7" + s);
     }
     if (Player.SHunger == 8){
   	 out.write("8" + s);
     }
     if (Player.SHunger == 9){
   	 out.write("9" + s);
     }
     if (Player.SHunger == 10){
   	 out.write("10" + s);
     }
     }
     
     
     


     out.close();
     outm.close();
     }catch (Exception e){//Catch exception if any
     System.err.println("Error: " + e.getMessage());
     }
     }
	
    
    



	public void tick() {
		
		 if (input.attack.clicked) {
			if (PauseMenu.title == true) {
				game.setMenu(new TitleMenu());

			}
			if (PauseMenu.newGame == true) {
				DeadMenu.shudrespawn = false;
				
				game.setMenu(new LoadingMenu());
			}
			
		}
		 if (PauseMenu.title == true) {
				nameText = "Quit to title?";
			}
			if (PauseMenu.newGame == true) {
				nameText = "Start a new game?";
			}
			if (PauseMenu.respawn == true) {
				nameText = "Respawn?";
			}
		 if (input.menu.clicked) {
			 game.setMenu(null);
		 }
		 
		//This is so that if the user presses x @ respawn menu, they respawn (what a concept)
		//if (input.)
	}
	
	public void render(Screen screen) {
		Font.renderFrame(screen, "", 1, 1, 32, 13);
		
		
		Font.draw("Are you sure you want to", screen, 2 * 8, 2 * 8, Color.get(-1, 333, 333, 333));
		Font.draw("" + nameText, screen, 2 * 8, 3 * 8, Color.get(-1, 333, 333, 333));

		if (PauseMenu.respawn == true) {
			Font.draw("(levels,inventory will be lost)", screen, 2 * 8, 4 * 8, Color.get(-1, 333, 333, 333));
		}
		if (PauseMenu.newGame == true) {
			Font.draw("(all data will be lost)", screen, 2 * 8, 4 * 8, Color.get(-1, 1000, 0, 0));
		}
		if (PauseMenu.title == true) {
			Font.draw("(all data will be lost)", screen, 2 * 8, 4 * 8, Color.get(-1, 1000, 0, 0));
		}
		
		Font.draw("C:Yes!", screen, 2 * 8, 6 * 8, Color.get(-1, 333, 333, 333));
		Font.draw("X:No!", screen, 9 * 8, 6 * 8, Color.get(-1, 333, 333, 333));
		
		
	}
}