package nnhw2;

import java.io.*;
import java.math.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import nnhw2.Paint;

//only can do xor.txt fukk

public class nnhw2 extends JFrame {

	static int frameSizeX = 800;
	static int frameSizeY = 800;
	static int neuralAmount = 3;
	static int x0 = -1;
	
	static float studyRate = 0.5f;

	static ArrayList<float[]> inputArray = new ArrayList<float[]>();
	static ArrayList<float[]> sortedArray = new ArrayList<float[]>();
	static ArrayList<float[]> tempArray = new ArrayList<float[]>();
	static ArrayList<float[]> trainArray = new ArrayList<float[]>();
	static ArrayList<float[]> testArray = new ArrayList<float[]>();
	static ArrayList<float[]> initialWeight = new ArrayList<float[]>();

	static float[] yOutput = new float[neuralAmount];

	static int sortedNewDesire = 0;

	static float[] yOutputArea;
	static float[] desireArea;
	static float[] gradient = new float[neuralAmount];
	static float[] errorFunction = new float[neuralAmount];
//	static ArrayList<Float> errorFunction=new ArrayList<Float>();

	public static void inputFileChoose(String[] args) throws IOException {

		String FileName = "/Users/Terry/Documents/workspace/datasets/hw2/iris.txt";
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

	private static void normalizeData(){
		for (int i = 0; i < inputArray.size(); i++) {
			for (int j = 0; j < inputArray.get(i).length; j++) {
					inputArray.get(i)[j]/=1000;
			}
		}
	}

	public static void sortInputArray(ArrayList<float[]> inputArray) {
		/*
		 * 1. set loop times = inputArray's dataamount 2. in while loop we have
		 * to dynamic change loop times cause we had remove some data in the
		 * array to reduce loop times 3. set a variable-standardDesire is mean
		 * the first data's desire , then use it to check one by one ,if found
		 * someone is as same as the standardDesire, put this data to
		 * sortedArray, so on ,we can get a sorted array which's desire is from
		 * 1 to number of class 4. everytime move a item to sortedArray , raise
		 * iRestFlag and set i to 0, then it will run loop from beginning 5.
		 * when inputarray left only 1 item must set as -1, or the last data's
		 * desire will be set one more number
		 * 
		 */
		int inputArraySize = inputArray.size();
		int iRestFlag = 0;
		System.out.println("--------- Start sort ---------");
		System.out.println("This is inputarray's size : " + inputArraySize);
		whileloop: while (true) {
			// set the first one's desire as standard
			int standardDesire = (int) inputArray.get(0)[inputArray.get(0).length - 1];
			System.out.println("Now the standartDesire is  : " + standardDesire);

			for (int i = 0; i < inputArray.size(); i++) {
				if (iRestFlag == 1) {
					i = 0;
				}
				if ((int) inputArray.get(i)[inputArray.get(i).length - 1] == standardDesire) {
					inputArray.get(i)[inputArray.get(i).length - 1] = sortedNewDesire;
					sortedArray.add(inputArray.get(i));
					inputArray.remove(i);
					iRestFlag = 1;
				} else {
					iRestFlag = 0;
				}
				if (inputArray.size() == 1) {// the last data need set i=-1 to
												// prevent after forloop's i++
					i = -1;
				}
			}
			if (inputArray.size() == 0) {
				System.out.println("--------- Sort done! ---------");
				System.out.println("");
				break whileloop;
			} else {
				sortedNewDesire++;// count desire
			}
		}
		System.out.println("The max sorted desire : " + sortedNewDesire);
	}

	private static void putInputToTemp(ArrayList<float[]> sorteArray) {
		int arrayInputAmount = sortedArray.size();
		Random rand = new Random();
		while (arrayInputAmount != 0) {
			int n = rand.nextInt(arrayInputAmount) + 0;
			tempArray.add(sortedArray.get(n));
			sortedArray.remove(n);// del input to prevent get same data
			arrayInputAmount--;
		}

	}

	private static void separateTemp(ArrayList<float[]> tempArray) {

		int totalamount = tempArray.size();
		int tocalamount = Math.round((float) (totalamount * 2) / 3);
		int totestamount = totalamount - tocalamount;

		while (tocalamount != 0) {
			trainArray.add(tempArray.get(0));
			tempArray.remove(0);
			tocalamount--;
		}
		System.out.println("train amount : " + trainArray.size());
		while (totestamount != 0) {
			testArray.add(tempArray.get(0));
			tempArray.remove(0);
			totestamount--;
		}
		System.out.println("test amount : " + testArray.size());
	}

	public static void generateInitialWeight() {
		/*
		 * not only can generate postive value , also can get negtive value
		 */
		System.out.println("--------------------------------------------------");
		Random rand = new Random();
		for (int i = 0; i < neuralAmount; i++) {
			float[] token = new float[trainArray.get(0).length];
			for (int j = 0; j < trainArray.get(0).length; j++) {
				if (Math.random() > 0.5) {
					token[j] = rand.nextFloat() + 0f;
					System.out.println("weight : " + token[j]);
				} else {
					token[j] = rand.nextFloat() - 1f;
					System.out.println("weight : " + token[j]);
				}
			}
			initialWeight.add(token);
		}
		System.out.println("--------------------------------------------------");
	}

	public static void calOutputArea() {
		/*
		 * get output bound that from 0 to 1
		 */
		int classAmount = sortedNewDesire + 1;
		yOutputArea = new float[classAmount + 1];

		for (int i = 0; i <= classAmount; i++) {
			if (i == 0) {
				yOutputArea[i] = 0f;
			} else {
				// get two decimal places
				yOutputArea[i] = (float) (Math.round((float) (1 * i) / classAmount * 100)) / 100;
			}
		}
		for (int i = 0; i < yOutputArea.length; i++) {
			System.out.println("yOutputBound" + i + " : " + yOutputArea[i]);
		}
	}
	
	public static void calDesireArea() {
		/*
		 * get output bound that from 0 to 1
		 */
		int classAmount = sortedNewDesire + 1;
		desireArea = new float[classAmount];

		for (int i = 0; i < classAmount; i++) {
			if (i == 0) {
				desireArea[i] = 0f;
			} else {
				// get two decimal places
				desireArea[i] = (float) (Math.round((float) i / (classAmount-1) * 100)) / 100;
			}
		}
		for (int i = 0; i < desireArea.length; i++) {
			System.out.println("desireAreaBound" + i + " : " + desireArea[i]);
		}
	}

	public static void calOutputValue(ArrayList<float[]> array, ArrayList<float[]> initialWeight) {
		/*
		 * 1. use for to run neuralAmount times to get y 
		 * 2. when its last loop get last output 
		 * 3. use yOutput which generated by upper loop and do cal with weight
		 *    to get z the last output notice : for(j) loop's yOutput[j-1] 
		 *    cause must fetch value from the first value 
		 * 4. the latest value of yOutput is outputz 
		 * 5. use a flag to detect classify correct or not
		 *    how to decide :?  
		 *    use yOutputArea to detect the data in right area or not
		 *    then throw desireArea to cal gradient and tune weight
		 * 6. if classify fail cal gradient and tune weight
		 */
		
		// looptimes count , still need to add rmse 
		int noOfData = 0;
		int classifyFlage = 0;
		int looptimes=0;		
		loop: 
		while (true) {
			int desire = (int) array.get(noOfData)[array.get(noOfData).length - 1];
			System.out.println("this is dataamount : " + noOfData);
			// youtput size wrong and initial size wrong 3!=14
			
			for (int i = 0; i < neuralAmount; i++) {
				if (i != neuralAmount - 1) {
					float sum = 0f;
					sum = x0 * initialWeight.get(i)[0];
					for (int j = 0; j < array.get(noOfData).length - 1; j++) {
						sum += array.get(noOfData)[j] * initialWeight.get(i)[j + 1];

					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sum)));
					System.out.println("y" + i + " output is : " + yOutput[i]);
				} else {
					float sumZ = 0f;
					sumZ = x0 * initialWeight.get(i)[0];
					for (int j = 0; j < yOutput.length - 1; j++) {
						sumZ += yOutput[j] * initialWeight.get(i)[j + 1];// match
																			// right
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sumZ)));
					System.out.println("y" + i + "(z) output is : " + yOutput[i]);
					
					// check classify area correct or not use a range bound (yOutputArea)
					if (yOutput[i] > yOutputArea[desire] && yOutput[i] <= yOutputArea[desire + 1]) {
						System.out.println("Correct classify");
						classifyFlage = 1;
					} else {
						System.out.println("Error clssify");
						classifyFlage = 0;
						//try store error message for rmse
						/*
						float errorTemp=desireArea[desire]-yOutput[i];
						errorFunction[noOfData]=errorTemp;
						*/
					}
 				}
			}
			// throw desireArea to gradient 
			if (classifyFlage == 0) {
				calculateGradient(desireArea[desire]);
				tuneWeight(noOfData, array);
			}
						
			System.out.println("---------------------------------------------------------");
	
			if(noOfData==array.size()-1){
				noOfData=0;
			}
			else{			
				noOfData++;
			}
			looptimes ++;
			if(looptimes>10000){
				System.out.println("out of looptimes");
				break loop;
			}
			
		}

	}

	private static float calRMSE(){
		/*idea: continue return errorSum and if the return value
		 *      if the value is not good at all the loop will 
		 *      run again & again
		 */
		System.out.println("----- try to cal error function -----");
		float errorSum=0;
		for(int i=0;i<errorFunction.length;i++){
			errorSum = errorSum + (errorFunction[i] * errorFunction[i]);
		}	
		errorSum /= 2;
		System.out.println("Test RMSE : "+errorSum);
		
		return errorSum;
	}
	
	private static void calculateGradient(float desire) {
		/*
		 * 1. declare count is neuralAmount-1 for array use 
		 * 2. while loop continue -- 
		 * 3. output layer's gradient calculation is different from hidden layer
		 */
		int countdown = neuralAmount - 1;
		while (countdown != -1) {
			if (countdown == neuralAmount - 1) {
				gradient[countdown] = (desire - yOutput[countdown]) * yOutput[countdown] * (1 - yOutput[countdown]);
			} else {
				gradient[countdown] = yOutput[countdown] * (1 - yOutput[countdown]) * gradient[neuralAmount - 1]
						* initialWeight.get(neuralAmount - 1)[countdown + 1];
			}
			countdown--;
		}
		System.out.println("gradient : ");
		for (int i = 0; i < gradient.length; i++) {
			System.out.println(gradient[i]);
		}
	}

	private static void tuneWeight(int noOfData, ArrayList<float[]> array) {
		/*
		 * 1. cause weight is a dim 2 matrix so we use 2 for loop to pack it 
		 * 2. but we use a if to separate with hidden and output layer 
		 * 3. in the calculation--notice: gradient[i] is i not j
		 */
		for (int i = 0; i < initialWeight.size(); i++) {
			if (i != initialWeight.size() - 1) {
				for (int j = 0; j < initialWeight.get(i).length; j++) {
					if (j == 0) {
						initialWeight.get(i)[j] += studyRate * gradient[i] * x0;
					} else {
						initialWeight.get(i)[j] += studyRate * gradient[i] * array.get(noOfData)[j - 1];
					}
				}
			} else {
				for (int j = 0; j < initialWeight.get(i).length; j++) {
					if (j == 0) {
						initialWeight.get(i)[j] += studyRate * gradient[i] * x0;
					} else {
						initialWeight.get(i)[j] += studyRate * gradient[i] * yOutput[j - 1];
					}
				}
			}

		}
		System.out.println("tune weight : ");
		printArrayData(initialWeight);
	}

	private static void checkWeight(){
		
		System.out.println("Show final weight ");
		
		printArrayData(initialWeight);
		
		System.out.println("-------------------- now start to check --------------------");
		
		printArrayData(trainArray);

		int noOfData = 0;
		int count=0;
		
		loop: 
		while (true) {
			int desire = (int) trainArray.get(noOfData)[trainArray.get(noOfData).length - 1];
			System.out.println("this is dataamount : " + noOfData);
			
			for (int i = 0; i < neuralAmount; i++) {
				if (i != neuralAmount - 1) {
					float sum = 0f;
					sum = x0 * initialWeight.get(i)[0];
					for (int j = 0; j < trainArray.get(noOfData).length - 1; j++) {
						sum += trainArray.get(noOfData)[j] * initialWeight.get(i)[j + 1];
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sum)));
					System.out.println("y" + i + " output is : " + yOutput[i]);
				} else {
					float sumZ = 0f;
					sumZ = x0 * initialWeight.get(i)[0];
					for (int j = 0; j < yOutput.length - 1; j++) {
						sumZ += yOutput[j] * initialWeight.get(i)[j + 1];// match
																			// right
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sumZ)));
					System.out.println("y" + i + "(z) output is : " + yOutput[i]);
							
					// check classify area correct or not use a range bound (yOutputArea)
					if (yOutput[i] > yOutputArea[desire] && yOutput[i] <= yOutputArea[desire + 1]) {
						System.out.println("Correct classify");
						count++;
					} else {
						System.out.println("Error clssify");
						//try store error message
					}
 				}
			}

			System.out.println("---------------------------------------------------------");
			
			if(noOfData==trainArray.size()-1){
				break loop;
			}
			else{			
				noOfData++;
			}			
		}
		System.out.println("total train array amount : "+trainArray.size());
		System.out.println("correct amount count : "+count);
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
	
	public static void main(String[] args) throws IOException {
		/*
		 * 1. choose input file
		 * 2. sort the desire , begin from 0 to classamount-1
		 * 3. random input data
		 * 4. separate data as 2/3 for train 1/3 for test
		 * 5. generate random initial weight
		 * 6. calculate the output's area to check y in the right area or not
		 * 7. calculate the  desire's area for tune weight
		 * 8. start to calculate the output value and generate gradient value
		 * 	  to tune weight
		 * 9. print final weight
		 * 10. do cal again to check the final weight is correct
		 * 11. GUI interface
		 */
		inputFileChoose(args);
		
		normalizeData();

		sortInputArray(inputArray);

		putInputToTemp(sortedArray);// copy to temp with random

		separateTemp(tempArray);// separate to train and test set,set 2/3 as
								// train set 1/3 as test set
		
//		System.out.println("trainArray's data : ");
//		printArrayData(trainArray); 
//		System.out.println("testArray's data : " );
//		printArrayData(testArray);

		generateInitialWeight();
		
		System.out.println("----------the initial weight size : "+initialWeight.size());
		System.out.println("----------the intital 1 weight length : "+initialWeight.get(0).length);

		calOutputArea();

		calDesireArea();
		
		calOutputValue(trainArray, initialWeight);
				
		//check the weight correct
		checkWeight();
		
		//genarateFrame(trainArray, sortedNewDesire+1);
	}

	public static void printArrayData(ArrayList<float[]> showArray) {
		for (int i = 0; i < showArray.size(); i++) {
			for (int j = 0; j < showArray.get(i).length; j++) {
				System.out.print(showArray.get(i)[j] + "\t");
			}
			System.out.println("");
		}
		System.out.println("");
	}

}