package node.network.packet.encpacket;

import java.security.PublicKey;
import java.util.Arrays;

import node.security.RSAEncrypt;

public class EncPacketBuilder 
{
	public static final int PAYLOAD_SIZE = 117;
	//(RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE / 8) - ENCRYPT_HEAD_SIZE + (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE % 8) == 0 ? 0 : 1; 
	//128 - 11*/
	
	public static EncPacket encPacketBuild(byte[] rawPacket, PublicKey publicKey)
	{
		if(rawPacket.length <= PAYLOAD_SIZE)	//패킷 분할이 필요없을경우.
		{
			try 
			{
				byte[][] arr = new byte[1][];
				arr[0] = RSAEncrypt.incode(rawPacket, publicKey);
				
				return new EncPacket(arr, false, 1);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				return null;
			}
		}
		else									//패킷 분할이 필요할경우.
		{
			int partCount = rawPacket.length / PAYLOAD_SIZE;
			byte[][] arr = new byte[partCount][];
				
			try 
			{
				for(int i = 0; i < partCount; ++i)
				{
					int copyStartIdx = i * PAYLOAD_SIZE;
					int copyEndIdx = (i + 1) * PAYLOAD_SIZE >= rawPacket.length ? rawPacket.length : (i + 1) * PAYLOAD_SIZE;
					
					byte[] tempArr = Arrays.copyOfRange(rawPacket, copyStartIdx, copyEndIdx);
					
					arr[i] = RSAEncrypt.incode(tempArr, publicKey);
				}
				
				return new EncPacket(arr, true, partCount);
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				return null;
			}
		}
	}
}