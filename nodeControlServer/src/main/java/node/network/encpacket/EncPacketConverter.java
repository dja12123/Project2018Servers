package node.network.encpacket;

import java.nio.ByteBuffer;
import java.security.Key;

import node.security.RSAEncrypt;

public class EncPacketConverter
{
	public static byte[] convertEncPacket(EncPacket targetPacket, Key privateKey)
	{
		ByteBuffer buffer = null;
		try
		{
			for(int i = 0; i < EncPacketUtil.MAGIC_NO_PART.length; ++i)

			buffer = ByteBuffer.wrap(RSAEncrypt.decode(targetPacket.payLoad[0], privateKey));
			
			for(int i = 1; i < targetPacket.partCount; ++i)
				buffer.put(RSAEncrypt.decode(targetPacket.payLoad[i], privateKey));
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			
			return null;
		}
		
		return EncPacketUtil.convertByteBufferToByteArr(buffer);
	}
}