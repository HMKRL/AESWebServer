package network;	

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class Server extends Thread
{
	private ServerSocket m_serverSocket;
	private Socket m_socket;
	private BlockingQueue<String> taskQueue;
	private char[] cipherText;
	private Boolean running;

	public Server(int port, BlockingQueue<String> q)
	{
		try
		{
			m_serverSocket = new ServerSocket(port);
			m_serverSocket.setSoTimeout(2000);
			taskQueue = q;
			running = true;
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	@Override
	public void run() {
		while(running) {
			try {
				System.out.println("Wainting for connection......");
				while(true) {
					try {
						m_socket = m_serverSocket.accept();
						break;
					}
					catch(SocketException se){
						System.out.println(se.getMessage());
					}
				}
				System.out.println("Connection established!");

				PrintWriter writer;
				BufferedReader reader;
				
				writer = new PrintWriter(m_socket.getOutputStream());
	
				reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
				
				String str;
				while(!reader.ready() && running);
				if(!running) break;
				
				str = reader.readLine();
				if(str.substring(0, 3).equals("GET")) {
					while(true) {
						str = reader.readLine();
						System.out.println(str);
						if(str.isEmpty()) break;
					}
				}
				
				else if(str.substring(0, 4).equals("POST")) {
					Boolean flag = true;
					String temp = "" + str + '\n', body = "";
					while(flag) {
						str = reader.readLine();
						temp += str + '\n';
						if(str.isEmpty()) {
							str = reader.readLine();
							taskQueue.add(str);
							body += str + '\n';
							str = reader.readLine();
							body += str + '\n';
							int recievedLength = Integer.parseInt(str);
							cipherText = new char[recievedLength];
							reader.read(cipherText, 0, recievedLength);
							body += new String(cipherText);
							taskQueue.add(Arrays.toString(cipherText).replaceAll(", ", "").substring(1, cipherText.length + 1));
							taskQueue.add(temp);
							taskQueue.add(body);
							flag = false;
						}
					}
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
				
				index.close();
				reader.close();
				writer.close();
				m_socket.close();
			}
			catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		try {
			m_serverSocket.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void requestStop() {
		running = false;
	}
}
