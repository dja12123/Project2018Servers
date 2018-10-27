package node.security;

import java.security.Key;
import javax.crypto.Cipher;

public class RSAEncrypt 
{
	public static int RSA_ENCRYPT_BYTE_SIZE = 512;
	
    public static byte[] incode(String inputStr, Key publicKey) throws Exception 
    {
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] arrCipherData = cipher.doFinal(inputStr.getBytes());

        return arrCipherData;
    }
    
    public static String decode(byte[] arrCipherData, Key privateKey) throws Exception 
    {
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] arrData = cipher.doFinal(arrCipherData);
        String strResult = new String(arrData);

        return strResult;
    }
}