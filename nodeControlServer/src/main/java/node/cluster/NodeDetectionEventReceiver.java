package node.cluster;

import node.detection.NodeDetectionEvent;

import node.util.observer.Observable;
import node.util.observer.Observer;

public class NodeDetectionEventReceiver implements Observer<NodeDetectionEvent>{

	private NodeDetectionEvent event = null;
	private ClusterService mainModule;
	
	public NodeDetectionEventReceiver(ClusterService mainModule) {
		// TODO Auto-generated constructor stub
		this.mainModule = mainModule;
	}
	@Override
	public void update(Observable<NodeDetectionEvent> object, NodeDetectionEvent data) {
		// TODO Auto-generated method stub
		event = data;
		mainModule.reciveEvent(data);
		mainModule.startSpark();
	}
	public NodeDetectionEvent getEvent() {	return event;	}
	
}