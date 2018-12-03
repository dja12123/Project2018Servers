package node.cluster.spark;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.NodeControlCore;
import node.bash.BashSet;
import node.bash.CommandExecutor;
import node.cluster.ClusterService;
import node.log.LogWriter;
import node.gpio.lcd.*;

public class SparkManager {
	private String sparkInstDir;
	
	private String sparkPort;
	private String sparkWebPort;
	
	private String sparkWorkerInstances;
	private String sparkWorkerCores;
	private String sparkWorkerMemory;
	
	private LCDObject lcdPan;
	private int lcdX = 1, lcdY = 35, lcdW = 50;
	
	public static final Logger sparkLogger = LogWriter.createLogger(SparkManager.class, "spark");
	
	public SparkManager() {
		
	}
	public void startSparkMaster(String masterIP, String option) {
		lcdPan = LCDControl.inst.replaceString(lcdPan, "Spark 마스터 동작중");
		sparkLogger.log(Level.INFO, "스파크 마스터 시작");
		StringBuffer confOp = new StringBuffer("-h ")
				.append(masterIP)
				.append(" -p ")
				.append(sparkPort)
				.append(" --webui-port ")
				.append(sparkWebPort);
		BashSet.execSh(BashSet.start_spkMaster, confOp.toString(), option);
	}
	public void stopSparkMaster() {
		lcdPan = LCDControl.inst.replaceString(lcdPan, "Spark 마스터 중지중");
		lcdPan = LCDControl.inst.removeShapeTimer(lcdPan, 2000);
		sparkLogger.log(Level.INFO, "스파크 마스터 중지");
		BashSet.execSh(BashSet.stop_spkMaster);
	}
	//option example spark://worker-11:7077 -m 512M -c 2
	public void startSparkWorker(String masterIp, String option) {
		lcdPan = LCDControl.inst.replaceString(lcdPan, "Spark 워커 동작중");
		sparkLogger.log(Level.INFO, "스파크 워커 시작");
		StringBuffer confOp = new StringBuffer("spark://")
				.append(masterIp)
				.append(":")
				.append(sparkPort)
				.append(" -m ")
				.append(sparkWorkerMemory)
				.append(" -c ")
				.append(sparkWorkerCores);
		
		BashSet.execSh(BashSet.start_spkWorker, confOp.toString(), option);
	}
	public void stopSparkWorker() {
		lcdPan = LCDControl.inst.replaceString(lcdPan, "Spark 워커 중지중");
		lcdPan = LCDControl.inst.removeShapeTimer(lcdPan, 2000);
		sparkLogger.log(Level.INFO, "스파크 워커 중지");
		BashSet.execSh(BashSet.stop_spkWorker);
	}
	
	protected void confSpark() {
		sparkLogger.log(Level.INFO, "스파크 property 설정중..");
		sparkInstDir = NodeControlCore.getProp("sparkInstDir");
		
		sparkPort = NodeControlCore.getProp("sparkPort");
		sparkWebPort = NodeControlCore.getProp("sparkWebPort");
		
		sparkWorkerInstances = NodeControlCore.getProp("sparkWorkerInstances");
		sparkWorkerCores = NodeControlCore.getProp("sparkWorkerCores");
		sparkWorkerMemory = NodeControlCore.getProp("sparkWorkerMemory");
		
		
	}
	
	
	public boolean initSpark() {
		lcdPan = LCDControl.inst.showString(lcdX, lcdY, "스파크 초기화 중");
		sparkLogger.log(Level.INFO, "스파크 초기화 중..");
		confSpark();
		String haveSpark = BashSet.execSh(BashSet.check_spark, sparkInstDir);
		if(haveSpark.equals("false" + CommandExecutor.lineSeparator) ) {
			lcdPan = LCDControl.inst.replaceString(lcdPan, "스파크 초기화 오류");
			lcdPan = LCDControl.inst.blinkShape(lcdPan, 500, 6);
			lcdPan = LCDControl.inst.removeShapeTimer(lcdPan, 3010);
			sparkLogger.log(Level.SEVERE, "Spark is Missing", new Exception("Spark is Missing"));
			return false;
		}
		lcdPan = LCDControl.inst.replaceString(lcdPan, " ");
		
		return true;
	}
}
