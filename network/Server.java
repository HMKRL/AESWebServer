package network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class Server extends Thread
{
	private ServerSocket m_serverSocket;
	private Socket m_socket;
	private BlockingQueue<String> taskQueue;

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
				//while(!reader.ready());
				/*while(reader.ready()) {
					System.out.print((char)reader.read());
				}*/
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
					int cnt = 0;
					while(cnt == 0) {
						str = reader.readLine();
						System.out.println(str);
						if(str.isEmpty()) {
							cnt++;
							char c;
							while(true) {
								c = (char)reader.read();
								System.out.print(c);
								if(c == '\r' || c == '\n') break;
							}
						}
					}
					System.out.println("Read finished");
				}
				
				writer.println("HTTP/1.1 200 OK\n");
				FileReader index = new FileReader("src/index.html");
				
				while(index.ready()) {
					char x = (char)index.read();
					writer.print(x);
				}
				writer.println("");
				
				writer.flush();
				
				while(!taskQueue.isEmpty()) {
					writer.print(taskQueue.peek());
					taskQueue.remove();
					writer.println("");
				}
				
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
