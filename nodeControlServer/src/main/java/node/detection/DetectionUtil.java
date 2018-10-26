package node.detection;

import java.util.UUID;
import java.util.logging.Level;

import node.device.DeviceInfoManager;

public class DetectionUtil
{
	public static boolean isChangeMasterNode(NodeInfoProtocol anotherMasterNodeInfo, UUID masterNode, DeviceInfoManager deviceInfoManager)
	{
		NodeDetectionService.nodeDetectionLogger.log(Level.WARNING, String.format("마스터 노드 겹침 확인(%s <=> %s)", masterNode.toString(), anotherMasterNodeInfo.getMasterNode().toString()));
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
