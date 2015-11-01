package nnhw2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JPanel;


public class Paint extends JPanel {

	private ArrayList<float[]> trainArray = new ArrayList<float[]>();
	private ArrayList<Color> colorArray = new ArrayList<Color>();
	private int classAmount;

	public Paint(ArrayList<float[]> trainArray, int classAmount) {
		this.trainArray = trainArray;
		this.classAmount = classAmount;
	}

	Color red = new Color(255, 0, 0);
	Color green = new Color(0, 255, 0);
	Color blue = new Color(0, 0, 255);
	Color black = new Color(0, 0, 0);
	Color white = new Color(255,255,255);

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		/*
		 * 1. this function can detect numbers of class then generate the
		 * 	numbers of class
		 * 2. stored red,blue,green in colorArray and if classAmount > 3 
		 *  do generate color function 
		 * 3. do calculate with r,g,b the function can generate
		 * 	about 25*8*6 difference color 
		 * 4. then put these color into a arraylist named colorArray 
		 * 	,so we can fetch the color in this arraylist
		 */
		
		for (int i = 0; i < trainArray.size(); i++) {
			for (int j = 0; j < trainArray.get(i).length; j++) {
				System.out.print(trainArray.get(i)[j] + "\t");
			}
			System.out.println("");
		}
		//generate 3 color first
		colorArray.add(red);
		colorArray.add(blue);
		colorArray.add(green);
		// if class amount > 3 do under function
		if(classAmount>3){
			int colorR = 100;
			int colorG = 50;
			int colorB = 0;
	
			for (int i = 3; i < classAmount; i++) {
	
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
		}
		
		for(int i=0; i<classAmount ; i++){
			for(int j=0;j<trainArray.size();j++){
				if(trainArray.get(j)[trainArray.get(j).length-1]==i){
					g.setColor(colorArray.get(i));
					g.fillOval((Math.round(398 + (trainArray.get(j)[0]) * 16)),
							Math.round((398 + (-trainArray.get(j)[1]) * 16)), 4, 4);
					
				}
			}
		}
		
		/* test multicolor
		for(int i=0; i<classAmount;i++){
		  g.setColor(colorArray.get(i)); g.drawLine(10+10*i, 500+10*i, 500+10*i,0+10*i); 
		}
		*/
		
		
		g.setColor(black);
		g.drawLine(400, 0, 400, 800);
		g.drawLine(0, 400, 800, 400);

	}
}