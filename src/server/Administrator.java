package server;

import java.io.*;
import java.util.HashMap;

public class Administrator extends Thread {
	private BufferedReader in;
	private static HashMap<String, User> userMap = ChatServer.userMap;
	private static HashMap<String, Channel> channelMap = ChatServer.channelMap;
	
	protected Administrator(InputStream in) {
		this.in = new BufferedReader(new InputStreamReader(in));
	}

	public void run() {
		try {
			try {
				String command;
				while ((command = in.readLine()) != null) {
					if (command.startsWith("KICK")) {
						kick(command);
					} else {
						System.out.println("ERROR:NO_SUCH_COMMAND");
					}
				}
			} finally {
				in.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
	}
	

	private void kick(String command) {
		String[] args = command.split(" ");
		if (args.length != 2) {
			System.out.println("ERROR:NEED_MORE_PARAMETERS");
			return;
		}
		String[] usernames = args[1].split(",");
		for (String username : usernames) {
			if (!userMap.containsKey(username)) {
				System.out.println("ERROR:NO_SUCH_USER - " + username);
				continue;
			}
			User user = userMap.get(username);
			server.Service.Command.valueOf("QUIT").execute(user, null);
			try {
				user.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

