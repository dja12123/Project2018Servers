package node.network.encpacket;

import java.nio.ByteBuffer;
import java.security.Key;
import java.util.Arrays;

import node.security.RSAEncrypt;
import node.security.RSAKeyManager;
import node.security.RSAKeyUtill;

public class EncPacketBuilder 
{
	public static void main(String[] args)
	{
		//System.out.println(RSAKeyManager.DEFAULT_PUBLIC_KEY.toString());
		printHex(RSAKeyManager.DEFAULT_PUBLIC_KEY.getEncoded());
		
		EncPacket packet = buildEncPacket(RSAKeyManager.DEFAULT_PUBLIC_KEY.getEncoded(),RSAKeyManager.getInstance().getPublicKey());
		
		
		try
		{
			printHex(RSAEncrypt.decode(packet.payLoad[0], RSAKeyManager.getInstance().getPrivateKey()));
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		byte[] rawPacket = EncPacketConverter.convertEncPacket(packet, RSAKeyManager.getInstance().getPrivateKey());
		
		//System.out.println(RSAKeyUtill.convertArrToKey(rawPacket).toString());
	
		/*for(byte value : rawPacket)
			System.out.printf("%02x " ,value);*/
		
		return;
	}
	
	public static void printHex(byte[] target)
	{
		System.out.println("---------------------------");
		for(byte value : target)
			System.out.printf("%02x " ,value);
		
		System.out.println();
		System.out.println("---------------------------");
	}
	
	public static EncPacket buildEncPacket(byte[] rawPacket, Key publicKey)
	{
		if(rawPacket.length + EncPacketUtil.MAGIC_NO_PART.length <= EncPacketUtil.PAYLOAD_SIZE)	//패킷 분할이 필요없을경우.
		{
			try 
			{
				byte[][] arr = new byte[1][];
				ByteBuffer buffer = ByteBuffer.allocate(EncPacketUtil.MAGIC_NO_PART.length + rawPacket.length);
				buffer.put(EncPacketUtil.MAGIC_NO_PART);
				buffer.put(rawPacket);
				arr[0] = RSAEncrypt.incode(EncPacketUtil.convertByteBufferToByteArr(buffer), publicKey);
				
				return new EncPacket(arr, 1);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				return null;
			}
		}
		else									//패킷 분할이 필요할경우.
		{
			int partCount = rawPacket.length / EncPacketUtil.PAYLOAD_SIZE;
			partCount += rawPacket.length % EncPacketUtil.PAYLOAD_SIZE == 0 ? 0 : 1;
			
			byte[][] arr = new byte[partCount][];
				
			try 
			{
				for(int i = 0; i < partCount; ++i)
				{
					int copyStartIdx = i * EncPacketUtil.PAYLOAD_SIZE;
					int copyEndIdx = (i + 1) * EncPacketUtil.PAYLOAD_SIZE >= rawPacket.length ? rawPacket.length : (i + 1) * EncPacketUtil.PAYLOAD_SIZE;
					
					ByteBuffer buffer = ByteBuffer.allocate(EncPacketUtil.MAGIC_NO_PART.length + copyEndIdx - copyStartIdx);

					buffer.put(EncPacketUtil.MAGIC_NO_PART);
					buffer.put(Arrays.copyOfRange(rawPacket, copyStartIdx, copyEndIdx));
					
					arr[i] = RSAEncrypt.incode(EncPacketUtil.convertByteBufferToByteArr(buffer), publicKey);
				}
				return new EncPacket(arr, partCount);
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				return null;
			}
		}
		
	}
}