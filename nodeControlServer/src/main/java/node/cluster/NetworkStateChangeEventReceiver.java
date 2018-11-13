package node.cluster;

<<<<<<< HEAD
import java.util.logging.Level;

import node.detection.NetworkStateChangeEvent;
=======
import node.detection.NodeDetectionEvent;
>>>>>>> 7b633e20ce67b38f21b226dff618fc6ac817880e
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