package kro.server;


public class Request{
	String requestLine;
	String[][] headers;
	byte[] content;
	
	String[] lines;
	
	public String getRequestLine(){
		return requestLine;
	}
	
	public String[][] getHeaders(){
		return headers;
	}
	
	public String[] getLines(){
		return lines;
	}
	
	public String getHeader(String header){
		for(int i = 0; i < headers.length; i++){
			if(headers[i][0].equals(header)){
				return headers[i][1];
			}
		}
		return "";
	}
	
	public byte[] getContent(){
		return content;
	}
}
