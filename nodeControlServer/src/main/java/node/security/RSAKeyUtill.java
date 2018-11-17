package node.security;

import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import node.network.encpacket.EncPacketUtil;

public class RSAKeyUtill 
{
	public static final String DEFAULT_PUBLIC_KEY_PROP = "defaultPublicKey";
	public static final String DEFAULT_PRIVATE_KEY_PROP = "defaultPrivateKey";

	public static byte[] convertKeyToArr(Key key) { return key.getEncoded(); }
	
	public static byte[] convertByteStringtoByteArr(String str)
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[0]);
		
		for(int i = 0; i < str.length(); i += 2)
			buffer.put((byte)Integer.parseInt(str.substring(i, i + 1),16));
		
		return EncPacketUtil.convertByteBufferToByteArr(buffer);
	}
    
    public static Key convertArrToKey(byte[] rawByte)
    {
    	KeyFactory kf = null;
    	try 
    	{
			kf = KeyFactory.getInstance("RSA");
		} 
    	catch (NoSuchAlgorithmException e) 
    	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	try
    	{
    		//publicKey
    		return kf.generatePublic(new X509EncodedKeySpec(rawByte));
    	}
    	catch (InvalidKeySpecException e) 
    	{
    		try 
    		{
    			//privateKey
				return kf.generatePrivate(new PKCS8EncodedKeySpec(rawByte));
			}
    		catch (InvalidKeySpecException e1) 
    		{
				return null;
			}
		}
    }
}