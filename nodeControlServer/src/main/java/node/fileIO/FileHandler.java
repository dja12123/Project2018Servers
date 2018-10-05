package node.fileIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.log.LogWriter;

public class FileHandler
{
	public static final Logger fileLogger = LogWriter.createLogger(FileHandler.class, "file");
	
	public static File[] getFileList(String file)
	{
		return getFileList(getResourceFile(file));
	}
	
	public static File[] getFileList(File file)
	{
		File[] fileList = file.listFiles();

		if (fileList == null)
		{
			fileLogger.log(Level.SEVERE, "디렉토리를 찾을 수 없음");
		}

		// Test Code
		if (fileList != null)
		{
			for (File f : fileList)
			{
				System.out.println(file.getName());
			}
		}
		return fileList;
	}

	public static File getResourceFile(String filePath)
	{
		String dir = FileHandler.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String[] temp = dir.split("/");
        
		dir = dir.substring(0, dir.length() - temp[temp.length - 1].length());
		dir += "extResource/" + filePath;
		File f = new File(dir);

		return f.exists() ? f : null;
	}

	public static FileInputStream getInputStream(File file)
	{
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(file);
		}
		catch (FileNotFoundException e)
		{
			fileLogger.log(Level.SEVERE, "인풋 스트림을 가져올 수 없음", e);
		}
		return inputStream;
	}
	
	public static FileInputStream getInputStream(String file)
	{
		return getInputStream(getResourceFile(file));
	}
	
	public static FileOutputStream getOutputStream(File file)
	{
		FileOutputStream outputStream = null;
		try
		{
			outputStream = new FileOutputStream(file);
		}
		catch (FileNotFoundException e)
		{
			fileLogger.log(Level.SEVERE, "아웃풋 스트림을 가져올 수 없음", e);
		}
		return outputStream;
	}
	
	public static FileOutputStream getOutputStream(String file)
	{
		return getOutputStream(getResourceFile(file));
	}

	public static String readFileString(File file)
	{
		BufferedReader bufRead;

		try
		{
			bufRead = new BufferedReader(new FileReader(file));
		}
		catch (FileNotFoundException e)
		{
			fileLogger.log(Level.SEVERE, "파일을 찾을 수 없음", e);
			return null;
		}

		StringBuffer fileReadString = new StringBuffer();
		String tempReadString = "";

		try
		{
			while ((tempReadString = bufRead.readLine()) != null)
			{
				// System.out.println(tempReadString);
				fileReadString.append(tempReadString + "\n");
			}

			bufRead.close();
		}
		catch (IOException e)
		{
			fileLogger.log(Level.SEVERE, "파일 버퍼 오류: 파일을 찾을 수 없음", e);
			return null;
		}

		return fileReadString.toString();
	}
	
	public static String readFileString(String file)
	{
		return readFileString(getResourceFile(file));
	}
}
