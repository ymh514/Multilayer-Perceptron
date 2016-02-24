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
	static int neuralAmount = 0;
	static int x0 = -1;
	static int looptimes = 0;
	static int correctCount = 0;
	static int sortedNewDesire = 0;

	static int looptimeLimit;
	static float errorLimit;
	static float studyRate;
	static float alpha;

	static int normailzeFlag = 0;

	static ArrayList<float[]> inputArray = new ArrayList<float[]>();
	static ArrayList<float[]> sortedArray = new ArrayList<float[]>();
	static ArrayList<float[]> tempArray = new ArrayList<float[]>();
	static ArrayList<float[]> trainArray = new ArrayList<float[]>();
	static ArrayList<float[]> testArray = new ArrayList<float[]>();
	static ArrayList<float[]> weight = new ArrayList<float[]>();
	static ArrayList<float[]> lastWeight = new ArrayList<float[]>();
	static ArrayList<Float> rmse = new ArrayList<Float>();

	static float[] yOutput;
	static float[] yOutputArea;
	static float[] desireArea;
	static float[] gradient;
	static float[] errorFunction = new float[1];

	public static void inputFileChoose(String[] args) throws IOException {

		String FileName = loadFile();

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

	public static String loadFile() {
		/*
		 * referance java tutorial how to read file then return the path
		 */
		JFileChooser fc = new JFileChooser();
		File NamePath = null;
		int Checker;

		fc.setCurrentDirectory(new java.io.File("D:\\NCU 1041\\NN\\Dataset2"));
		fc.setDialogTitle("Choose a file input");
		Checker = fc.showOpenDialog(null);

		if (Checker == JFileChooser.APPROVE_OPTION) {
			NamePath = fc.getSelectedFile();
			System.out.println("The name of path : " + NamePath.getAbsolutePath());
		} else {
			JOptionPane.showMessageDialog(null, "You have clicked Canceled");
		}
		return NamePath.getAbsolutePath();
	}

	private static void setParameter() {

		JTextField usrlooptimes = new JTextField("10000000");
		JTextField usrstudyrate = new JTextField("0.1");
		JTextField usrmomentum = new JTextField("0.5");
		JTextField usrerrorlimit = new JTextField("0.0001");
		JCheckBox normalizeCheck = new JCheckBox("normalize", true);

		Object[] message = { "Looptimes", usrlooptimes, "Studyrate", usrstudyrate, "Momentum (0~1)", usrmomentum,
				"Errorlimit", usrerrorlimit, normalizeCheck };

		JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		pane.createDialog(null, "Initial Parameter").setVisible(true);

		// read all initail parameter to static parameter

		if (normalizeCheck.isSelected()) {
			normailzeFlag = 1;
		}

		looptimeLimit = Integer.parseInt(usrlooptimes.getText());
		System.out.println("Your loop times is " + looptimeLimit);

		studyRate = Float.parseFloat(usrstudyrate.getText());
		System.out.println("Your studyrate is " + studyRate);

		alpha = Float.parseFloat(usrmomentum.getText());
		System.out.println("Your Momentum is " + alpha);

		errorLimit = Float.parseFloat(usrerrorlimit.getText());
		System.out.println("Your errorlimit is " + errorLimit);

	}

	private static void normalizeData() {
		/*
		 * idea: find the biggest number(no matter positive or negative ,set it
		 * as denominator
		 */

		float[] tempArray = new float[inputArray.get(0).length];
		for (int i = 0; i < inputArray.size(); i++) {
			for (int j = 0; j < inputArray.get(i).length - 1; j++) {
				if (tempArray[j] < inputArray.get(i)[j]) {
					tempArray[j] = inputArray.get(i)[j];
				}
			}
		}
		for (int i = 0; i < inputArray.size(); i++) {
			for (int j = 0; j < inputArray.get(i).length - 1; j++) {
				inputArray.get(i)[j] /= tempArray[j];
			}

		}

		/*
		 * belw is old ver. to normalize and it's wrong direction
		 * fix this situation on top
		 * 
		 */

		// for (int i = 0; i < inputArray.size(); i++) {
		// float max = Float.MIN_VALUE;
		// for (int j = 0; j < inputArray.get(i).length - 1; j++) {
		// if (Math.abs(inputArray.get(i)[j]) > max) {
		// max = Math.abs(inputArray.get(i)[j]);
		// }
		// }
		// for (int k = 0; k < inputArray.get(i).length - 1; k++) {
		// inputArray.get(i)[k] /= max;
		// }
		// }
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

		int iRestFlag = 0;
		System.out.println("--------- Start sort ---------");

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
		 * not only can generate positive value , also can get negative value
		 * but ranged in -1~1
		 */
		System.out.println("--------------------------------------------------");
		Random rand = new Random();
		for (int i = 0; i < neuralAmount; i++) {
			float[] token = new float[trainArray.get(0).length];
			for (int j = 0; j < trainArray.get(0).length; j++) {
				if (Math.random() > 0.5) {
					token[j] = rand.nextFloat() + 0f;
					// System.out.println("weight : " + token[j]);
				} else {
					token[j] = rand.nextFloat() - 1f;
					// System.out.println("weight : " + token[j]);
				}
			}
			weight.add(token);
		}
		System.out.println("--------------------------------------------------");
	}

	private static void lastWeightInitial() {
		/*
		 * for accelerate process,we add a momentum, and initial we set all 0
		 */
		for (int i = 0; i < neuralAmount; i++) {
			float[] test = new float[trainArray.get(0).length];
			for (int j = 0; j < trainArray.get(0).length; j++) {
				test[j] = 0f;
			}
			lastWeight.add(test);
		}
	}

	public static void calOutputArea() {
		/*
		 * get output bound that from 0 to 1 purpose : check youtput at right
		 * range
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
		 * get output bound that from 0 to 1 purpose : generate right desire
		 * except to do gradient and tune weight
		 */
		int classAmount = sortedNewDesire + 1;
		desireArea = new float[classAmount];

		for (int i = 0; i < classAmount; i++) {
			if (i == 0) {
				desireArea[i] = 0f;
			} else {
				// get two decimal places
				desireArea[i] = (float) (Math.round((float) i / (classAmount - 1) * 100)) / 100;
			}
		}
		for (int i = 0; i < desireArea.length; i++) {
			System.out.println("desireAreaBound" + i + " : " + desireArea[i]);
		}
	}

	public static void calOutputValue(ArrayList<float[]> array, ArrayList<float[]> weight) {
		/*
		 * 1. use for to run neuralAmount times to get y 2. when its last loop
		 * get last output 3. use yOutput which generated by upper loop and do*
		 * cal with weight to get z the last output notice : for(j) loop's
		 * yOutput[j-1] cause must fetch value from the first value 4. the
		 * latest value of yOutput is outputz 5. no matter classify right or
		 * not,we will do four things every iteration: (1) check the y is in
		 * right range or not, and will count the correct times (correctcount)
		 * for cal correct ratio (2) find gradient (3) tune weight (4) cal E(n)
		 * error and store to rmse 6. after a loop check the correct ratio
		 * first, if not 100% go on check RMSE if correct ratio 100% or RMSE <
		 * the errorlimit which we set, will break the loop if keep run a new
		 * loop rmse and correctcount will be clear empty 7. if looptimes >
		 * limit looptimes will break loop
		 * 
		 */

		int noOfData = 0;
		loop: while (true) {
			int desire = (int) array.get(noOfData)[array.get(noOfData).length - 1];

			for (int i = 0; i < neuralAmount; i++) {
				if (i != neuralAmount - 1) {
					float sum = 0f;
					sum = x0 * weight.get(i)[0];
					for (int j = 0; j < array.get(noOfData).length - 1; j++) {
						sum += array.get(noOfData)[j] * weight.get(i)[j + 1];
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sum)));
					// System.out.println("y" + i + " output is : " +
					// yOutput[i]);

				} else {
					float sumZ = 0f;
					sumZ = x0 * weight.get(i)[0];
					for (int j = 0; j < yOutput.length - 1; j++) {
						sumZ += yOutput[j] * weight.get(i)[j + 1];// match right
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sumZ)));
					// System.out.println("y" + i + "(z) output is : " +
					// yOutput[i]);

					// check classify area correct or not use a range bound
					// (yOutputArea)
					if (yOutput[i] > yOutputArea[desire] && yOutput[i] <= yOutputArea[desire + 1]) {
						correctCount++;
					}

					errorFunction[0] = 0;
					errorFunction[0] = (desireArea[desire] - yOutput[i]);
				}
			}

			calEn();
			calculateGradient(desireArea[desire]);
			tuneWeight(noOfData, array);

			if (noOfData == array.size() - 1) {
				noOfData = 0;
				looptimes++;

				float ratio = (float) correctCount / array.size();
				if (ratio == 1) {
					System.out.println("Train correct ratio 100%");
					break loop;
				}
				System.out.println("Train correct ratio is : " + ratio * 100 + "%");

				if (calRMSE() < errorLimit) {
					System.out.println("find eavrmse < " + errorLimit);
					break loop;
				}

				rmse.removeAll(rmse);
				correctCount = 0;
			} else {
				noOfData++;
			}

			if (looptimes > looptimeLimit) {
				System.out.println("out of looptimes");
				break loop;
			}
		}

	}

	private static float calEn() {
		/*
		 * idea: every iteration will store it's E(n) to rmse(arraylist)
		 */
		float errorSum = 0;

		errorSum = errorFunction[0] * errorFunction[0];
		errorSum /= 2;
		rmse.add(errorSum);

		return errorSum;
	}

	private static float calRMSE() {
		/*
		 * after a loop we will load rmse(arraylist)'s value to cal Eav RMSE
		 */
		float sumrmse = 0f;
		for (int i = 0; i < rmse.size(); i++) {
			sumrmse += rmse.get(i);
		}
		sumrmse = sumrmse / rmse.size();
		System.out.println("RMSE : " + sumrmse);
		return sumrmse;
	}

	private static void calculateGradient(float desire) {
		/*
		 * 1. declare count is neuralAmount-1 for array use 2. while loop
		 * continue -- 3. output layer's gradient calculation is different from
		 * hidden layer
		 */
		int countdown = neuralAmount - 1;
		while (countdown != -1) {
			if (countdown == neuralAmount - 1) {
				gradient[countdown] = (desire - yOutput[countdown]) * yOutput[countdown] * (1 - yOutput[countdown]);
			} else {
				gradient[countdown] = yOutput[countdown] * (1 - yOutput[countdown]) * gradient[neuralAmount - 1]
						* weight.get(neuralAmount - 1)[countdown + 1];
			}
			countdown--;
		}
		/*
		 * System.out.println("gradient : "); for (int i = 0; i <
		 * gradient.length; i++) { System.out.println(gradient[i]); }
		 */
	}

	private static void tuneWeight(int noOfData, ArrayList<float[]> array) {
		/*
		 * 1. cause weight is a dim 2 matrix so we use 2 for loop to pack it 2.
		 * but we use a if to separate with hidden and output layer 3. in the
		 * calculation--notice: gradient[i] is i not j
		 */
		float tuneWeight = 0f;
		for (int i = 0; i < weight.size(); i++) {
			if (i != weight.size() - 1) {
				for (int j = 0; j < weight.get(i).length; j++) {
					if (j == 0) {
						tuneWeight = alpha * lastWeight.get(i)[j] + studyRate * gradient[i] * x0;
						weight.get(i)[j] += tuneWeight;
						lastWeight.get(i)[j] = tuneWeight;
					} else {
						tuneWeight = alpha * lastWeight.get(i)[j]
								+ studyRate * gradient[i] * array.get(noOfData)[j - 1];
						weight.get(i)[j] += tuneWeight;
						lastWeight.get(i)[j] = tuneWeight;
					}
				}
			} else {
				for (int j = 0; j < weight.get(i).length; j++) {
					if (j == 0) {
						tuneWeight = alpha * lastWeight.get(i)[j] + (studyRate * gradient[i] * x0);
						weight.get(i)[j] += tuneWeight;
						lastWeight.get(i)[j] = tuneWeight;

					} else {
						tuneWeight = alpha * lastWeight.get(i)[j] + (studyRate * gradient[i] * yOutput[j - 1]);
						weight.get(i)[j] += tuneWeight;
						lastWeight.get(i)[j] = tuneWeight;

					}
				}
			}

		}
		/*
		 * System.out.println("tune weight : "); printArrayData(weight);
		 */
	}

	private static void checkWeight(ArrayList<float[]> array) {

		// System.out.println("Show final weight ");
		// printArrayData(weight);
		// System.out.println("-------------------- now start to check
		// --------------------");

		int noOfData = 0;
		int correctCount = 0;

		loop: while (true) {
			int desire = (int) array.get(noOfData)[array.get(noOfData).length - 1];
			// System.out.println("this is dataamount : " + noOfData);

			for (int i = 0; i < neuralAmount; i++) {
				if (i != neuralAmount - 1) {
					float sum = 0f;
					sum = x0 * weight.get(i)[0];
					for (int j = 0; j < array.get(noOfData).length - 1; j++) {
						sum += array.get(noOfData)[j] * weight.get(i)[j + 1];
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sum)));
				} else {
					float sumZ = 0f;
					sumZ = x0 * weight.get(i)[0];
					for (int j = 0; j < yOutput.length - 1; j++) {
						sumZ += yOutput[j] * weight.get(i)[j + 1];// match
																	// right
					}
					yOutput[i] = (float) (1 / (1 + Math.exp(-sumZ)));

					// check classify area correct or not use a range bound
					// (yOutputArea)
					if (yOutput[i] > yOutputArea[desire] && yOutput[i] <= yOutputArea[desire + 1]) {
						// System.out.println("Correct classify");
						correctCount++;
					} else {
						// System.out.println("### Error clssify");
					}
				}
			}

			// System.out.println("---------------------------------------------------------");
			if (noOfData == array.size() - 1) {
				break loop;
			} else {
				noOfData++;
			}
		}
		float ratio = (float) correctCount / array.size();
		System.out.println("Test correct ratio is :　" + (ratio * 100) + "%");
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
		 * 1. choose input file 2. set initial parameter 3. check the normalize
		 * checkbox 4. sort the desire , begin from 0 to classamount-1 5. random
		 * input data 6. separate data as 2/3 for train 1/3 for test 7. set the
		 * neural amount ,and declare youtput and gradient which are need neural
		 * amount declare 8. generate random initial weight 9. calculate the
		 * output's area to check y in the right area or not 10. calculate the
		 * desire's area for tune weight 11. start to calculate with train data
		 * 12. do cal again to check test data's correct ratio 13. GUI interface
		 * 
		 */

		long startTime = System.currentTimeMillis();

		inputFileChoose(args);

		setParameter();

		if (normailzeFlag == 1) {
			normalizeData();
		}

		sortInputArray(inputArray);

		putInputToTemp(sortedArray);// copy to temp with random

		separateTemp(tempArray);// separate to train and test set,set 2/3 as
								// train set 1/3 as test set

		neuralAmount = trainArray.get(0).length;
		yOutput = new float[neuralAmount];
		gradient = new float[neuralAmount];

		generateInitialWeight();

		lastWeightInitial();

		calOutputArea();

		calDesireArea();

		calOutputValue(trainArray, weight);

		checkWeight(testArray);

		System.out.println("looptimes :　" + looptimes);

		long endTime = System.currentTimeMillis();

		System.out.println("used times : " + ((endTime - startTime) / 1000) + "s");

		genarateFrame(trainArray, sortedNewDesire + 1);
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