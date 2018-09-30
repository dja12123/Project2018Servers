package fileIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileHandler {
	public static String readFileString(String path) {
		File file = new File(path);
		BufferedReader bufRead;
		
		try {
			bufRead = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.out.println("[Error] File is not found");
			return null;
		}

		StringBuffer fileReadString = new StringBuffer();
		String tempReadString = "";
		
        try 
        {
			while ((tempReadString = bufRead.readLine()) != null) 
			{
				fileReadString.append(tempReadString);
			}
			
			bufRead.close();
        } 
		catch (IOException e) 
		{
			System.out.println("[Error] File Buffer Error. File is not found.");
			return null;
		}
		
		return fileReadString.toString();
	}	
}
