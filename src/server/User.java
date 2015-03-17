package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class User {
	private String username;
	private String nickname;
	private Socket socket;
	private PrintStream out;
	private static String server = ChatServer.server;
	
	public User(Socket socket) {
		this.socket = socket;
		connect();
	}
	
	private void connect() {
		try {
			out = new PrintStream(socket.getOutputStream());
		}
		catch (IOException e) {
			System.err.println(e);
		}
	}
	
	public void setUsername(String name) {
		username = name;
	}
	
	public void setNickname(String name) {
		nickname = name;
	}
	
	public String username() {
		return username;
	}
	
	public String nickname() {
		return nickname;
	}
	
	public void close() throws IOException {
		socket.close();
		return;
	}
	
	public void println(String message) {
		out.println("server $" + server + ":" + message);
		out.flush();
	}
}
