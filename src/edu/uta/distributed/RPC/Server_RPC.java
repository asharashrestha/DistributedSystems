package edu.uta.distributed.RPC;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;
import java.lang.Math;

public class Server_RPC {
	private static final Logger LOGGER = Logger.getLogger(Server_RPC.class.getName());
	private Socket clientsocket = null;// to communicate with client
	private ServerSocket server = null; // it listens to port for request

	private DataInputStream s_input = null;
	private DataOutputStream s_output = null;

	private ObjectInputStream sObj_input = null;
	private ObjectOutputStream sObj_output = null;

	public final static int PORT = 6666;

	public Server_RPC(int port) {
		try {
			server = new ServerSocket(port);
		} catch (Exception e) {
			System.out.println("Server ShutDown Unexpectedly");
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			System.out.println("Server Started");
			while (true) {
				System.out.println("Server Ready for Connection.. Waiting for Client");
				clientsocket = server.accept();

				s_input = new DataInputStream(clientsocket.getInputStream());
				s_output = new DataOutputStream(clientsocket.getOutputStream());
				processClientrequest();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processClientrequest() {

		String req = null;
		try {
			req = s_input.readUTF();
			LOGGER.info("Function " + req);
			if (req.compareTo("calculate_pi") == 0) {
				System.out.println("Calculate PI");
				pi();
			} else if (req.compareTo("add") == 0) {
				add(s_input.readDouble(), s_input.readDouble());
			} else if (req.compareTo("sort") == 0) {
				int length = s_input.readInt();
				sObj_input = new ObjectInputStream(s_input);
				//LOGGER.info("Length : " + length);
				Double[] array = new Double[length];
				array = (Double[]) sObj_input.readObject();
				for (int i = 0; i < length; i++)
					System.out.println(array[i]);
				sort(length, array);
				// sObj_output.writeObject(abc);
			} else if (req.compareTo("matrix_multiply") == 0) {
				Integer[][] matrix_size = new Integer[3][2];
				sObj_input = new ObjectInputStream(s_input);
				matrix_size = (Integer[][]) sObj_input.readObject();
				Double[][] mat1 = new Double[matrix_size[0][0]][matrix_size[0][1]];
				Double[][] mat2 = new Double[matrix_size[1][0]][matrix_size[1][1]];
				Double[][] mat3 = new Double[matrix_size[2][0]][matrix_size[2][1]];

				mat1 = (Double[][]) sObj_input.readObject();
				mat2 = (Double[][]) sObj_input.readObject();
				mat3 = (Double[][]) sObj_input.readObject();
				//LOGGER.info("Sanity Check : " + mat1[0][0] + " " + mat2[0][0] + " " + mat3[0][0] + " ");
				matrix_multiply(matrix_size, mat1, mat2, mat3);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception i) {
			i.printStackTrace();
		}

	}

	public void close() {
		// TODO Auto-generated method stub
		System.out.println("Closing Connnection");

		try {
			s_output.writeUTF("END");
			clientsocket.close();
			s_input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void pi() {
		int i;
		double PI;
		double polygon_squared = 2.0;
		double polygon_numof_side = 4.0;
		int n;
		try {
			n = 15;
			for (i = 0; i < n; i++) {
				polygon_squared = 2 - 2 * Math.sqrt(1 - polygon_squared / 4);
				polygon_numof_side = 2 * polygon_numof_side;
			}
			PI = polygon_numof_side * Math.sqrt(polygon_squared) / 2;

			s_output.writeDouble(PI);

			s_output.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void add(double i, double j) {
		double sum;

		try {

			sum = i + j;
			s_output.writeDouble(sum);
			s_output.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sort(int arr_length, Double[] arr) {
		double pivot;
		int i, j;
		try {

			for (i = 1; i < arr_length; i++) {
				pivot = arr[i];
				j = i - 1;
				while (j >= 0 && arr[j] > pivot) {
					arr[j + 1] = arr[j];
					j = j - 1;
				}
				arr[j + 1] = pivot;
			}
			sObj_output = new ObjectOutputStream(s_output);
			sObj_output.writeObject(arr);
			sObj_output.flush();
			s_output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void matrix_multiply(Integer[][] matrix_size, Double[][] mat1, Double[][] mat2, Double[][] mat3) {
		int i, j, k;
		try {
			Double[][] PRODUCT_kari = new Double[matrix_size[0][0]][matrix_size[1][1]];
			Double[][] PRODUCT = new Double[matrix_size[0][0]][matrix_size[2][1]];
			// INitializing
			for (i = 0; i < matrix_size[0][0]; i++) {
				for (j = 0; j < matrix_size[1][1]; j++) {
					PRODUCT_kari[i][j] = 0.0;
				}
			}
			for (i = 0; i < matrix_size[0][0]; i++) {
				for (j = 0; j < matrix_size[2][1]; j++) {
					PRODUCT[i][j] = 0.0;
				}
			}
			// CALCULATING PRUDUCT
			for (i = 0; i < matrix_size[0][0]; i++) {
				for (j = 0; j < matrix_size[1][1]; j++) {
					for (k = 0; k < matrix_size[0][1]; k++) {
						PRODUCT_kari[i][j] += mat1[i][k] * mat2[i][k];
					}
				}
			}

			for (i = 0; i < matrix_size[0][0]; i++) {
				for (j = 0; j < matrix_size[2][1]; j++) {
					for (k = 0; k < matrix_size[1][1]; k++) {
						PRODUCT[i][j] += PRODUCT_kari[i][k] * mat3[i][k];
					}
				}
			}
			String PRODUCT_s = "";
			for (i = 0; i < matrix_size[0][0]; i++) {
				for (j = 0; j < matrix_size[2][1]; j++) {
					PRODUCT_s = PRODUCT_s + String.valueOf(PRODUCT[i][j]) + " ";
				}
				PRODUCT_s = PRODUCT_s + "\n";
			}
			System.out.println(PRODUCT_s);
			sObj_output = new ObjectOutputStream(s_output);
			sObj_output.writeObject(PRODUCT);
			sObj_output.flush();
			s_output.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server_RPC server = new Server_RPC(PORT);
		server.start();
	}

}
