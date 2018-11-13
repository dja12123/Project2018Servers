package node.security;

import java.security.Key;
import javax.crypto.Cipher;

public class RSAEncrypt 
{
	public static int RSA_ENCRYPT_BYTE_SIZE = 1024;
	
    public static byte[] incode(byte[] target, Key publicKey) throws Exception 
    {
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] arrCipherData = cipher.doFinal(target);

        return arrCipherData;
    }
    
    public static byte[] decode(byte[] target, Key privateKey) throws Exception 
    {
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] data = cipher.doFinal(target);
            
        return data;
    }
}