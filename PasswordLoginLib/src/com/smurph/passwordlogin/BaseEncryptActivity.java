package com.smurph.passwordlogin;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.app.Activity;
import android.content.ContentResolver;

import com.smurph.passwordhasher.Base64Coder;
import com.smurph.passwordhasher.exceptions.Base64DecodingException;

public abstract class BaseEncryptActivity extends Activity {

	/**
	 * @param cr
	 * @param key
	 */
	public abstract boolean encryptSecret(ContentResolver cr, SecretKey key);
	
	/**
	 * @param key
	 */
	public abstract boolean decryptSecret(ContentResolver cr, SecretKey key);
	
	/**
	 * @param key
	 * @param plaintext
	 * @return
	 */
	protected String encrypt(SecretKey key, String plaintext) {
		try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            
            byte[] iv = generateIv(cipher.getBlockSize());
            
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            
            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

            return Base64Coder.encodeWebSafe(iv) + ":" + Base64Coder.encodeWebSafe(cipherText);
        } catch (GeneralSecurityException e) {
        	e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
        }
		return "";
	}
	
	/**
	 * @param key
	 * @param cipherText
	 * @return
	 */
	protected String decrypt(SecretKey key, String cipherText) {
		String[] cipherData = cipherText.split(":");
		
    	try {
    		byte[] iv = Base64Coder.decodeWebSafe(cipherData[0]);
    		
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");

            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            
            byte[] byteText = cipher.doFinal(Base64Coder.decodeWebSafe(cipherData[1]));
    	
	    	return new String(byteText, "UTF-8");
        } catch (GeneralSecurityException e) {
        	e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
        } catch (Base64DecodingException e) {
        	e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 
	 * @param length
	 * @return
	 */
	private byte[] generateIv(int length) {
		try {
			byte[] b = new byte[length];
			SecureRandom.getInstance("SHA1PRNG").nextBytes(b);
			return b;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** The Algorithm used for encrypting and decrypting */
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
}
