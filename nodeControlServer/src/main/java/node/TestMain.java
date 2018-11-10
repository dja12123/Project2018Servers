package node;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import com.savarese.rocksaw.net.RawSocket;

import node.fileIO.FileHandler;


public class TestMain
{
	static
	{
		
		File rawSocketLib = FileHandler.getExtResourceFile("rawSocket");
		StringBuffer libPathBuffer = new StringBuffer();
		libPathBuffer.append(rawSocketLib.toString());
		libPathBuffer.append(";");
		libPathBuffer.append(System.getProperty("java.library.path"));
		
		System.setProperty("java.library.path", libPathBuffer.toString());
		Field sysPathsField = null;
		try
		{
			sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.mapLibraryName("rocksaw");

	}
	public static void main(String[] args)
	{
		NodeControlCore.init();
		
		System.out.println(System.getProperty("java.library.path"));
		
		
		
		RawSocket rawSocket = new RawSocket();
		try
		{
			rawSocket.open(RawSocket.PF_INET, RawSocket.getProtocolByName("UDP"));
		}
		catch (IllegalStateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] buffer = new byte[100000];
		while(true)
		{
			int readLen = 0;
			try
			{
				readLen = rawSocket.read(buffer);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i = 0; i < readLen; ++i)
			{
				System.out.println(buffer[i]);
			}
		}
		
		
	}
}
