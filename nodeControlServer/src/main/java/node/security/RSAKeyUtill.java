package node.security;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAKeyUtill 
{
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