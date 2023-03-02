/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.swingreverse;

import org.apache.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

public final class TestePBE {
	private static SecretKey skey;
	private static final String algorithm = "Blowfish";
	final static Logger log = Logger.getLogger(TestePBE.class);
        
        
	public static final void encrypt(String password, File arq)
		throws IOException,
            BadPaddingException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            InvalidKeyException,
            NoSuchAlgorithmException,
            InvalidKeySpecException {
                  MessageDigest digester = MessageDigest.getInstance("SHA-256");
                    digester.update(password.getBytes("UTF-8"));
                    byte[] key = digester.digest();
                    SecretKeySpec spec = new SecretKeySpec(key, "AES");
            
        	SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
		KeySpec ks = new PBEKeySpec(password.toCharArray(), new byte[]{1,2,3,4,5,6,7,8,9,10}, 1000);
		SecretKey skey = skf.generateSecret (ks);
		final Cipher cipher = Cipher.getInstance(algorithm);
		//cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		//cipher.doFinal(text.getBytes());
	}
        
	public static final void decrypt(String password, File arq)
		throws IOException,
            BadPaddingException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            InvalidKeyException,
            NoSuchAlgorithmException,
            InvalidKeySpecException {
		SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
		KeySpec ks = new PBEKeySpec(password.toCharArray(), new byte[]{1,2,3,4,5,6,7,8,9,10}, 1000);
		SecretKey skey = skf.generateSecret (ks);
		final Cipher cipher = Cipher.getInstance(algorithm);
		//cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		//cipher.doFinal(text.getBytes());
	}
        
	private static int tamArquivoTeste = 12345;
	
        private static void geraArquivoTeste(File arq) throws IOException {
	    Random r = new Random();
	    r.setSeed (123456789);
	    byte[] bytes = new byte[tamArquivoTeste];
	//    r.next (bytes);
	    OutputStream os = new FileOutputStream(arq);
	    os.write (bytes);
	    os.close();
        }
        
	private static boolean confereArquivoTeste(File arq) throws IOException {
	    boolean ret = true;
	    Random r = new Random();
	    r.setSeed (123456789);
	    byte[] bytes = new byte[tamArquivoTeste];
	   // r.next(bytes);
	    byte[] readBytes = new byte[tamArquivoTeste];
	    if (arq.length() != tamArquivoTeste) {
	       log.trace ("Arquivo de tamanho diferente do esperado");
	        return false;
            }
            
	    InputStream is = new FileInputStream(arq);
	    int nBytes = is.read (readBytes);
	    is.close();
	    if (nBytes != tamArquivoTeste) {
	       log.trace ("Arquivo de tamanho diferente do esperado");
	        return false;
            }
            return Arrays.equals (readBytes, bytes);
        }
        
        
	public static void main(String[] args) throws Exception {
            String password = "senha altamente secreta";
            File arqEntrada = new File("teste.bin");
            File arqCifrado = new File("teste.bin.cifrado");
            File arqDecifrado = new File("teste.bin.decifrado");
            //geraArquivoTeste(arqTeste);		
           // TestePBE.encrypt (password, arqEntrada, arqCifrado);
              TestePBE.encrypt (password, arqEntrada);
            //-- Checando com a senha certa
            //TestePBE.decrypt (password, arqCifrado, arqDecifrado);
             TestePBE.decrypt (password, arqDecifrado);
            if (confereArquivoTeste (arqEntrada)) {
               log.trace ("Os arquivos bateram");
            }
            //-- Checando com a senha errada
            TestePBE.decrypt ("senhaErrada", arqEntrada);
            if (confereArquivoTeste (arqDecifrado)) {
               log.trace ("Os arquivos bateram");
            }
	}
}