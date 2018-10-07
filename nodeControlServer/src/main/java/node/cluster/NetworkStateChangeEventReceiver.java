package node.cluster;

import node.network.NetworkStateChangeEvent;
import node.util.observer.Observable;
import node.util.observer.Observer;

public class NetworkStateChangeEventReceiver implements Observer<NetworkStateChangeEvent>{

	private NetworkStateChangeEvent event = null;
	
	@Override
	public void update(Observable<NetworkStateChangeEvent> object, NetworkStateChangeEvent data) {
		// TODO Auto-generated method stub
		event = data;
		
		
	}
	public NetworkStateChangeEvent getEvent() {	return event;	}
	
}
