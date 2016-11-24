package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class Server extends Thread
{
	private ServerSocket m_serverSocket;
	private Socket m_socket;
	private BlockingQueue<String> taskQueue;
	private char[] cipherText;

	public Server(int port, BlockingQueue<String> q)
	{
		try
		{
			m_serverSocket = new ServerSocket(port);
			taskQueue = q;
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				System.out.println("Wainting for connection......");
				m_socket = m_serverSocket.accept();
				System.out.println("Connection established!");

				PrintWriter writer;
				BufferedReader reader;
				
				writer = new PrintWriter(m_socket.getOutputStream());
	
				reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
				
				String str;
				str = reader.readLine();
				System.out.println(str);
				if(str.substring(0, 3).equals("GET")) {
					while(true) {
						str = reader.readLine();
						System.out.println(str);
						if(str.isEmpty()) break;
					}
				}
				
				else if(str.substring(0, 4).equals("POST")) {
					Boolean flag = true;
					while(flag) {
						str = reader.readLine();
						System.out.println(str);
						if(str.isEmpty()) {
								str = reader.readLine();
								System.out.println(str);
								str = reader.readLine();
								System.out.println(Integer.parseInt(str));
								cipherText = new char[Integer.parseInt(str)];
								reader.read(cipherText, 0, Integer.parseInt(str));
								for(char element:cipherText) System.out.print(element);
								System.out.println("");
								System.out.printf("recieved %d chars\n", cipherText.length);
								taskQueue.add(Arrays.toString(cipherText).replaceAll(", ", "").substring(1, cipherText.length + 1));
								flag = false;
						}
					}
					System.out.println("Read finished");
				}
				
				writer.print("HTTP/1.1 200 OK\r\n");
				writer.print("\r\n");
				InputStream in = getClass().getResourceAsStream("/resources/index.html");
				BufferedReader index = new BufferedReader(new InputStreamReader(in));
				
				while((str = index.readLine()) != null) {
					writer.print(str + "\r\n");
				}
				writer.println("");
				
				writer.flush();
				
				/*while(!taskQueue.isEmpty()) {
					writer.print(taskQueue.peek());
					taskQueue.remove();
					writer.println("");
				}*/
				
				index.close();
				reader.close();
				writer.close();
				m_socket.close();
			}
			catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void sendStopSignal() {
		try {
			m_socket.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
