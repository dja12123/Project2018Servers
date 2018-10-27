package node.security;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAKeyManager
{
    private static RSAKeyManager instance;
    
    private PublicKey publicKey;
    private PrivateKey privateKey;
    
    private String b64PublicKey;
    private String b64PrivateKey;
    
    private RSAKeyManager() 
    {
    	try
    	{
    		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(RSAEncrypt.RSA_ENCRYPT_BYTE_SIZE);
            KeyPair keyPair = keyPairGenerator.genKeyPair();
            
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
		}
    }
    
    public Key getPublicKey()
    {
        return publicKey;
    }

    public Key getPrivateKey()
    {
        return privateKey;
    }
    
    public String getPublicB64()
    {
        if(b64PublicKey == null)
            b64PublicKey = convertKeyToB64(publicKey);
        
        return b64PublicKey;
    }
    
    public String getPrivateB64()
    {
        if(b64PrivateKey == null)
            b64PrivateKey = convertKeyToB64(privateKey);
        
        return b64PrivateKey;
    }
    
    public static RSAKeyManager getInstance()
    {
        if(instance == null)
            instance = new RSAKeyManager();
            
        return instance;
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