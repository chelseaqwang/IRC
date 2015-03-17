package client;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {

	private static final String HOST = "localhost";
	private static final int PORT = 6667;

	public static void main(String[] args) throws UnknownHostException, IOException {

		Socket socket = new Socket(HOST, PORT);
		PrintStream outstream = new PrintStream(socket.getOutputStream());
		Thread t = new InputFromServer(socket.getInputStream());
		t.setDaemon(true);
		t.start();

		Scanner keyIn = new Scanner(System.in);
		System.out.println("You are connecting to server ...");
		System.out.print("Please enter your username: ");
		String name = keyIn.nextLine();
		outstream.println( "USER" + " " + name + " " + HOST );

		while (!socket.isClosed()) {
			String line = keyIn.nextLine();

			outstream.println(line);
			outstream.flush();

		}

		keyIn.close();
		socket.close();
	}
}

