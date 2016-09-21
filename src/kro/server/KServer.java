/*
 * @author Krokozyabra
 */

package kro.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class KServer{
	ServerSocket serverSocket;
	
	
	int kaTimeout = 0;//default keep-alive timeout
	
	public KServer(int port){
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
			
			//создаем поток для одновременной бработки нескольких запросов
			new Thread(new Runnable(){
				public void run(){
					try{
						Thread.sleep(100);

						handle(socket);//обработка запроса
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
			String[] lines = getLines(cs);//разбиение на строки


			request.requestLine = lines[0];//первая строка
			request.headers = getHeaders(lines);//получение хейдеров
			request.content = getContent(bs, cs);//контента
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
				long fTime = System.currentTimeMillis();//текущее время
				
				boolean responsed = false;//не закрывать, пока не будет ответа
				
				la: while(true){
					try{
						while(socket.getInputStream().available() == 0){
							if((socket.isClosed() || System.currentTimeMillis() - fTime > kaTimeout) && responsed){
								try{
									socket.close();
								}catch(Exception ex){
								}
								break la;
							}
						}
						
						fTime = System.currentTimeMillis();
						Thread.sleep(100);
						
						handle(socket);
						
						responsed = true;
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	public void setKeppAliveTimeout(int timeout){
		this.kaTimeout = timeout;
	}
	
	public int getKeppAliveTimeout(){
		return kaTimeout;
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
