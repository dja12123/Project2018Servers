package node.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.IServiceModule;
import node.NodeControlCore;
import node.bash.CommandExecutor;
import node.db.DB_Handler;
import node.device.DeviceInfoManager;
import node.log.LogWriter;
import node.network.NetworkEvent;
import node.network.packet.Packet;
import node.network.packet.PacketUtil;
import node.network.socketHandler.BroadcastSender;
import node.network.socketHandler.RawSocketReceiver;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NetworkManager implements IServiceModule
{
	public static final Logger logger = LogWriter.createLogger(NetworkManager.class, "network");
	
	public static final String PROP_INFOBROADCAST_PORT = "infoBroadcastPort";
	public static final String PROP_INTERFACE = "networkInterface";
	
	public final DeviceInfoManager deviceInfoManager;
	
	private final RawSocketReceiver rawSocketReceiver;
	private final BroadcastSender broadcastSender;
	
	private HashMap<String, Observable<NetworkEvent>> observerMap;
	
	public NetworkManager(DeviceInfoManager deviceInfoManager)
	{
		this.deviceInfoManager = deviceInfoManager;

		this.rawSocketReceiver = new RawSocketReceiver(this, this.deviceInfoManager);
		this.broadcastSender = new BroadcastSender();
		
		
		this.observerMap = new HashMap<String, Observable<NetworkEvent>>();
	}
	
	public void addObserver(String key, Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> ob = this.observerMap.getOrDefault(key, null);
		if(ob == null)
		{
			ob = (new Observable<NetworkEvent>());
			this.observerMap.put(key, ob);
		}
		
		ob.addObserver(observer);
	}
	
	public void removeObserver(String key, Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> observable = this.observerMap.getOrDefault(key, null);
		if(observable == null)
		{
			return;
		}
		observable.removeObserver(observer);
		
		if(observable.size() == 0)
		{
			this.observerMap.remove(key);
		}
	}
	
	public void removeObserver(Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> observable;
		for(String key : this.observerMap.keySet())
		{
			observable = this.observerMap.get(key);
			observable.removeObserver(observer);
			
			if(observable.size() == 0)
			{
				this.observerMap.remove(key);
			}
		}
	}
	
	public void sendMessage(Packet packet)
	{
		if(packet.isBroadcast())
		{
			this.broadcastSender.sendMessage(packet.getDataByte());
		}
	}
	
	public void socketReadCallback(InetAddress addr, byte[] packetBuffer)
	{
		if(!PacketUtil.isPacket(packetBuffer))
		{
			return;
		}
		
		Packet packetObj = new Packet(packetBuffer);
		
		String eventKey = packetObj.getKey();
		
		Observable<NetworkEvent> observable = observerMap.getOrDefault(eventKey, null);
		if(observable == null)
		{
			return;
		}
		
		NetworkEvent event = new NetworkEvent(eventKey, addr, packetObj);
		observable.notifyObservers(NodeControlCore.mainThreadPool, event);
	}

	@Override
	public boolean startModule()
	{
		this.rawSocketReceiver.start();
		this.broadcastSender.start();
		return true;
	}

	@Override
	public void stopModule()
	{
		this.observerMap.clear();
		this.rawSocketReceiver.stop();
		this.broadcastSender.stop();
	}
	
	public static void main(String[] args) throws UnknownHostException
	{
		/*NodeControlCore.init();
		NetworkManager networkManager = new NetworkManager();
		networkManager.startModule();
		networkManager.setInetAddr(InetAddress.getByName("192.168.0.99"));*/
	}
	
	public void setInetAddr(InetAddress inetAddress)
	{
		ArrayList<String> command = new ArrayList<String>();
		String iface = NodeControlCore.getProp(PROP_INTERFACE);
		System.out.println("interface: " + iface);
		
		byte[] myAddrByte = inetAddress.getAddress();
		myAddrByte[3] = 1;
		String gatewayAddr = null;
		try
		{
			gatewayAddr = InetAddress.getByAddress(myAddrByte).getHostAddress();
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(gatewayAddr);
		command.add(String.format("ifdown %s", iface));
		command.add(String.format("ip addr flush dev %s", iface));
		command.add(String.format("ip addr change dev %s %s/24", iface, inetAddress.getHostAddress()));
		command.add(String.format("ip route add default via %s", gatewayAddr));
		command.add(String.format("ifup %s", iface));
		
		synchronized (this.rawSocketReceiver)
		{
			logger.log(Level.INFO, "IP�옱�븷�떦...");
			this.rawSocketReceiver.stop();
			try
			{
				CommandExecutor.executeBash(command);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			this.rawSocketReceiver.start();
			logger.log(Level.INFO, "�셿猷�");
		}
		
	}
}
