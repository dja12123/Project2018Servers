package node.network.packet;

import node.security.RSAEncrypt;

public class PacketManager
{
    //[{start/center/fin}][{num}].....{PAYLOAD_SIZE}...........
    public static final int TAG_SIZE = 2;
    public static final int PAYLOAD_SIZE = (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE / 8) + (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE % 8) == 0 ? 0 : 1 - TAG_SIZE;
    
    public static byte[][] sculptPacket(byte[] target)
    {
        int targetLength = ;
        
        int partConut = (target.length / (PAYLOAD_SIZE + TAG_SIZE)) + (target.length % (PAYLOAD_SIZE + TAG_SIZE) == 0 ? 0 : 1);
        
        
        
        return new byte[0][0];
    }
    
}

/*
public class PacketManager 
{
	public static final byte[] MAGIC_NO_PART = new byte[] {0x50, 0x41, 0x52, 0x54};	//PART
	
	private static final int ENCRYPT_HEAD_SIZE = 11;
	public static final int PAYLOAD_SIZE = (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE / 8) - ENCRYPT_HEAD_SIZE + (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE % 8) == 0 ? 0 : 1; //128 - 11
	
	public static byte[][] packetSlicer(byte[] rawPacket)
	{
		if(rawPacket.length <= PAYLOAD_SIZE)
		{
			byte[][] temp = new byte[1][];
			temp[0] = rawPacket;
		}
			
		
		return new byte[0][0];
	}
	
	public static byte[] packetAssembler(byte[][] slicedPacket)
	{
		return new byte[0];
	}
}
*/


/*
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;

public class PacketManager 
{
	public static final byte[] MAGIC_NO_PART = new byte[] {0x50, 0x41, 0x52, 0x54};	//PART
	
	public static final int MAGIC_PART_NUM_PLACE = MAGIC_NO_PART.length; //idx = 4;
	private static final int PART_HEAD_SIZE = 5;
	public static final int PAYLOAD_SIZE = 117;
	/*(RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE / 8) - ENCRYPT_HEAD_SIZE + (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE % 8) == 0 ? 0 : 1;         //128 - 11*/
	
	public static byte[][] packetSlicer(byte[] rawPacket, PublicKey key)
	{
		if(rawPacket.length <= PAYLOAD_SIZE)
		{
			try 
			{
				byte[][] arr = new byte[1][];
				arr[0] = RSAEncrypt.incode(rawPacket, key);
				
				return arr;
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			return null;
		}
		else
		{
			int partCount = (rawPacket.length + MAGIC_NO_PART.length) / PAYLOAD_SIZE;
			
			if(rawPacket.length - (PAYLOAD_SIZE * partCount) > 0)
				++partCount;
			
			byte[][] arr = new byte[partCount][];
			
			ByteBuffer buff = ByteBuffer.wrap(MAGIC_NO_PART);
			buff.putInt(partCount);
			buff.put(rawPacket, 0, PAYLOAD_SIZE - PART_HEAD_SIZE);
			
			try 
			{
				arr[0] = RSAEncrypt.incode(convertByteBufferToByteArr(buff), key);
				
				int lastCopiedIndex = PAYLOAD_SIZE - PART_HEAD_SIZE;
				for(int i = 1; i < partCount; ++i)
				{
					buff = ByteBuffer.wrap(rawPacket, lastCopiedIndex, PAYLOAD_SIZE);
					lastCopiedIndex += PAYLOAD_SIZE;
					
					arr[i] = RSAEncrypt.incode(convertByteBufferToByteArr(buff), key);
				}
				
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			return arr;
		}
	}
	
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
	
	private static byte[] convertByteBufferToByteArr(ByteBuffer buffer)
	{
		byte[] b = new byte[buffer.remaining()];
		buffer.get(b);
		return b;
	}
}





*/