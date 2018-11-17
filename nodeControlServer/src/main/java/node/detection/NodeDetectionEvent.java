package node.detection;

import java.net.InetAddress;

public class NodeDetectionEvent {
	public static final int STATE_UPLINK = 0;
	public static final int STATE_FAIL = 1;
	
	public final InetAddress masterIP;
	// 마스터 노드 아이피 (link fail일경우 null)
	public final boolean isMaster;
	// 내 가 마스터인지 아닌지
	public final int state;
	// 링크 상태
	
	public NodeDetectionEvent(InetAddress masterIP, boolean isMaster, int what) {
		this.masterIP = masterIP;
		this.isMaster = isMaster;
		this.state = what;
	}
	public NodeDetectionEvent(InetAddress masterIP, boolean isMaster) {
		this(masterIP, isMaster, 0);
	}
}
