package node.detection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;

import node.device.Device;
import node.network.packet.Packet;
import node.network.packet.PacketBuildFailureException;
import node.network.packet.PacketBuilder;
import node.network.packet.PacketUtil;

public class NodeInfoProtocol
{
	private UUID masterNode;
	private UUID[] uuids;
	private InetAddress[] addrs;
	private int size;
	
	public NodeInfoProtocol(UUID masterNode, UUID[] uuids, InetAddress[] addrs, int size)
	{
		this.masterNode = masterNode;
		this.uuids = uuids;
		this.addrs = addrs;
		this.size = size;
	}
	
	public NodeInfoProtocol(Packet masterNodePacket)
	{
		String[][] nodeInfoStr = PacketUtil.getDataArray(masterNodePacket);
		
		this.size = nodeInfoStr.length;
		this.uuids = new UUID[this.size];
		this.addrs = new InetAddress[this.size];
		
		for(int i = 0; i < this.size; ++i)
		{
			this.uuids[i] = UUID.fromString(nodeInfoStr[i][0]);
			try
			{
				if(nodeInfoStr[i][1].equals("null"))
				{
					this.addrs[i] = null;
				}
				else
				{
					this.addrs[i] = InetAddress.getByName(nodeInfoStr[i][1]);
				}
			}
			catch (UnknownHostException e)
			{
				NodeDetectionService.logger.log(Level.SEVERE, "마스터 노드로부터 전송된 IP 정보 손상", e);
			}
		}
		
		this.masterNode = masterNodePacket.getSender();
	}
	
	public UUID getMasterNode()
	{
		return this.masterNode;
	}
	
	public UUID getUUID(int i)
	{
		return this.uuids[i];
	}
	
	public InetAddress getAddr(int i)
	{
		return this.addrs[i];
	}
	
	public int getSize()
	{
		return this.size;
	}
	
	public String getPacketDataField()
	{
		StringBuffer msgBuffer = new StringBuffer();
		
		for(int i = 0; i < this.size; ++i)
		{
			msgBuffer.append(this.uuids[i]);
			msgBuffer.append(PacketUtil.DPROTO_SEP_COL);
			if(this.addrs[i] != null)
			{
				msgBuffer.append(this.addrs[i].getHostAddress());
			}
			else
			{
				msgBuffer.append("null");
			}
			msgBuffer.append(PacketUtil.DPROTO_SEP_ROW);
		}
		
		return msgBuffer.toString();
	}
	
}
