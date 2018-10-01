package node.fileIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileHandler {
	public static File[] getFileList(String path) {
		File[] fileList = null;
		try {
			fileList = new File(path).listFiles();
		} catch (SecurityException e) {
			System.out.println("[Error] Permission denied. Check Permission!");
		}
		
		if (fileList == null) {
			System.out.println("[Error] Directory is not found");
		}
		
		//Test Code
		if (fileList != null) {
			for (File file : fileList) {
				System.out.println(file.getName());
			}
		}
		
		return fileList;
	}
	
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
				//System.out.println(tempReadString);
				fileReadString.append(tempReadString + "\n");
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
	
	public static FileInputStream getFileInputStream(String path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			System.out.println("[Error] File is not found");
			return null;
		}
	}
}
