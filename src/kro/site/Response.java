package kro.site;


import java.io.OutputStream;
import java.util.ArrayList;

public class Response{
	String statusLine = "HTTP/1.1 200 OK";
	String[] headers = null;
	byte[] content = null;

	ArrayList<String> headersList = new ArrayList<String>();


	public void addHeader(String header){
		headersList.add(header);
		headers = new String[headersList.size()];

		for(int i = 0; i < headers.length; i++){
			headers[i] = headersList.get(i);
		}
	}

	public void setStatusLine(String statusLine){
		this.statusLine = statusLine;
	}

	public void setContent(byte[] content){
		this.content = content;
	}


	public void send(OutputStream os) throws Exception{
		String response = "";
		response += statusLine + "\r\n";
		if(headers != null){

			for(int i = 0; i < headers.length; i++){
				response += headers[i] + "\r\n";
			}
		}
		response += "\r\n";

		os.write(response.getBytes());
		if(content != null){
			os.write(content);
		}
		
		os.flush();
	}
}
