import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.net.ServerSocket;

public class Server {

	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	public static ArrayList<clientThread> clients = new ArrayList<clientThread>();
	public static void main(String args[]) {
	int portNumber = 1233;
	if (args.length < 1) 
		{
		System.out.println("No port specified by user.\nServer is running using default port number=" + portNumber);
		} 
		else 
		{
			portNumber = Integer.valueOf(args[0]).intValue();
                 	System.out.println("Server is running using the specified port number=" + portNumber);
		}

		try {
			serverSocket = new ServerSocket(portNumber);
		} 
                catch (Exception e) {
			System.out.println("Server Socket cannot be created");
		}

		int clientNum = 1;
		while (true) {
			try {
		clientSocket = serverSocket.accept();
				clientThread curr_client =  new clientThread(clientSocket, clients);
				clients.add(curr_client);
				curr_client.start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
			} 
                        catch (Exception e) {
                          System.out.println("Client can not be connected");
			}
		}
}
}

class clientThread extends Thread {

	private String clientName = null;
	private ObjectInputStream is = null;
	private ObjectOutputStream os = null;
	private Socket clientSocket = null;
	private final ArrayList<clientThread> clients;
	public clientThread(Socket clientSocket, ArrayList<clientThread> clients) {

		this.clientSocket = clientSocket;
		this.clients = clients;

	}
	public void run() {
		ArrayList<clientThread> clients = this.clients;

		try {
			is = new ObjectInputStream(clientSocket.getInputStream());
			os = new ObjectOutputStream(clientSocket.getOutputStream());

			String name;
			while (true) {

				synchronized(this)
				{
					this.os.writeObject("Please enter your name :");
					this.os.flush();
					name = ((String) this.is.readObject()).trim();
					if ((name.indexOf('@') == -1) || (name.indexOf('!') == -1)) {
						break;
					} else {
						this.os.writeObject("Username should not contain '@' or '!' characters.");
						this.os.flush();
					}
				}
			}
				System.out.println("Client Name is " + name); 
				this.os.writeObject(" Welcome " + name + " to our chat room ***\nEnter /quit to leave the chat room");
				this.os.flush();

				this.os.writeObject("Directory Created");
				this.os.flush();
				synchronized(this)
				{
				for (clientThread curr_client : clients)  
				{
					if (curr_client != null && curr_client == this) {
						clientName = "@" + name;
						break;
					}
				}
				for (clientThread curr_client : clients) {
					if (curr_client != null && curr_client != this) {
						curr_client.os.writeObject(name + " has joined");
						curr_client.os.flush();

					}

				}
			}
			while (true) {
				this.os.writeObject("Please Enter message:");
				this.os.flush();
				String line = (String) is.readObject();
				if (line.startsWith("/quit")) {
					break;
				}
				if (line.startsWith("@")) {
					unicast(line,name);        	
				}
				else if(line.startsWith("!"))
				{
					blockcast(line,name);
				}
				else 
				{
					broadcast(line,name);
				}
			}
			this.os.writeObject("*** Bye " + name + " ***");
			this.os.flush();
			System.out.println(name + " disconnected.");
			clients.remove(this);
			synchronized(this) {
				if (!clients.isEmpty()) {
					for (clientThread curr_client : clients) {
						if (curr_client != null && curr_client != this && curr_client.clientName != null) {
							curr_client.os.writeObject("*** The user " + name + " disconnected ***");
							curr_client.os.flush();
						}
					}
				}
			}
			this.is.close();
			this.os.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.println("User Session terminated");
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found");
		}
	}
	void blockcast(String line, String name) throws IOException, ClassNotFoundException {
		String[] words = line.split(":", 2);
		if (words[1].split(" ")[0].toLowerCase().equals("sendfile"))
		{
			byte[] file_data = (byte[]) is.readObject();
			synchronized(this) {
				for (clientThread curr_client : clients) {
					if (curr_client != null && curr_client != this && curr_client.clientName != null
							&& !curr_client.clientName.equals("@"+words[0].substring(1)))
					{
						curr_client.os.writeObject("Sending_File:"+words[1].split(" ",2)[1].substring(words[1].split("\\s",2)[1].lastIndexOf(File.separator)+1));
						curr_client.os.writeObject(file_data);
						curr_client.os.flush();
					}
				}
				this.os.writeObject(">>Blockcast File sent to everyone except "+words[0].substring(1));
				this.os.flush();
				System.out.println("File sent by "+ this.clientName.substring(1) + " to everyone except " + words[0].substring(1));
			}
		}
		else 
		{
			if (words.length > 1 && words[1] != null) {
				words[1] = words[1].trim();
				if (!words[1].isEmpty()) {
					synchronized (this){
						for (clientThread curr_client : clients) {
							if (curr_client != null && curr_client != this && curr_client.clientName != null
									&& !curr_client.clientName.equals("@"+words[0].substring(1))) {
								curr_client.os.writeObject("<" + name + "> " + words[1]);
								curr_client.os.flush();
							}
						}
						this.os.writeObject(">>Blockcast message sent to everyone except "+words[0].substring(1));
						this.os.flush();
						System.out.println("Message sent by "+ this.clientName.substring(1) + " to everyone except " + words[0].substring(1));
					}
				}
			}
		}
	}
	void broadcast(String line, String name) throws IOException, ClassNotFoundException {
		if (line.split("\\s")[0].toLowerCase().equals("sendfile"))
		{
			byte[] file_data = (byte[]) is.readObject();
			synchronized(this){
				for (clientThread curr_client : clients) {
					if (curr_client != null && curr_client.clientName != null && curr_client.clientName!=this.clientName) 
					{
						curr_client.os.writeObject("Sending_File:"+line.split("\\s",2)[1].substring(line.split("\\s",2)[1].lastIndexOf(File.separator)+1));
						curr_client.os.writeObject(file_data);
						curr_client.os.flush();
					}
				}
				this.os.writeObject("Broadcast file sent successfully");
				this.os.flush();
				System.out.println("Broadcast file sent by " + this.clientName.substring(1));
			}
		}
		else
		{
			synchronized(this){
				for (clientThread curr_client : clients) {
					if (curr_client != null && curr_client.clientName != null && curr_client.clientName!=this.clientName) 
					{
						curr_client.os.writeObject("<" + name + "> " + line);
						curr_client.os.flush();
					}
				}
				this.os.writeObject("Broadcast message sent successfully.");
				this.os.flush();
				System.out.println("Broadcast message sent by " + this.clientName.substring(1));
			}
		}
	}
	void unicast(String line, String name) throws IOException, ClassNotFoundException {
		String[] words = line.split(":", 2); 
		if (words[1].split(" ")[0].toLowerCase().equals("sendfile"))
		{
			byte[] file_data = (byte[]) is.readObject();
			for (clientThread curr_client : clients) {
				if (curr_client != null && curr_client != this && curr_client.clientName != null
						&& curr_client.clientName.equals(words[0]))
				{
					curr_client.os.writeObject("Sending_File:"+words[1].split(" ",2)[1].substring(words[1].split("\\s",2)[1].lastIndexOf(File.separator)+1));
					curr_client.os.writeObject(file_data);
					curr_client.os.flush();
					System.out.println(this.clientName.substring(1) + " transferred a private file to client "+ curr_client.clientName.substring(1));
					this.os.writeObject("Private File sent to " + curr_client.clientName.substring(1));
					this.os.flush();
					break;
				}
			}
		}
		else
		{
			if (words.length > 1 && words[1] != null) {
				words[1] = words[1].trim();
				if (!words[1].isEmpty()) {
					for (clientThread curr_client : clients) {
						if (curr_client != null && curr_client != this && curr_client.clientName != null
								&& curr_client.clientName.equals(words[0])) {
							curr_client.os.writeObject("<" + name + "> " + words[1]);
							curr_client.os.flush();
							System.out.println(this.clientName.substring(1) + " transferred a private message to client "+ curr_client.clientName.substring(1));
							this.os.writeObject("Private Message sent to " + curr_client.clientName.substring(1));
							this.os.flush();
							break;
						}
					}
				}
			}
		}
	}


}