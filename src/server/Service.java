package server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Service implements Runnable {
	private Socket socket;
	private static HashMap<String, User> userMap = ChatServer.userMap;
	private static HashMap<String, Channel> channelMap = ChatServer.channelMap;

	public Service(Socket socket) {
		this.socket = socket;
	} 

	public void run() {
		try {
			Scanner in = new Scanner(socket.getInputStream());
			User user = new User(socket);
			try {
				while (!socket.isClosed()) {
					if (!in.hasNext()) break;
					String command = in.nextLine();
					if (!command.startsWith("USER") && user.username() == null) {
						user.println("ERROR:NULL_USERNAME - Please set your username");
						continue;
					}
					System.out.println("From " + user.username() + ":" + command);
					
					if (command.startsWith("QUIT")) {
						user.println("Disconnect.");
						break;
					} else if (command.startsWith("JOIN")) {
						Command.valueOf("JOIN").execute(user, command);            
					} else if (command.startsWith("USER")) {
						Command.valueOf("USER").execute(user, command);
					} else if (command.startsWith("NICK")) {
						Command.valueOf("NICK").execute(user, command);
					} else if (command.startsWith("LIST")) {
						Command.valueOf("LIST").execute(user, command);
					} else if (command.startsWith("PART")) {
						Command.valueOf("PART").execute(user, command);
					} else if (command.startsWith("NAMES")) {
						Command.valueOf("NAMES").execute(user, command);
					} else if (command.startsWith("PRIVMSG")) {
						Command.valueOf("PRIVMSG").execute(user, command);
					} else {
						user.println("ERROR:NO_SUCH_COMMAND");
					}
				}
			} finally {
				Command.valueOf("QUIT").execute(user, "");
				in.close();
				socket.close();
				System.out.println("From " + user.username() + ":Client disconnected.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public enum Command {

		//connect
		//USER username
		USER() {
			@Override
			public void execute(User user, String command) {
				String[] args = command.split(" ");
				if (args[1] == null) {
					user.println("ERROR:NEED_MORE_PARAMS");
					return;
				}
				if (user.username()  != null) {
					user.println("ERROR:CANNOT_CHANGE_USERNAME");
					return;
				}
				if (userMap.keySet().contains(args[1])) {
					user.println("ERROR:USERNAME_OCCUPIED");
					return;
				}
				user.setUsername(args[1]);
				userMap.put(user.username(), user);
				user.println("You are connected.");
			}  
		},
		
		//NICK nickname
		NICK() {
			@Override
			public void execute(User user, String command) {
				String[] args = command.split(" ");
				if (args[1] == null) {
					user.println("ERROR:NEED_MORE_PARAMS");
					return;
				}
				user.setNickname(args[1]);
				user.println("nickname set to " + user.nickname());
			}
		},

		// create/join a room
		//JOIN channel 
		//JOIN channel,channel
		JOIN() {
			@Override
			public void execute(User user, String command) {
				String[] args = command.split(" ");
				if (args.length != 2) {
					user.println("ERROR:WRONG_NUMBER_OF_PARAMETERS");
					return;
				}
				String[] names = args[1].split(",");
				if (names.length > 5) {
					user.println("ERROR:TOO_MANY_CHANNELS");
					return;
				}
				for (String name : names) {
					if (!name.startsWith("#")) {
						user.println("ERROR:BAD_CHANNEL_MASK - channel name should start with #" + name);
						continue;
					}
					Channel channel = channelMap.get(name);
					
					// if channel does not exist, create a new channel
					if (channel == null) {
						channel = new Channel(name);
						user.println("New channle created " + name);
						channel.name = name;
						channel.add(user);
						channelMap.put(name, channel);
					}
					else if (channel.contains(user)) {
						user.println("You have joined channle " + name + " before.");
					}
					else {
						channel.add(user);
						user.println("You are in channel" + name + " now.");
					}
				}   
			}
		},

		//LIST
		LIST() {
			public void execute(User user, String command) {
				user.println(Arrays.toString(channelMap.keySet().toArray()));
				user.println("\\LIST END");
			}
		},

		//PART channel{,channel}
		PART {
			public void execute(User user, String command){
				String[] args = command.split(" ");
				if (args[1] == null) {
					user.println("ERROR:NEED_MORE_PARAMS");
					return;
				}
				String[] names = args[1].split(",");
				for (String name : names) {
					if (!name.startsWith("#")) {
						user.println("ERROR:BAD_CHANNEL_MASK - channel name should start with #" + name);
						continue;
					}
					Channel channel = channelMap.get(name);
					if (channel == null) {
						user.println("ERROR:NO_SUCH_CHANNEL" + name);
					}
					else if (!channel.contains(user)) {
						user.println("ERROR:NOT_ON_CHANNEL" + name);
					}
					else {
						channel.remove(user);
						user.println("You have left channel " + name);
						if (channel.members().size() == 0) {
							channelMap.remove(name);
						}
					}
				}   
			}  
		},

		//NAMES channel
		//NAMES - return all user names on the server
		NAMES() {
			public void execute(User user, String command) {
				String[] args = command.split(" ");
				if (args.length == 1) {
					for (String username : userMap.keySet()) {
						User u = userMap.get(username);
						user.println(u.username() + "\t" + u.nickname());
					}
				} else if (!args[1].startsWith("#")) {
					user.println("ERROR:BAD_CHANNEL_MASK:channel name should start with # " + args[1]);
					return;
				} else {			
					Channel channel = channelMap.get(args[1]);
					if (channel == null) {
						user.println("ERROR:NO_SUCH_CHANNEL");
					}
					else {
						for(User u : channel.members()) {
							user.println(u.username() + "\t" + u.nickname());
						}
					}
				}
			}
		},
		//PRIVMSG user :message
		//PRIVMSG #channel :message
		//PRIVMSG $server :message
		PRIVMSG() {
			public void execute(User user, String command) {
				String[] args = command.split(" ");
				if (args[2] == null) {
					user.println("ERROR:NEED_MORE_PARAMS");
					return;
				}
				if (!args[2].startsWith(":")) {
					user.println("message should start with :");
					return;
				}
				if (args[1].startsWith("#")) { // send a message to a channel or channels
					String[] channelNames = args[1].split(",");
					for (String channelName : channelNames) {
						Channel channel = channelMap.get(channelName);
						if (!channel.contains(user)) {
							user.println("ERROR:NOT_ON_CHANNEL");
							return;
						}
						channel.channelMSG(user, "PRIVMSG" + " " + channelName + " :" + command.split(":")[1]);
						user.println("Message sent to channel " + channelName);
					}
				} else if (args[1].startsWith("$localhost")) { // send a message to a server					
					for (String userName : userMap.keySet()) {
						User u = userMap.get(userName);	
						if(u.username() != user.username()) {
							u.println(user.username() + ":" + command);
							user.println("Message sent to server");
						}
					}
				} else { //send private message to a person
					if (userMap.keySet().contains(args[1])) {
						User u = userMap.get(args[1]);
						u.println(user.username() + "***private***" + command);
						user.println("Message sent to " + u.username());
					} else {
						user.println("ERROR:NO_SUCH_USER");
					}
				}
			}

		},

		QUIT() {
			public void execute(User user, String command) {
				for (String channelName : new ArrayList<String>(channelMap.keySet())) {
					Channel channel = channelMap.get(channelName);
					channel.remove(user);
					if (channel.members().size() == 0) {
						channelMap.remove(channelName);
					}
				}
				userMap.remove(user.username());
			}
		};

		public abstract void execute(User user, String command); 

	}
}
