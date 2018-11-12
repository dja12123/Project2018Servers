package node.detection;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.network.NetworkManager;
import node.NodeControlCore;
import node.log.LogWriter;
import node.network.NetworkEvent;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NodeInstaller implements Runnable
{
	public static final Logger logger = LogWriter.createLogger(NodeInstaller.class, "nodeInstaller");
	
	private static final String PROP_DEFAULT_WAIT_TIME = "nodeInitDefaultWaitTime";
	private static final String PROP_RANDOM_WAIT_TIME = "nodeInitRandomWaitTime";
	
	private NetworkManager networkManager;
	private Thread waitThread;
	private NodeDetectionService nodeDetectionService;
	private NodeInfoProtocol masterNodeData;
	private Observer<NetworkEvent> networkObserverFunc;
	private boolean isRun;
	
	private int defaultWaitTime;
	private int randomWaitTime;

	public NodeInstaller(NodeDetectionService nodeDetectionService, NetworkManager networkManager)
	{
		this.nodeDetectionService = nodeDetectionService;
		this.networkManager = networkManager;
		//this.dbHandler.getInstaller().checkAndCreateTable(NODE_TABLE_SCHEMA);
		this.networkObserverFunc = this::updateNetwork;
		this.isRun = false;
	}

	public synchronized void updateNetwork(Observable<NetworkEvent> object, NetworkEvent data)
	{
		//String addr = data.inetAddr.getHostAddress();
		//String uuid = data.packet.getSender().toString();
		//Time time = new Time(System.currentTimeMillis());
		
		if(data.key.equals(MasterNodeService.KPROTO_MASTER_BROADCAST))
		{
			this.masterNodeData = new NodeInfoProtocol(data.packet);
			this.waitThread.interrupt();
		}
	}
	
	public synchronized void start()
	{
		if(this.isRun) return;
		logger.log(Level.INFO, "노드 초기화 활성화");
		
		this.defaultWaitTime = Integer.parseInt(NodeControlCore.getProp(PROP_DEFAULT_WAIT_TIME));
		this.randomWaitTime = Integer.parseInt(NodeControlCore.getProp(PROP_RANDOM_WAIT_TIME));
		
		this.waitThread = new Thread(this);
		this.waitThread.start();
		this.networkManager.addObserver(MasterNodeService.KPROTO_MASTER_BROADCAST, this.networkObserverFunc);
		this.isRun = true;
	}
	
	public synchronized void stop()
	{
		if(!this.isRun) return;
		logger.log(Level.INFO, "노드 초기화 중지");
		this.networkManager.removeObserver(MasterNodeService.KPROTO_MASTER_BROADCAST, this.networkObserverFunc);
		this.isRun = false;
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep(this.defaultWaitTime);
		}
		catch (InterruptedException e)
		{
			//마스터 노드 감지
			logger.log(Level.INFO, String.format("마스터 노드 감지(%s)", this.masterNodeData.getMasterNode().toString()));
			this.nodeDetectionService.workNodeSelectionCallback(this.masterNodeData);
			this.stop();
			return;
		}
		int randomWaitTime = (int)(Math.random() * this.randomWaitTime);
		logger.log(Level.INFO, String.format("마스터 노드 미감지, 랜덤한 시간 대기 (%dms)", randomWaitTime));
		try
		{
			Thread.sleep(randomWaitTime);
		}
		catch (InterruptedException e)
		{
			logger.log(Level.INFO, String.format("마스터 노드 감지(%s)", this.masterNodeData.getMasterNode().toString()));
			this.nodeDetectionService.workNodeSelectionCallback(this.masterNodeData);
			this.stop();
			return;
		}
		logger.log(Level.INFO, "마스터 노드 선언");
		this.nodeDetectionService.masterNodeSelectionCallback();
		this.stop();
	}
}	
