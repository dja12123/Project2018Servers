package node.web;

import node.IServiceModule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class WebServiceMain extends NanoHTTPD implements IServiceModule 
{
	private static final Logger LOG = Logger.getLogger(WebServiceMain.class.getName());
	
	public WebServiceMain() {
		super(80);
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		Method method = session.getMethod();
		String uri = session.getUri();
		WebServiceMain.LOG.info(method + " '" + uri + "' ");
		
		File readFile = new File("~/nanohttpd/www/index.html");
		BufferedReader readBuffer = null;
		try 
		{
			readBuffer = new BufferedReader(new FileReader(readFile));
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		String msg = null;
		
        try 
        {
			while ((msg = readBuffer.readLine()) != null) {}
			readBuffer.close();
        } 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
        catch (NullPointerException e1)
        {
        	e1.printStackTrace();
        }


		return newFixedLengthResponse(msg);
	}
	
	@Override
	public boolean start()
	{
		ServerRunner.run(WebServiceMain.class);
		return true;
	}

	@Override
	public void stop()
	{
		
		
	}

}
