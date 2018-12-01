package node.network.protocol.encpacket;

import java.nio.ByteBuffer;

import node.security.RSAEncrypt;

public class EncPacketUtil 
{
	//0, 1 = MagicNum(PT)
	public static final int PACKET_SIZE = (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE / 8) - (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE % 8) == 0 ? 0 : 1;
	public static final byte[] MAGIC_NO_PART = new byte[] {0x50,0x54};	//PT
	public static final int PAYLOAD_SIZE = PACKET_SIZE - MAGIC_NO_PART.length;
	//(RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE / 8) - ENCRYPT_HEAD_SIZE + (RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE % 8) == 0 ? 0 : 1; 
	//128 - 11*/
	
	public static byte[] convertByteBufferToByteArr(ByteBuffer buffer)
	{
		buffer.position(0);
		byte[] arr = new byte[buffer.remaining()];
		buffer.get(arr);
		return arr;
	}
}
