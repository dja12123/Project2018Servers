package node.cluster;

import node.network.NetworkConnectEvent;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NetworkConnectEventReceiver implements Observer<NetworkConnectEvent>{

	private NetworkConnectEvent event;
	
	@Override
	public void update(Observable<NetworkConnectEvent> object, NetworkConnectEvent data) {
		// TODO Auto-generated method stub
		event = data;
		
		
	}

	
}
