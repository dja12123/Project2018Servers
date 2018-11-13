package node.cluster;

import node.detection.NodeDetectionEvent;

import node.util.observer.Observable;
import node.util.observer.Observer;

public class NetworkStateChangeEventReceiver implements Observer<NodeDetectionEvent>{

	private NodeDetectionEvent event = null;
	private ClusterService mainModule;
	
	public NetworkStateChangeEventReceiver(ClusterService mainModule) {
		// TODO Auto-generated constructor stub
		this.mainModule = mainModule;
	}
	@Override
	public void update(Observable<NodeDetectionEvent> object, NodeDetectionEvent data) {
		// TODO Auto-generated method stub
		event = data;
		mainModule.reciveEvent(getEvent());
		mainModule.startSpark();
	}
	public NodeDetectionEvent getEvent() {	return event;	}
	
}