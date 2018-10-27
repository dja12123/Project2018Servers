package node.detection;

import java.util.Random;
import java.util.logging.Level;

import node.network.NetworkManager;
import node.network.NetworkEvent;
import node.network.SocketHandler;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NodeInstaller implements Runnable
{
	private static final int DEFAULT_WAIT_TIME = 5000;
	private static final int RANDOM_WAIT_TIME = 5000;
	private NetworkManager networkManager;
	private Thread waitThread;
	private NodeDetectionService nodeDetectionService;
	private NodeInfoProtocol masterNodeData;
	private Observer<NetworkEvent> networkObserverFunc;

	public NodeInstaller(NodeDetectionService nodeDetectionService, NetworkManager networkManager)
	{
		this.nodeDetectionService = nodeDetectionService;
		this.networkManager = networkManager;
		//this.dbHandler.getInstaller().checkAndCreateTable(NODE_TABLE_SCHEMA);
		this.networkObserverFunc = this::updateNetwork;
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
		NetworkManager.networkLogger.log(Level.INFO, "노드 알림 수신 시작");
		this.waitThread = new Thread(this);
		this.waitThread.start();
		this.networkManager.addObserver(MasterNodeService.KPROTO_MASTER_BROADCAST, this.networkObserverFunc);
	}
	
	public synchronized void stop()
	{
		this.networkManager.removeObserver(MasterNodeService.KPROTO_MASTER_BROADCAST, this.networkObserverFunc);
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep(DEFAULT_WAIT_TIME);
		}
		catch (InterruptedException e)
		{
			//마스터 노드 감지
			this.nodeDetectionService.workNodeSelectionCallback(this.masterNodeData);
			this.stop();
			return;
		}
		
		int randomWaitTime = new Random(RANDOM_WAIT_TIME).nextInt();
		try
		{
			Thread.sleep(randomWaitTime);
		}
		catch (InterruptedException e)
		{
			this.nodeDetectionService.workNodeSelectionCallback(this.masterNodeData);
			this.stop();
			return;
		}
		this.nodeDetectionService.masterNodeSelectionCallback();
		this.stop();
	}
}	
