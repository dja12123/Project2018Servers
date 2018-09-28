package node.network.communicator;

import java.util.HashMap;
import java.util.Map;

import node.network.packet.Packet;
import node.util.observer.Observable;
import node.util.observer.Observer;

/**
  * @FileName : NetworkObservable.java
  * @Project : Project2018Servers
  * @Date : 2018. 9. 26. 
  * @작성자 : dja12123
  * @변경이력 :
  * @프로그램 설명 : 네트워크 수신기 옵저버블
  */
public class NetworkObservable
{
	private Map<String, Observable<NetworkEvent>> observerMap;
	
	private NetworkObservable()
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
	
	void packetReceive(Packet packet, DeviceInfo sender)
	{
		NetworkEvent eventObj = new NetworkEvent(packet, sender);
		String key = packet.getKey();
		Observable<NetworkEvent> observable = this.observerMap.getOrDefault(key, null);
		if(observable != null)
		{
			observable.notifyObservers(eventObj);
		}
	}
}
