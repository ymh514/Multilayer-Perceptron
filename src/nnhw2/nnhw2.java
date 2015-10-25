package nnhw2;

import java.io.*;
import java.math.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import nnhw2.Paint;

public class nnhw2 extends JFrame {

	static int frameSizeX = 800;
	static int frameSizeY = 800;

	static ArrayList<Integer> classTypes = new ArrayList<Integer>();
	static ArrayList<float[]> inputArray = new ArrayList<float[]>();
	static ArrayList<float[]> sortedArray = new ArrayList<float[]>();

	public static void printArrayData(ArrayList<float[]> showArray) {
		for (int i = 0; i < showArray.size(); i++) {
			for (int j = 0; j < showArray.get(i).length; j++) {
				System.out.print(showArray.get(i)[j] + "\t");
			}
			System.out.println("");
		}
	}

	private static int countClass(ArrayList<float[]> inputArray) {
		/*
		 * 1. put first class's type into classTypes 1st place 2. if next line's
		 * class diffrent with 1st class so go on next if 3. search classTypes's
		 * all class to judge if all are diffrent rais addFlag 4. if addFlag
		 * raised, add this new class into classType
		 */
		int addFlag = 0;
		classTypes.add((int) inputArray.get(0)[(inputArray.get(0).length) - 1]);

		for (int i = 0; i < inputArray.size(); i++) {
			if (classTypes.get(0) != (int) inputArray.get(i)[(inputArray.get(i).length) - 1]) {
				for (int j = 0; j < classTypes.size(); j++) {
					if (classTypes.get(j) != inputArray.get(i)[(inputArray.get(0).length) - 1]) {
						addFlag = 1;
					} else {
						addFlag = 0;
					}
				}
				if (addFlag == 1) {
					classTypes.add((int) inputArray.get(i)[(inputArray.get(0).length) - 1]);
				}
			}
		}
		return classTypes.size();
	}

	private static void genarateFrame(ArrayList<float[]> inputArray, int countClass) {
		JFrame frame = new JFrame();

		frame.setVisible(true);// just set visible
		frame.setLocation(100, 100);// set the frame show location
		frame.setSize(frameSizeX, frameSizeY);// set the frame size
		frame.setResizable(false);

		Paint trypaint = new Paint(inputArray, countClass);
		frame.add(trypaint);// add paint(class) things in to the frame
	}

	public static void inputFileChoose(String[] args) throws IOException {

		String FileName = "C:\\Users\\Terry\\Desktop\\nnhw2dataset\\wine.txt";
		FileReader fr = new FileReader(FileName);
		BufferedReader br = new BufferedReader(fr);// 在br.ready反查輸入串流的狀況是否有資料

		String txt;
		while ((txt = br.readLine()) != null) {
			/*
			 * If there is space before split(), it will cause the error So, we
			 * could to use trim() to remove the space at the beginning and the
			 * end. Then split the result, which doesn't include the space at
			 * the beginning and the end. "\\s+" would match any of space, as
			 * you don't have to consider the number of space in the string
			 */
			String[] token = txt.trim().split("\\s+");// <-----背起來
			// String[] token = txt.split(" ");//<-----original split
			float[] token2 = new float[token.length];// 宣告float[]

			try {
				for (int i = 0; i < token.length; i++) {
					token2[i] = Float.parseFloat(token[i]);
				} // 把token(string)轉乘token2(float)
				inputArray.add(token2);// 把txt裡面內容先切割過在都讀進array內
			} catch (NumberFormatException ex) {
				System.out.println("Sorry Error...");
			}
		}
		fr.close();// 關閉檔案

	}

	public static void sortInputArray(ArrayList<float[]> inputArray) {
		/*
		 * 1. set loop times = inputArray's dataamount 
		 * 2. in while loop we have to dynamic change loop times cause we had 
		 * 	remove some data in the array to reduce loop times 
		 * 3. set a variable-standardDesire is mean the first data's desire ,
		 *  then use it to check one by one ,if found someone is as same as 
		 *  the standardDesire, put this data to sortedArray, so on ,we can get a
		 *  sorted array which's desire is from 1 to number of class
		 * 4. everytime move a item to sortedArray , raise iRestFlag and set i to
		 * 	0, then it will run loop from beginning 
		 * 5. when inputarray left only 1 item must set as -1, or the last data's
		 * 	desire will be set one more number
		 *  
		 */
		int inputArraySize = inputArray.size();
		int sortedNewDesire =0;
		int iRestFlag=0;
		System.out.println("--------- Start sort ---------");
		System.out.println("This is inputarray's size : "+inputArraySize);
		whileloop:
		while (true) {
			int standardDesire = (int) inputArray.get(0)[inputArray.get(0).length - 1];// set the first one's desire as standard
			System.out.println("Now the standartDesire is  : "+standardDesire);
			
			for (int i = 0; i < inputArray.size(); i++) {
				if(iRestFlag ==1){
					i=0;
				}
				if ((int)inputArray.get(i)[inputArray.get(i).length - 1] == standardDesire) {
					inputArray.get(i)[inputArray.get(i).length - 1]=sortedNewDesire;
					sortedArray.add(inputArray.get(i));
					inputArray.remove(i);
					iRestFlag = 1;
				}
				else{
					iRestFlag =0;
				}
				if(inputArray.size()==1){//the last data need set i=-1 to prevent after forloop's i++
					i=-1;
				}
			}
			if(inputArray.size()==0){
				System.out.println("Sort done !");
				break whileloop;
			}
			else{
				sortedNewDesire ++;//count desire 
			}
		}
		System.out.println("The max sorted desire : "+sortedNewDesire);
	}

	public static void main(String[] args) throws IOException {

		inputFileChoose(args);


//		int countAmount = countClass(inputArray);
//		printArrayData(inputArray);

		sortInputArray(inputArray);
		printArrayData(sortedArray);

//		genarateFrame(sortedArray, countAmount);
	}

}
