package node.network.packet.encpacket;

import java.security.PrivateKey;

import node.security.RSAEncrypt;

public class EncPacketConverter
{
	public static final byte[] MAGIC_NO_PART = new byte[] {0x50, 0x41, 0x52, 0x54};	//PART
	
	public static final int MAGIC_PART_NUM_PLACE = MAGIC_NO_PART.length; //idx = 4;
	public static final int PAYLOAD_SIZE = 117;
	/*(RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE / 8) - ENCRYPT_HEAD_SIZE + (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE % 8) == 0 ? 0 : 1;         //128 - 11*/
	
	public static byte[] packetAssembler(byte[][] slicedPacket, PrivateKey privateKey)
	{
		if(isPart(slicedPacket[0], privateKey) == 0);
		
		
		
		
		
		return new byte[0];
	}
	
	//0 = not part, 1 ~ 255 need Assemble
	public static int isPart(byte[] firstPacket, PrivateKey privateKey)
	{
		try
		{
			byte[] arr = RSAEncrypt.decode(firstPacket, privateKey);
			
			for(int i = 0; i < MAGIC_NO_PART.length; ++i)
			{
				if(arr[i] != MAGIC_NO_PART[i])
					return 0;
			}
			
			return arr[MAGIC_PART_NUM_PLACE];			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return 0;
	}
}