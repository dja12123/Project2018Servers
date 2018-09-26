package node.network.communicator;

import java.util.HashMap;
import java.util.Map;

import node.network.packet.Packet;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class CommunicationHandler
{
	private Map<String, Observable<NetworkEvent>> observerMap;
	
	private CommunicationHandler()
	{
		this.observerMap = new HashMap<String, Observable<NetworkEvent>>();
	}
	
	public void addObserver(String key, Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> observable = this.observerMap.getOrDefault(key, null);
		if(observable == null)
		{
			observable = new Observable<NetworkEvent>();
			this.observerMap.put(key, observable);
		}
		observable.addObserver(observer);
	}
	
	public void removeObserver(String key, Observer<NetworkEvent> observer)
	{
		Observable<NetworkEvent> observable = this.observerMap.getOrDefault(key, null);
		if(observable == null) return;
		observable.removeObserver(observer);
	}
	
	public void removeObserver(Observer<NetworkEvent> observer)
	{
		for(String key : this.observerMap.keySet())
		{
			Observable<NetworkEvent> observable = this.observerMap.get(key);
			observable.removeObserver(observer);
			
			if(observable.size() == 0)
				this.observerMap.remove(key);
		}
	}
	
	public void sendMessage(Packet packet)
	{
		
	}
}
