package node.network.encpacket;

import java.nio.ByteBuffer;
import java.security.Key;
import java.util.Arrays;

import node.security.RSAEncrypt;

public class EncPacketConverter
{
	public static byte[] convertEncPacket(EncPacket targetPacket, Key privateKey)
	{
		try
		{
			byte[] firstLine = RSAEncrypt.decode(targetPacket.payLoad[0], privateKey);
			
			if(firstLine[0] == EncPacketUtil.MAGIC_NO_PART[0] && firstLine[1] == EncPacketUtil.MAGIC_NO_PART[1]) //분할일때
			{
				ByteBuffer buffer = ByteBuffer.wrap(deleteHead(firstLine));
				
				for(int i = 1; i < targetPacket.partCount; ++i)
					buffer.put(deleteHead(RSAEncrypt.decode(targetPacket.payLoad[i], privateKey)));
				
				return EncPacketUtil.convertByteBufferToByteArr(buffer);
			}
			else	//분할이 아닐때
			{
				return firstLine;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			
			return null;
		}
	}
	
	private static byte[] deleteHead(byte[] rawPacket)
	{
		return Arrays.copyOfRange(rawPacket, EncPacketUtil.MAGIC_NO_PART.length, rawPacket.length);
	}
}