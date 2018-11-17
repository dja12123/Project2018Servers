package node.security;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

import node.fileIO.FileHandler;

public class RSAKeyUtill 
{
	public static final String DEFAULT_PUBLIC_KEY_PROP = "defaultPublicKey";
	public static final String DEFAULT_PRIVATE_KEY_PROP = "defaultPrivateKey";
	
	public static void main(String[] args)
	{
		try 
		{
			Properties prop = new Properties();
			prop.load(FileHandler.getResourceAsStream("/config.properties"));
			
			String[] publicKeyStrArr = prop.getProperty(DEFAULT_PUBLIC_KEY_PROP).split(" ");
			byte[] defaultPublicKeyByte = new byte[publicKeyStrArr.length];
			
			for(int i = 0; i < publicKeyStrArr.length; ++i)
				defaultPublicKeyByte[i] = (byte)Integer.parseInt(publicKeyStrArr[i], 16);
				
			
			KeyFactory kf = KeyFactory.getInstance("RSA");
			Key publicKey = kf.generatePublic(new X509EncodedKeySpec(defaultPublicKeyByte));
			
	
			/*byte[] temp = RSAKeyManager.getInstance().getPublicKey().getEncoded();
			for(byte value : temp)
				System.out.printf("%02x ",value);
			
			System.out.println();
			System.out.println("-------------------------------------------------------");
            
			temp = RSAKeyManager.getInstance().getPrivateKey().getEncoded();
			for(byte value : temp)
				System.out.printf("%02x ",value);*/
			
            //PKCS8EncodedKeySpec();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String convertKeyToB64(Key key)
    {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    public static Key convertB64StringToKey(String B64str)
    {
    	try
    	{
    		byte[] data = Base64.getDecoder().decode((B64str.getBytes()));
       		X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
       		KeyFactory fact = KeyFactory.getInstance("RSA");
       		return fact.generatePublic(spec);
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
    		
    		return null;
		}
    }
}