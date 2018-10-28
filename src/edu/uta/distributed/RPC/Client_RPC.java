package edu.uta.distributed.RPC;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * 
 * @author akashlohani
 *
 */
public class Client_RPC {

	/**
	 * @param args
	 */

	private static final Logger LOGGER = Logger.getLogger(Client_RPC.class.getName());
	private Socket clientSocket = null;
	private static DataInputStream c_input = null;
	private static DataOutputStream c_output = null;
	private static ObjectInputStream cObj_input = null;
	private static ObjectOutputStream cObj_output = null;
	private static Integer[][] matrix_size = new Integer[3][2];

	public final static int PORT = 6666;
	public final static String IPADD = "localhost";

	public void connect() {
		try {
			clientSocket = new Socket(IPADD, PORT);
			System.out.println("Connected to Server");
			OutputStream out = clientSocket.getOutputStream();
			InputStream input = clientSocket.getInputStream();
			// definition for exchanging Stream information
			c_output = new DataOutputStream(out);
			c_input = new DataInputStream(input);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			System.out.println("Disconnected from Server");
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String loadMenuOption() {
		Scanner s = new Scanner(System.in);
		System.out.println(
				"Choose RPC Option.\n 1.Calculate PI\n 2.Addition\n 3.Sort Array\n 4.Multiply Matrix\n 5.End ");
		return s.nextLine();
	}

	public static double calculate_pi() {
		double res = 0;
		try {
			c_output.writeUTF("calculate_pi");
			res = c_input.readDouble();
			c_output.flush();
			c_output.close();
			return res;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return (Double) null;
		}

	}

	public static double add(double i, double j) {
		double sum = 0;
		try {
			c_output.writeUTF("add");
			c_output.writeDouble(i);
			c_output.writeDouble(j);
			sum = c_input.readDouble();

			c_output.flush();
			c_output.close();
			return sum;
		} catch (IOException i1) {
			i1.printStackTrace();
			return 0;
		}
	}

	public static Double[] sort(Double[] arrayA) {
		try {
			c_output.writeUTF("sort");
			int size = arrayA.length;
			c_output.writeInt(size);
			cObj_output = new ObjectOutputStream(c_output);

			cObj_output.writeObject(arrayA);

			cObj_input = new ObjectInputStream(c_input);
			arrayA = (Double[]) cObj_input.readObject();

			cObj_input.close();
			cObj_output.flush();
			cObj_output.close();
			return arrayA;
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client_RPC client = new Client_RPC();
		String userChoice;
		Scanner s = new Scanner(System.in);
		while (true) {
			userChoice = client.loadMenuOption();

			switch (userChoice) {
			case "1":
				client.connect();
				System.out.println("The value of pi is " + calculate_pi());
				client.closeConnection();

				break;
			case "2":

				System.out.println("Enter first number to add");
				double num1 = s.nextDouble();
				System.out.println("Enter second number to add");
				double num2 = s.nextDouble();

				client.connect();

				System.out.println("Sum of  " + num1 + " and " + num2 + " is : " + add(num1, num2));

				client.closeConnection();
				break;
			case "3":

				System.out.println("Enter the size of Array");
				int size = s.nextInt();
				Double[] array = new Double[size];
				for (int i = 0; i < size; i++) {
					System.out.println("Enter the value of your Array [Press enter after each value]");
					array[i] = s.nextDouble();
					//LOGGER.info("Input Value " + array[i]);
				}
				client.connect();
				array = sort(array);
				System.out.println("The sorted Array via RPC is : ");
				for (int i = 0; i < size; i++)
					System.out.print(array[i] + " ");
				break;
			case "4":

				while (true) {
					System.out.println("How many columns are there in your MatrixA?");
					matrix_size[0][0] = s.nextInt();

					System.out.println("How many rows are there in your MatrixA?");

					matrix_size[0][1] = s.nextInt();

					System.out.println("How many columns are there in your MatrixB?");
					matrix_size[1][0] = s.nextInt();

					System.out.println(" How many rows are there in your MatrixB?");
					matrix_size[1][1] = s.nextInt();

					System.out.println("How many columns are there in your MatrixC?");
					matrix_size[2][0] = s.nextInt();

					System.out.println(" How many rows are there in MatrixC?");
					matrix_size[2][1] = s.nextInt();
					if (matrix_size[0][1] != matrix_size[1][0] || matrix_size[1][1] != matrix_size[2][0])
						System.out.println("Invalid Size of Matrix for multiplication | Please choose valid Size");
					else
						break;
				}
				Double[][] mat1 = new Double[matrix_size[0][0]][matrix_size[0][1]];
				Double[][] mat2 = new Double[matrix_size[1][0]][matrix_size[1][1]];
				Double[][] mat3 = new Double[matrix_size[2][0]][matrix_size[2][1]];

				// DATA INPUT MATRIX1
				for (int i = 0; i < matrix_size[0][0]; i++) {
					for (int j = 0; j < matrix_size[0][1]; j++) {
						System.out.println("Enter the value of Matrix1([" + String.valueOf(i) + "]["
								+ String.valueOf(j) + "])");
						mat1[i][j] = s.nextDouble();
					}
				}

				// DATA INPUT MATRIX2
				for (int i = 0; i < matrix_size[1][0]; i++) {
					for (int j = 0; j < matrix_size[1][1]; j++) {
						System.out.println("Enter the value of Matrix2([" + String.valueOf(i) + "]["
								+ String.valueOf(j) + "])");

						mat2[i][j] = s.nextDouble();
					}
				}

				// DATA INPUT MATRIX3
				for (int i = 0; i < matrix_size[2][0]; i++) {
					for (int j = 0; j < matrix_size[2][1]; j++) {
						System.out.println("Enter the value of Matrix3([" + String.valueOf(i) + "]["
								+ String.valueOf(j) + "])");
						mat3[i][j] = s.nextDouble();
					}
				}
				client.connect();

				String PRODUCT_s = "";
				Double[][] PRODUCT = matrix_multiply(mat1, mat2, mat3);
				
				for (int i = 0; i < matrix_size[0][0]; i++) {
					for (int j = 0; j < matrix_size[2][1]; j++) {
						PRODUCT_s = PRODUCT_s + String.valueOf(PRODUCT[i][j]) + " ";
					}
					PRODUCT_s = PRODUCT_s + "\n";
				}
				System.out.println("MatA * MatB * MatC =");
				System.out.println(PRODUCT_s);
				break;
			case "5":
				System.exit(-1);
				break;
			// default:
			// client.closeConnection();
			}

		}

	}

	public static Double[][] matrix_multiply(Double[][] mat1, Double[][] mat2, Double[][] mat3) {
		// TODO Auto-generated method stub
		try {
			c_output.writeUTF("matrix_multiply");
			cObj_output = new ObjectOutputStream(c_output);
			System.out.println(matrix_size[0][0]);
			cObj_output.writeObject(matrix_size);

			cObj_output.writeObject(mat1);
			cObj_output.writeObject(mat2);
			cObj_output.writeObject(mat3);

			cObj_input = new ObjectInputStream(c_input);

			Double[][] PRODUCT = new Double[(int) matrix_size[0][0]][(int) matrix_size[2][1]];
			PRODUCT = (Double[][]) cObj_input.readObject();

			cObj_input.close();
			cObj_output.flush();
			cObj_output.close();
			return PRODUCT;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
