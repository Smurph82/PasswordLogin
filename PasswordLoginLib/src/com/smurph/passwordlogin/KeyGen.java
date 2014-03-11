/**
 * Copyright (c) 2014 Benjamin Murphy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smurph.passwordlogin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class KeyGen {

	public KeyGen(Context context, String pin) {
		this(context, pin, MIN_ITERATION, MIN_KEY_LENGTH);
	}
	
	public KeyGen(Context context, String pin, int iterationCount, int keyLength) {
		this(context, pin, iterationCount, keyLength, null);
	}
	
	public KeyGen(Context context, String pin, int iterationCount, int keyLength, byte[] salt) {
		if (iterationCount<MIN_ITERATION)
			mIterationCount = MIN_ITERATION;
		else
			mIterationCount = iterationCount;
		
		if (keyLength<MIN_KEY_LENGTH)
			mKeyLength = MIN_KEY_LENGTH;
		else
			mKeyLength = keyLength;
		
		PKCS5_SALT_LENGTH = mKeyLength / 8;
		
		if (salt==null) {
			mSalt = generateSalt();
		} else {
			mSalt = salt;
		}
		
		mContext = context;
		mPin = pin;
	}
	
	/**
	 * 
	 * @param context
	 * @param pin
	 * @return
	 */
	public static SecretKey getKey(Context context, String pin) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if (pref.contains(HAS_KEYSTORE)) {
			KeyStore ks = null;
			InputStream in = null;
			try {
				ks = KeyStore.getInstance("BKS", "BC");
				
				in = context.openFileInput("PinPro.bks");
				
				ks.load(in, pin.toCharArray());
				
				KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry("PinProtect", null);
				
				return entry.getSecretKey();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (CertificateException e) {
				e.printStackTrace();
			} catch (UnrecoverableEntryException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			} finally {
				if (in!=null) {
					try {
						in.close();
						in = null;
					} catch (Exception e) { /* Don't care */ }
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeySpecException
	 */
	public SecretKey generateAndSave() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		if (mPin.isEmpty() || mContext==null)
			throw new NullPointerException("Pin and/or Context are empty");
		
        KeySpec keySpec = new PBEKeySpec(mPin.toCharArray(), mSalt, mIterationCount, mKeyLength);
        
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_PBKDF2, "BC");
        
        byte[] keybytes = keyFactory.generateSecret(keySpec).getEncoded();

        SecretKey result = new SecretKeySpec(keybytes, "AES");

        save(result);
        
        return result;
	}
	
	/**
	 * 
	 * @param key
	 */
	private void save(SecretKey key) {		
		KeyStore ks = null;
		OutputStream out = null;			
		try {
			ks = KeyStore.getInstance("BKS", "BC");
			ks.load(null, mPin.toCharArray());
			
			out = mContext.openFileOutput("PinPro.bks", Context.MODE_APPEND);
			
			ks.setEntry("PinProtect", new KeyStore.SecretKeyEntry(key), null);
			
			ks.store(out, mPin.toCharArray());
			
			PreferenceManager.getDefaultSharedPreferences(mContext).edit()
					.putBoolean(HAS_KEYSTORE, true).commit();
			
			if (D) Log.i(TAG, "save: KeyStore created.");
			
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			clean();
			if (out!=null) {
				try {
					out.flush();
					out.close();
					out = null;
				} catch (Exception e) { /* Don't care */ }
			}
		}
	}
	
	/**
	 * 
	 */
	public void clean() {
		mPin = null;
		mContext = null;
	}
	
	/**
	 * @return
	 */
	public byte[] generateSalt() {
		try {
			byte[] b = new byte[PKCS5_SALT_LENGTH];
			SecureRandom.getInstance("SHA1PRNG").nextBytes(b);
			return b;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	/**  */
	public static final String HAS_KEYSTORE = "has_keystore";
	
	/**  */
	private Context mContext;
	
	/**  */
	private String mPin;
	
	/**  */
	private final byte[] mSalt;
	
	/**  */
	private final int mIterationCount;
	
	/**  */
	private final int mKeyLength;
	
	/** The length of the salt */
	private final int PKCS5_SALT_LENGTH;
	
	/**  */
	private static final int MIN_KEY_LENGTH = 256;
	
    /** PBKDF2 encryption algorithm */
	// http://android-developers.blogspot.com/2013/12/changes-to-secretkeyfactory-api-in.html
	private static final String ALGORITHM_PBKDF2 = "PBKDF2WithHmacSHA1";
	
	/**  */
	private static final int MIN_ITERATION = 10000;
	
	/** Used with {@link Log} */
	private final String TAG = KeyGen.class.getSimpleName();

	/** Used with {@link Log} */	
	private final boolean D = BuildConfig.DEBUG;
}
