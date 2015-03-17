package client;
import java.io.*;

public class InputFromServer extends Thread {
	private BufferedReader in;

	protected InputFromServer(InputStream in) {
		this.in = new BufferedReader(new InputStreamReader(in));
	}

	public void run() {
		try {
			try {
				String msg;
				while ((msg = in.readLine()) != null) {
					System.out.println(msg);
				}
			} finally {
				in.close();
				System.out.println("Disconnected.");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

