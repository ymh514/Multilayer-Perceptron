package nnhw2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JPanel;

public class Paint extends JPanel {

	private ArrayList<float[]> inputArray = new ArrayList<float[]>();
	private ArrayList<Integer> classTypes = new ArrayList<Integer>();
	private ArrayList<Color> colorArray = new ArrayList<Color>();
	private int countAmount;

	public Paint(ArrayList<float[]> inputArray, int countAmount) {
		this.inputArray = inputArray;
		this.countAmount = countAmount;
		this.classTypes = classTypes;
	}

	Color red = new Color(255, 0, 0);
	Color green = new Color(0, 255, 0);
	Color blue = new Color(0, 0, 255);
	Color black = new Color(0, 0, 0);

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		/*
		 * 1. this function can detect numbers of class then genarate the
		 * 	numbers of class 
		 * 2. do calculate with r,g,b the function can genarate
		 * 	about 25*8*6 diffrence color 
		 * 3. then put these color into a arraylist named colorArray 
		 * 	,so we can fetch the color in this arraylist
		 */
		System.out.println("this come hereeeeeee   " + countAmount);
		int colorR = 50;
		int colorG = 50;
		int colorB = 50;

		for (int i = 0; i < countAmount; i++) {

			colorR += 10;
			if (colorR > 255) {
				colorR -= 255;
			}

			colorG += 30;
			if (colorG > 255) {
				colorG -= 255;
			}

			colorB += 40;
			if (colorB > 255) {
				colorB -= 255;
			}

			Color colorType = new Color(colorR, colorG, colorB);
			colorArray.add(colorType);
		}
		/*
		 * test can draw multicolor for(int i=0; i<countAmount;i++){
		 * g.setColor(colorArray.get(i)); g.drawLine(10+10*i, 500+10*i,
		 * 500+10*i,0+10*i); }
		 */

		g.setColor(black);
		g.drawLine(400, 0, 400, 800);
		g.drawLine(0, 400, 800, 400);

	}
}
