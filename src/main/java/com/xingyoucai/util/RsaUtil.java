package com.xingyoucai.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public class RsaUtil {

	public static final String KEY_ALGORITHM = "RSA";  
      
    public static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";  

    private static final int MAX_ENCRYPT_BLOCK = 117;  
      
    private static final int MAX_DECRYPT_BLOCK = 128; 
    
	public static final String PRIVATE_KEY_FILE = "E:/rsa/pkcs8_priv.pem";

	public static final String PUBLIC_KEY_FILE = "E:/rsa/public.key";

	public static String publicKey = "";
	
	public static String privateKey = "";
	
	static{
		try {
			BufferedReader publicKeyReader = new BufferedReader(new InputStreamReader(RsaUtil.class.getResourceAsStream("/keys/public.key")));
			String strPublicKey = "";
			String publicLine = "";
			while ((publicLine = publicKeyReader.readLine()) != null) {
				strPublicKey += publicLine;
			}
			publicKeyReader.close();
			publicKey = strPublicKey.replace("-----BEGIN PUBLIC KEY-----", "")
					.replace("-----END PUBLIC KEY-----", "");
			
			BufferedReader privateKeyReader = new BufferedReader(new InputStreamReader(RsaUtil.class.getResourceAsStream("/keys/pkcs8_priv.pem")));
			String strPrivateKey = "";
			String privateLine = "";
			while ((privateLine = privateKeyReader.readLine()) != null) {
				strPrivateKey += privateLine;
			}
			privateKeyReader.close();
			// 私钥需要使用pkcs8格式的，公钥使用x509格式的
			privateKey = strPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "")
					.replace("-----END PRIVATE KEY-----", "");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static byte[] encrypt(String data) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicK = keyFactory.generatePublic(x509KeySpec);  
        // 对数据加密  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.ENCRYPT_MODE, publicK);  
        int inputLen = data.getBytes().length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;  
        // 对数据分段加密  
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {  
                cache = cipher.doFinal(data.getBytes(), offSet, MAX_ENCRYPT_BLOCK);  
            } else {  
                cache = cipher.doFinal(data.getBytes(), offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * MAX_ENCRYPT_BLOCK;  
        }  
        byte[] encryptedData = out.toByteArray();  
        out.close();  
        return encryptedData;  
	}
	
	public static String decrypt(byte[] encryptedData) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.DECRYPT_MODE, privateK);  
        int inputLen = encryptedData.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;  
        // 对数据分段解密  
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {  
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);  
            } else {  
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * MAX_DECRYPT_BLOCK;  
        }  
        byte[] decryptedData = out.toByteArray();  
        out.close();  
        return new String(decryptedData); 
	}
	
	public static String sign(byte[] data) throws Exception {
    	
        byte[] keyBytes = Base64.decodeBase64(privateKey);
        
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);  
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);  
        signature.initSign(privateK);  
        signature.update(data);  
        return Base64.encodeBase64String(signature.sign());
    } 
	
	public static boolean verify(byte[] data,String sign)  
            throws Exception {  
        byte[] keyBytes = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        PublicKey publicK = keyFactory.generatePublic(keySpec);  
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);  
        signature.initVerify(publicK);  
        signature.update(data);  
        return signature.verify(Base64.decodeBase64(sign));  
    }
	
	public static void main(String a[]) {
		try {
			String source = "channelId=2&method=getLoanDetailInfo&params="+
					 "{\"loanDate\":\"2016-12-09\",\"commissions\":60,\"loanAmount\":2000,\"balance\":2080.53,"+
					 "\"refunds\":[{\"periodNumber\":1,\"dueDate\":\"20170108\",\"dueAmount\":693.51,\"status\":3},"+
					 "{\"periodNumber\":2,\"dueDate\":\"20170207\",\"dueAmount\":693.51,\"status\":3},"+
					 "{\"periodNumber\":3,\"dueDate\":\"20170309\",\"dueAmount\":693.51,\"status\":3}]}&signType=RSA&ver=1.0";
      byte[] encodedData = RsaUtil.encrypt(source);
      System.out.println("加密后文字：\r\n" + Base64.encodeBase64String(encodedData));
      String decodedData = RsaUtil.decrypt(encodedData);
      System.out.println("解密后文字: \r\n" + decodedData);
      
      System.err.println("私钥签名——公钥验证签名");
      String sign = RsaUtil.sign(source.getBytes());
      System.err.println("签名:\r" + sign);
      boolean status = RsaUtil.verify(source.getBytes(), sign);
      
      System.err.println("验证结果:\r" + status);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
