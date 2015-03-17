package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ChatServer {

	final static int PORT = 6667;
	public static HashMap<String, User> userMap = new HashMap<String, User>();
	public static HashMap<String, Channel> channelMap = new HashMap<String, Channel>();
	public static String server = "localhost";

	public static void main(String[] args) throws IOException {

		ServerSocket welcomeSocket = new ServerSocket(PORT);
		
		Thread t = new Administrator(System.in);
		t.setDaemon(true);
		t.start();

		while (true) {          
			Socket connectionSocket = welcomeSocket.accept(); 
			Service service = new Service(connectionSocket);
			Thread thread = new Thread(service);
			thread.start(); 
		}
	}
}
