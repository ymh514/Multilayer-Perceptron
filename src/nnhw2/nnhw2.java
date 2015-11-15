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
	static int neuralAmount = 14;
	static int x0 = -1;
	static int looptimeLimit = 10000000;
	static int looptimes = 0;
	static float errorLimit = 0.001f;
	static int correctCount = 0;
	static int sortedNewDesire = 0;

	static float studyRate = 0.8f;
	static float alpha = 0.5f;

	static ArrayList<float[]> inputArray = new ArrayList<float[]>();
	static ArrayList<float[]> sortedArray = new ArrayList<float[]>();
	static ArrayList<float[]> tempArray = new ArrayList<float[]>();
	static ArrayList<float[]> trainArray = new ArrayList<float[]>();
	static ArrayList<float[]> testArray = new ArrayList<float[]>();
	static ArrayList<float[]> initialWeight = new ArrayList<float[]>();
	static ArrayList<float[]> lastWeight = new ArrayList<float[]>();
	static ArrayList<Float> rmse=new ArrayList<Float>();

	static float[] yOutput = new float[neuralAmount];
	static float[] yOutputArea;
	static float[] desireArea;
	static float[] gradient = new float[neuralAmount];
	static float[] errorFunction = new float[1];

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
			int standardDesire = (int) inputArray.get(0)[inputArray.get(0).length - 1];// set
																						// the
																						// first
																						// one's
																						// desire
																						// as
																						// standard
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
					//System.out.println("weight : " + token[j]);
				} else {
					token[j] = rand.nextFloat() - 1f;
					//System.out.println("weight : " + token[j]);
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
		//int looptimes=0;
		loop: 
		while (true) {
			int desire = (int) array.get(noOfData)[array.get(noOfData).length - 1];
			//System.out.println("this is dataamount : " + noOfData);
			//System.out.println("this is the desire want : "+desireArea[desire]);
			
			for (int i = 0; i < neuralAmount; i++) {
				if (i != neuralAmount - 1) {
					float sum = 0f;
					sum = x0 * initialWeight.get(i)[0];
					for (int j = 0; j < array.get(noOfData).length - 1; j++) {
						sum += array.get(noOfData)[j] * initialWeight.get(i)[j + 1];
					}
					//System.out.println("sum sum sum sum : "+sum);
					yOutput[i] = (float) (1 / (1 + Math.exp(-sum)));
					//**************************************************************
					//yOutput[i] = yOutput[i]*(desireArea[desireArea.length-1]-desireArea[0])+desireArea[0];
					//**************************************************************

					//System.out.println("y" + i + " output is : " + yOutput[i]);
					
				} else {
					float sumZ = 0f;
					sumZ = x0 * initialWeight.get(i)[0];
					for (int j = 0; j < yOutput.length - 1; j++) {
						sumZ += yOutput[j] * initialWeight.get(i)[j + 1];// match
																			// right
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sumZ)));
					//**************************************************************
					//yOutput[i] = yOutput[i]*(desireArea[desireArea.length-1]-desireArea[0])+desireArea[0];
					//**************************************************************

					//System.out.println("y" + i + "(z) output is : " + yOutput[i]);
					
					
					// check classify area correct or not use a range bound (yOutputArea)
					if (yOutput[i] > yOutputArea[desire] && yOutput[i] <= yOutputArea[desire + 1]) {
						//System.out.println("Correct classify");
						//classifyFlage = 1;
						correctCount++;
					} else {
						//System.out.println("Error clssify");
						//classifyFlage = 0;
					}
					
					errorFunction[0]=0;
					errorFunction[0]=(desireArea[desire]-yOutput[i]);
 				}
			}
			calenRMSE();
			// throw desireArea to gradient 
			//if(classifyFlage==0){
			calculateGradient(desireArea[desire]);
			tuneWeight(noOfData, array);
			//}
			
			//System.out.println("---------------------------------------------------------");
			
			if(noOfData==array.size()-1){
				noOfData=0;
				looptimes ++;
				float ratio = (float)correctCount/array.size();
				if(ratio==1){
					System.out.println("correct ratio 100%");
				//	break loop;
				}
				System.out.println("The correct ratio is : " + ratio*100+"%");
				if(caleavRMSE()<errorLimit){
					System.out.println("find eavrmse < "+errorLimit);
					break loop;
				}
				rmse.removeAll(rmse);
				correctCount=0;
			}
			else{			
				noOfData++;
			}
			if(looptimes>looptimeLimit){
				System.out.println("out of looptimes");
				break loop;
			}
		}

	}

	private static float calenRMSE(){
		/*idea: continue return errorSum and if the return value
		 *      if the value is not good at all the loop will 
		 *      run again & again
		 */
//		System.out.println("----- try to cal error function -----");
		float errorSum=0;
	
		errorSum = errorFunction[0]*errorFunction[0];
		
		errorSum /= 2;
		rmse.add(errorSum);
//		System.out.println("Test RMSE : "+errorSum);
		return errorSum;
	}
	
	private static float caleavRMSE(){
		float sumrmse=0f;
		for(int i=0;i<rmse.size();i++){
			sumrmse += rmse.get(i);
		}
		sumrmse = sumrmse/rmse.size();
		System.out.println("均方誤差:: " + sumrmse);
		return sumrmse;
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
		/*
		System.out.println("gradient : ");
		for (int i = 0; i < gradient.length; i++) {
			System.out.println(gradient[i]);
		}
		*/
	}

	private static void tuneWeight(int noOfData, ArrayList<float[]> array) {
		/*
		 * 1. cause weight is a dim 2 matrix so we use 2 for loop to pack it 
		 * 2. but we use a if to separate with hidden and output layer 
		 * 3. in the calculation--notice: gradient[i] is i not j
		 */
		float tuneWeight = 0f;
		for (int i = 0; i < initialWeight.size(); i++) {
			if (i != initialWeight.size() - 1) {
				for (int j = 0; j < initialWeight.get(i).length; j++) {
					if (j == 0) {
						tuneWeight = alpha*lastWeight.get(i)[j]+studyRate * gradient[i] * x0;
						initialWeight.get(i)[j] += tuneWeight;
						lastWeight.get(i)[j]=tuneWeight;
					} else {
						tuneWeight = alpha*lastWeight.get(i)[j]+studyRate * gradient[i] * array.get(noOfData)[j - 1];
						initialWeight.get(i)[j] += tuneWeight;
						lastWeight.get(i)[j]=tuneWeight;
					}
				}
			} else {
				for (int j = 0; j < initialWeight.get(i).length; j++) {
					if (j == 0) {
						tuneWeight = alpha*lastWeight.get(i)[j]+(studyRate * gradient[i] * x0);
						initialWeight.get(i)[j] += tuneWeight;
						lastWeight.get(i)[j]=tuneWeight;
						
					} else {
						tuneWeight = alpha*lastWeight.get(i)[j]+(studyRate * gradient[i] * yOutput[j - 1]);
						initialWeight.get(i)[j] += tuneWeight;
						lastWeight.get(i)[j]=tuneWeight;
						
					}
				}
			}

		}
		/*
		System.out.println("tune weight : ");
		printArrayData(initialWeight);
		*/
	}

	private static void checkWeight(ArrayList<float[]> array){
		
		//System.out.println("Show final weight ");
		
		//printArrayData(initialWeight);
		
		//System.out.println("-------------------- now start to check --------------------");
		
		//printArrayData(trainArray);
		
		
		int noOfData = 0;
		int correctCount = 0;
		int errorCount = 0;
		loop: 
		while (true) {
			int desire = (int) array.get(noOfData)[array.get(noOfData).length - 1];
			System.out.println("this is dataamount : " + noOfData);
			
			for (int i = 0; i < neuralAmount; i++) {
				if (i != neuralAmount - 1) {
					float sum = 0f;
					sum = x0 * initialWeight.get(i)[0];
					for (int j = 0; j < array.get(noOfData).length - 1; j++) {
						sum += array.get(noOfData)[j] * initialWeight.get(i)[j + 1];
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sum)));
					//System.out.println("y" + i + " output is : " + yOutput[i]);
				} else {
					float sumZ = 0f;
					sumZ = x0 * initialWeight.get(i)[0];
					for (int j = 0; j < yOutput.length - 1; j++) {
						sumZ += yOutput[j] * initialWeight.get(i)[j + 1];// match
																			// right
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sumZ)));
					//System.out.println("y" + i + "(z) output is : " + yOutput[i]);
							
					// check classify area correct or not use a range bound (yOutputArea)
					if (yOutput[i] > yOutputArea[desire] && yOutput[i] <= yOutputArea[desire + 1]) {
						System.out.println("Correct classify");
						correctCount++;
					} else {
						System.out.println("###  Error clssify");
						errorCount++;
						//try store error message
					}
 				}
			}

			System.out.println("---------------------------------------------------------");
			if(noOfData==array.size()-1){
				break loop;
			}
			else{			
				noOfData++;
			}			
		}
		float ratio = (float)correctCount/array.size();
		System.out.println("This is correct ratio :　"+(ratio*100) + "%");
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

	private static void normalizeData(){
		for (int i = 0; i < inputArray.size(); i++) {
			float max = Float.MIN_VALUE;
			for (int j = 0; j < inputArray.get(i).length-1; j++) {
				if(Math.abs(inputArray.get(i)[j])>max){
					max = Math.abs(inputArray.get(i)[j]);
				}
			}
			for (int k = 0; k < inputArray.get(i).length-1; k++) {
				inputArray.get(i)[k] /= max;
			}
		}
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
		long startTime = System.currentTimeMillis();
		inputFileChoose(args);
		
		normalizeData();

		sortInputArray(inputArray);
				
		putInputToTemp(sortedArray);// copy to temp with random

		separateTemp(tempArray);// separate to train and test set,set 2/3 as
								// train set 1/3 as test set
		
		printArrayData(trainArray);

//		System.out.println("trainArray's data : ");
//		printArrayData(trainArray); 
//		System.out.println("testArray's data : " );
//		printArrayData(testArray);

		generateInitialWeight();
		
		printArrayData(initialWeight);
		/*
		float[] a = { (float) -1.2, 1, 1 };
		float[] b = { (float) 0.3, 1, 1 };
		float[] c = { (float) 0.5, (float) 0.4, (float) 0.8 };
		initialWeight.add(a);
		initialWeight.add(b);
		initialWeight.add(c);	
		*/
				
		for (int i = 0; i < neuralAmount; i++) {
			float[] test = new float[trainArray.get(0).length];
			for (int j = 0; j < trainArray.get(0).length; j++) {

				test[j] = 0f;
			}
			lastWeight.add(test);
		}
		
		calOutputArea();

		calDesireArea();
		
		calOutputValue(trainArray, initialWeight);
				
		//check the weight correct
		checkWeight(testArray);
		
		caleavRMSE();
		System.out.println("Looptimes :　"+looptimes);
		long endTime = System.currentTimeMillis();
		System.out.println("used times : "+((endTime-startTime)/1000)+"s");
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