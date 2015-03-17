package server;

import java.util.LinkedList;

public class Channel {
	private LinkedList<User> members;
	protected String name;
	
	public Channel(String name) {
		this.name = name;
		members = new LinkedList<User>();
	}
	
	//send a message to all members in the channel
	public void systemMSG(String message) {
		for (User user: members) {
			user.println(message);
		}
	}
	
	//send a message to all other members in the channel
	public void channelMSG(User u, String message) {
		for (User user: members) {
			if (user.equals(u)) {
				continue;
			}
			user.println(u.username() + ":" + message);
		}
	}
	
	public boolean contains(User user) {
		return members.contains(user);
	}
	
	public void add(User user) {
		members.add(user);
		systemMSG(user.username() + " has joined " + name);
	}
	
	public void remove(User user) {
		if (contains(user)) {
			members.remove(user);
			systemMSG(user.username() + " has left " + name);
		}
	}	
	
	public LinkedList<User> members() {
		return members;
	}

}
