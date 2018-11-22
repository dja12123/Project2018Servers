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
	//매개변수로 ArrayList<문자열> 타입으로 넘기면 bash명령이 한줄씩 실행됨
	public static final Logger cmdlogger = LogWriter.createLogger(CommandExecutor.class, "cmd");
	public static final String lineSeparator = System.getProperty("line.separator");
	/*
	public static void executeBash(ArrayList<String> cmd) throws Exception {
		//StringBuffer successOutput = new StringBuffer();
		//StringBuffer errorOutput = new StringBuffer();
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
	        	cmdlogger.log(Level.INFO, msg + lineSeparator);
	        }
	        errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
	        while((msg = errorBufferReader.readLine()) != null) {
	        	cmdlogger.log(Level.INFO, msg + lineSeparator);
	        }
	        process.waitFor();
	        
	        //shell 실행이 정상종료/ 비정상종료 됬을때 콘솔에 로그 표시
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
	        }
	        
	    }
	    finally {
	        tempScript.delete();
	        
	    }
	    successBufferReader.close();
        errorBufferReader.close();
	}*/
	
	//명령을 mv -r /df /fd 하고 싶으면 매개변수로 ("mv -r /df /fd") 이런식으로 넘기면 명령줄 실행
	public static String executeCommand(String cmd) throws Exception {

        return executeCommand(cmd, true);
	}
	
	//명령을 mv -r /df /fd 하고 싶으면 매개변수로 ("mv -r /df /fd") 이런식으로 넘기면 명령줄 실행
	public static String executeCommand(String cmd, boolean isPrint) throws Exception {

		BufferedReader successBufferReader = null;
		BufferedReader errorBufferReader = null;
		String msg = null;
		StringBuffer resultMsg = new StringBuffer();
		
		Runtime runtime = Runtime.getRuntime();
        //ProcessBuilder pb = new ProcessBuilder(cmd);
        
        //pb.inheritIO();
        
        //Process process = pb.start();
		if(isPrint) {
			cmdlogger.log(Level.INFO, "명령 실행: " + cmd);
		}
		
		Process process = runtime.exec(cmd);
		
        successBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
        while((msg = successBufferReader.readLine()) != null) {
        	if(isPrint) {
        		cmdlogger.log(Level.INFO, "실행 결과: " + msg);
        	}
        	resultMsg.append(msg + lineSeparator);
        }
        errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

        while((msg = errorBufferReader.readLine()) != null) {
        	if(isPrint) {
        		cmdlogger.log(Level.INFO, "실행 오류: " +msg);
        	}
        	resultMsg.append(msg + lineSeparator);
        }
        
        process.waitFor();
        
        process.destroy();
        if(successBufferReader != null) {
        	successBufferReader.close();
        }
        if(errorBufferReader != null) {
            errorBufferReader.close();
        }
        
        return resultMsg.toString();
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