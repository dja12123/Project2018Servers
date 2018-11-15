package node.security;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

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
    
    public static RSAKeyManager getInstance()
    {
        if(instance == null)
            instance = new RSAKeyManager();
            
        return instance;
    }
    
    public Key getPublicKey() { return publicKey; }

    public Key getPrivateKey() { return privateKey; }
    
    public String getPublicB64()
    {
        if(b64PublicKey == null)
            b64PublicKey = RSAKeyUtill.convertKeyToB64(publicKey);
        
        return b64PublicKey;
    }
    
    public String getPrivateB64()
    {
        if(b64PrivateKey == null)
            b64PrivateKey = RSAKeyUtill.convertKeyToB64(privateKey);
        
        return b64PrivateKey;
    }
}