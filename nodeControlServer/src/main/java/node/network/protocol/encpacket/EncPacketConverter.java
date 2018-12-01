package node.network.protocol.encpacket;

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
                byte[] lastLine = RSAEncrypt.decode(targetPacket.payLoad[targetPacket.partCount - 1], privateKey);
                
                int totalPayloadSize = (EncPacketUtil.PAYLOAD_SIZE * (targetPacket.partCount - 1)) + (lastLine.length - EncPacketUtil.MAGIC_NO_PART.length);
                
                ByteBuffer buffer = ByteBuffer.allocate(totalPayloadSize);

				buffer.put(deleteHead(firstLine));
                
				for(int i = 1; i < targetPacket.partCount - 1; ++i)
					buffer.put(deleteHead(RSAEncrypt.decode(targetPacket.payLoad[i], privateKey)));
                
                buffer.put(deleteHead(lastLine));

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