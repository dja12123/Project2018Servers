package node.detection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;

import node.NodeControlCore;
import node.device.DeviceInfoManager;
import node.network.NetworkUtil;

public class DetectionUtil
{
	private static InetAddress masterAddr;
	
	public static final String PROP_delayMasterNodeBroadcast = "delayMasterNodeBroadcast";
	public static final String PROP_masternodeIP = "masternodeIP";
	
	static
	{
		String masterIP = NodeControlCore.getProp(PROP_masternodeIP);
		String fullIP = String.format("%s.%s", NetworkUtil.DEFAULT_SUBNET, masterIP);
		try
		{
			masterAddr = InetAddress.getByName(fullIP);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}
	
	public static InetAddress masterAddr()
	{
		return masterAddr;
	}
	
	public static boolean isChangeMasterNode(NodeInfoProtocol anotherMasterNodeInfo, UUID masterNode, DeviceInfoManager deviceInfoManager)
	{
		NodeDetectionService.logger.log(Level.WARNING, String.format("마스터 노드 겹침 확인(%s <=> %s)", masterNode.toString(), anotherMasterNodeInfo.getMasterNode().toString()));
		if(anotherMasterNodeInfo.getSize() == deviceInfoManager.getNodeCount())
		{// 상대 마스터 노드와 내 마스터 노드의 추종자 노드 개수가 같을때.
			if(anotherMasterNodeInfo.getMasterNode().toString().compareTo(masterNode.toString()) > 0)
			{// UUID 비교.
				return true;
			}
		}
		else if(anotherMasterNodeInfo.getSize() > deviceInfoManager.getNodeCount())
		{// 상대 마스터 노드의 추종자 노드가 더 많을때.
			return true;
		}
		return false;
	}
}
