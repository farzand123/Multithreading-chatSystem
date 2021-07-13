import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

	private static Socket clientSocket = null;
	private static ObjectOutputStream os = null;
	private static ObjectInputStream is = null;
	private static BufferedReader inputLine = null;
	private static BufferedInputStream bis = null;
	private static boolean closed = false;
	public static void main(String[] args) {
		int portNumber = 1234;
		String host = "localhost";
		if (args.length < 2) {
			System.out.println("Default Server: " + host + ", Default Port: " + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
			System.out.println("Server: " + host + ", Port: " + portNumber);
		}
		try {
			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			os = new ObjectOutputStream(clientSocket.getOutputStream());
			is = new ObjectInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Unknown " + host);
		} catch (IOException e) {
			System.err.println("No Server found. Please ensure that the Server program is running and try again.");
		}
		if (clientSocket != null && os != null && is != null) {
			try {
				new Thread(new Client()).start();
				while (!closed) {
					String msg = (String) inputLine.readLine().trim();
					if ((msg.split(":").length > 1))
					{
						if (msg.split(":")[1].toLowerCase().startsWith("sendfile"))
						{
							File sfile = new File((msg.split(":")[1]).split(" ",2)[1]);					
							if (!sfile.exists())
							{
								System.out.println("File Doesn't exist!!");
								continue;
							}			
							byte [] mybytearray  = new byte [(int)sfile.length()];
							FileInputStream fis = new FileInputStream(sfile);
							bis = new BufferedInputStream(fis);
							while (bis.read(mybytearray,0,mybytearray.length)>=0)
							{
								bis.read(mybytearray,0,mybytearray.length);
							}
							os.writeObject(msg);
							os.writeObject(mybytearray);
							os.flush();
						}
						else
						{
							os.writeObject(msg);
							os.flush();
						}
					}
					else if (msg.toLowerCase().startsWith("sendfile"))
					{
						File sfile = new File(msg.split(" ",2)[1]);				
						if (!sfile.exists())
						{
							System.out.println("File Doesn't exist!!");
							continue;
						}
						byte [] mybytearray  = new byte [(int)sfile.length()];
						FileInputStream fis = new FileInputStream(sfile);
						bis = new BufferedInputStream(fis);
						while (bis.read(mybytearray,0,mybytearray.length)>=0)
						{
							bis.read(mybytearray,0,mybytearray.length);
						}
						os.writeObject(msg);
						os.writeObject(mybytearray);
						os.flush();
					}
					else 
					{
						os.writeObject(msg);
						os.flush();
					}
				}
				os.close();
				is.close();
				clientSocket.close();
			} catch (IOException e) 
			{
				System.err.println("IOException:  " + e);
			}
		}
	}
	public void run() {
		String responseLine;
		String filename = null;
		byte[] ipfile = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		File directory_name = null;
		String full_path;
		String dir_name = "Received_Files";
		try {
			while ((responseLine = (String) is.readObject()) != null)  {
				if (responseLine.equals("Directory Created"))
				{
					directory_name = new File((String) dir_name);
					if (!directory_name.exists())
					{
						directory_name.mkdir();
						System.out.println("New Receiving file directory for this client created!!");
					}
					else
					{
						System.out.println("Receiving file directory for this client already exists!!");
					}
				}
				else if (responseLine.startsWith("Sending_File"))
				{
					try
					{
						filename = responseLine.split(":")[1];
						full_path = directory_name.getAbsolutePath()+"/"+filename; 
						ipfile = (byte[]) is.readObject();
						fos = new FileOutputStream(full_path);
						bos = new BufferedOutputStream(fos);
						bos.write(ipfile);
						bos.flush();
						System.out.println("File Received.");
					}
					finally
					{
						if (fos != null) fos.close();
						if (bos != null) bos.close();
					}
				}
				else
				{
					System.out.println(responseLine);
				}
				if (responseLine.indexOf("*** Bye") != -1)		
					break;
			}
			closed = true;
			System.exit(0);
	} catch (IOException | ClassNotFoundException e) {
			System.err.println("Server Process Stopped Unexpectedly!!");
		}
	}
}