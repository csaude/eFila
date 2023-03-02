/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.swingreverse;

import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public final class SecretKeyUtil {
     
    public static String SECRET_KEY_DAT = "C:/IDART/secretkey_aes256.txt";
    public static String SECRET_KEY_TYPE = "AES";
    public static int KEYSIZE = 256;
    public static String UTF_8 = "UTF8";

    final static Logger log = Logger.getLogger(SecretKeyUtil.class);
     
    public static void main(String args[]) {
        try {
            SecretKeyUtil.generateSecretKey();
          
           log.trace("Key file created! "+ SecretKeyUtil.encrypter("!farmac@ccs.mz")+" - " +SecretKeyUtil.decrypter(SecretKeyUtil.encrypter("!farmac@ccs.mz")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
     
    public static void generateSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        KeyGenerator keyGen = KeyGenerator.getInstance(SECRET_KEY_TYPE);
        keyGen.init(KEYSIZE);
        SecretKey secretKey = keyGen.generateKey();
        byte[] secretKeyBytes = secretKey.getEncoded();
 
        //SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(SECRET_KEY_TYPE);
        //DESedeKeySpec keyspec = (DESedeKeySpec) keyfactory.getKeySpec(key, DESedeKeySpec.class);
        SecretKeySpec key = new SecretKeySpec(secretKeyBytes, SECRET_KEY_TYPE);
        //byte[] rawkey = key.getKey();
         byte[] encoded = key.getEncoded();
 
        // Write the raw key to the file
        File keyfile = new File(SECRET_KEY_DAT);
        FileOutputStream fos = new FileOutputStream(keyfile);
        fos.write(encoded);
        fos.close();
    }
     
    public static SecretKey getSecretkey() throws NoSuchAlgorithmException,
            InvalidKeySpecException, IOException, InvalidKeyException {
         
        boolean exists = new File(SECRET_KEY_DAT).exists();
        if (!exists) {
            SecretKeyUtil.generateSecretKey();
        }
        return SecretKeyUtil.readSecretKey();
    }
 
//    public static SecretKey readSecretKey() throws IOException, NoSuchAlgorithmException, InvalidKeyException,
//            InvalidKeySpecException {
//        // Read the raw bytes from the keyfile
//        File keyfile = new File(SECRET_KEY_DAT);
//        DataInputStream input = new DataInputStream(new FileInputStream(keyfile));
//        byte[] rawkey = new byte[(int) keyfile.length()];
//        input.readFully(rawkey);
//        input.close();
// 
//        // Convert the raw bytes to a secret key like this
//        SecretKeySpec keyspec = new SecretKeySpec(rawkey,SECRET_KEY_TYPE);
//        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(SECRET_KEY_TYPE);
//        SecretKey key = keyfactory.generateSecret(keyspec);
//        return key;
//    }
    
        
public static SecretKey readSecretKey() throws IOException, NoSuchAlgorithmException, InvalidKeyException,
        InvalidKeySpecException {
       // Read the raw bytes from the keyfile  
       File keyfile = new File(SECRET_KEY_DAT);
       DataInputStream input = new DataInputStream(new FileInputStream(keyfile));
       byte[] rawkey = new byte[(int) keyfile.length()];  
       input.readFully(rawkey);  
       input.close();  
  
       // Convert the raw bytes to a secret key like this  
       SecretKeySpec keyspec = new SecretKeySpec(rawkey,SECRET_KEY_TYPE);
       return keyspec;  
   }
    
    public static String encrypter(String value) {
    try {
        SecretKey secretKey = SecretKeyUtil.getSecretkey();
        Cipher cipher = Cipher.getInstance(SecretKeyUtil.SECRET_KEY_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] bytes = value.getBytes();
        byte[] bytesEncrypted = cipher.doFinal(bytes);
        return new String(bytesEncrypted);
    } catch (Exception ex) {
        ex.printStackTrace();
        return value;
    }
}
    
    public static String decrypter(String value) {
    try {
        SecretKey secretKey = SecretKeyUtil.getSecretkey();
        Cipher cipher = Cipher.getInstance(SecretKeyUtil.SECRET_KEY_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] bytes = value.getBytes();
        byte[] bytesDecrypted = cipher.doFinal(bytes);
        return new String(bytesDecrypted);
    } catch (Exception ex) {
        ex.printStackTrace();
        return value;
    }
    
}
    
}
