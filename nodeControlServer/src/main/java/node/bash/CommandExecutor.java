package node.bash;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.log.LogWriter;

public class CommandExecutor {
	//매개변수로 ArrayList<문자열> 타입으로 넘기면 bash명령이 실행됨
	public static final Logger cmdlogger = LogWriter.createLogger(CommandExecutor.class, "cmd");
	
	public static void executeCommands(ArrayList<String> cmd) throws IOException {
		StringBuffer successOutput = new StringBuffer();
		StringBuffer errorOutput = new StringBuffer();
		BufferedReader successBufferReader = null;
		BufferedReader errorBufferReader = null;
		String msg = null;
		
	    File tempScript = createTempScript(cmd);

	    try {
	        ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
	        pb.inheritIO();
	        
	        Process process = pb.start();
	        successBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
	        while((msg = successBufferReader.readLine()) != null) {
	        	cmdlogger.log(Level.SEVERE, msg + System.getProperty("line.separator"));
	        }
	        errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
	        while((msg = errorBufferReader.readLine()) != null) {
	        	cmdlogger.log(Level.SEVERE, msg + System.getProperty("line.separator"));
	        }
	        
	        process.waitFor();
	        /*//shell 실행이 정상종료/ 비정상종료 됬을때 콘솔에 로그 표시
	        if(process.exitValue() == 0) {
	        	System.out.println("Succeed Process# Here Printed Process log");
	        	System.out.println(successOutput.toString());
	        }
	        else {
	        	System.out.println("Process Stop by Error# Here Printed Process log");
	        	System.out.println(successOutput.toString());
	        }
	        //shell 실행시 에러가 발생
	        if(errorOutput.length() != 0) {
	        	System.out.println("Shell Error#Here Printed Shell log");
	        	System.out.println(errorOutput.toString());
	        }*/
	        
	    } catch (InterruptedException e) {
			e.printStackTrace();
		}
	    finally {
	        tempScript.delete();
	    }
	}
	
	//쉘 명령을 임시파일에 저장
	public static File createTempScript(ArrayList<String> cmd) throws IOException {
		Iterator<String> cmdit = cmd.iterator();
		
	    File tempScript = File.createTempFile("script", null);

	    Writer streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
	    PrintWriter printWriter = new PrintWriter(streamWriter);
	    
	    while(cmdit.hasNext()) {
	    	printWriter.println(cmdit.next());
	    }
	    
	    printWriter.close();

	    return tempScript;
	}
}
