/*
 * @author Krokozyabra
 */

package kro.site;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class KSite{
	ServerSocket serverSocket;
	
	public KSite(int port){
		new Thread(new Runnable(){
			public void run(){
				try{
					serverSocket = new ServerSocket(port);

					start();
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}).start();
	}

	private void start() throws Exception{
		while(true){
			Socket socket = serverSocket.accept();

			//создаем поток для нескольких клиентов
			new Thread(new Runnable(){
				public void run(){
					try{
						Thread.sleep(100);

						handle(socket);
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}).start();
		}
	}

	private void handle(Socket socket) throws Exception{
		Request request = new Request();

		try{
			byte[] bs = getBytes(socket);
			char[] cs = getChars(bs);
			String[] lines = getLines(cs);


			request.requestLine = lines[0];
			request.headers = getHeaders(lines);
			request.content = getContent(bs, cs);
			request.lines = lines;
			
			if(!socket.getKeepAlive() && request.getHeader("Connection").contains("keep-alive")){
				socket.setKeepAlive(true);
				addKeepAliveThread(socket);
			}
		}catch(Exception ex){
		}
		
		handle(socket, request);
	}
	
	private void addKeepAliveThread(Socket socket){
		new Thread(new Runnable(){
			public void run(){
				while(true){
					try{
						while(socket.getInputStream().available() == 0);
						Thread.sleep(100);

						handle(socket);
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}
		}).start();
	}

	protected abstract void handle(Socket socket, Request request);

	private byte[] getContent(byte[] bs, char[] cs){
		int index = new String(cs).indexOf("\r\n\r\n") + 4;

		return Arrays.copyOfRange(bs, index, bs.length);
	}

	private String[][] getHeaders(String[] lines){
		String[][] headers = new String[lines.length - 1][2];

		for(int i = 0; i < lines.length - 1; i++){
			headers[i] = lines[i + 1].split(": ");
		}
		return headers;
	}

	private String[] getLines(char[] cs){
		String string = new String(cs);
		string = string.split("\r\n\r\n")[0];

		return string.split("\r\n");
	}

	private char[] getChars(byte[] bs){
		char[] cs = new char[bs.length];
		for(int i = 0; i < cs.length; i++){
			cs[i] = (char)bs[i];
		}

		return cs;
	}

	private byte[] getBytes(Socket socket) throws Exception{
		ArrayList<Byte> bs = new ArrayList<Byte>();

		while(socket.getInputStream().available() > 0){
			bs.add((byte)socket.getInputStream().read());
		}

		byte[] bsArr = new byte[bs.size()];
		for(int i = 0; i < bsArr.length; i++){
			bsArr[i] = bs.get(i);
		}

		return bsArr;
	}


}
