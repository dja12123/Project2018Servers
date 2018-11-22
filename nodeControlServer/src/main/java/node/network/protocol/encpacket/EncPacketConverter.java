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
				int byteSize = 0; 
				byte[][] bufferArr = new byte[targetPacket.partCount][];
				bufferArr[0] = deleteHead(firstLine);
				byteSize += bufferArr[0].length;
				
				for(int i = 1; i < targetPacket.partCount; ++i)
				{
					bufferArr[i] = deleteHead(RSAEncrypt.decode(targetPacket.payLoad[i], privateKey));
					byteSize += bufferArr[i].length;
				}
				
				ByteBuffer buffer = ByteBuffer.allocate(byteSize);
				
				
				for(byte[] value : bufferArr)
					buffer.put(value);
					
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