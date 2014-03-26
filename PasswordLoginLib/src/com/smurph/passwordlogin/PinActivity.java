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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smurph.passwordhasher.PBKDF2Hash;
import com.smurph.passwordhasher.exceptions.Base64DecodingException;

public abstract class PinActivity extends Activity implements OnClickListener {
	
	/** Action for the intent calling this {@link Activity} to create a pin */
	public static final String ACTION_CREATE_PIN = "com.smurph.passwordlogin.action.create.pin";
	
	/** Action for the intent calling this {@link Activity} to remove the pin*/
	public static final String ACTION_REMOVE_PIN = "com.smurph.passwordlogin.action.remove.pin";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (!pref.contains(KEY_HASHED_PASS) && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
			onLoadBypass();
			this.finish();
		} else if (!pref.contains(KEY_HASHED_PASS) && getIntent().getAction().equals(ACTION_CREATE_PIN)) {
			mIsCreateMode = true;
		} else {
			mIsCreateMode = false;
			mHashedPin = pref.getString(KEY_HASHED_PASS, null);
		}
		
		setContentView(R.layout.activity_pin);
		
		init();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (mPin.length()<4) {
			if (id == R.id.key_1) {
				mPin += 1;
				pinEntered();
			} else if (id == R.id.key_2) {
				mPin += 2;
				pinEntered();
			} else if (id == R.id.key_3) {
				mPin += 3;
				pinEntered();
			} else if (id == R.id.key_4) {
				mPin += 4;
				pinEntered();
			} else if (id == R.id.key_5) {
				mPin += 5;
				pinEntered();
			} else if (id == R.id.key_6) {
				mPin += 6;
				pinEntered();
			} else if (id == R.id.key_7) {
				mPin += 7;
				pinEntered();
			} else if (id == R.id.key_8) {
				mPin += 8;
				pinEntered();
			} else if (id == R.id.key_9) {
				mPin += 9;
				pinEntered();
			} else if (id == R.id.key_0) {
				mPin += 0;
				pinEntered();
			}
			return;
		}
		
		if (id == R.id.key_delete) {
			pinRemoved();
		} else if (id == R.id.key_return) {
			checkPin();
		} else {
			if (D) Log.i(TAG, "onClick: was not handled.");
		}
	}
	
	/**
	 * This is called when there is no pin and we should just load the next {@link Activity}
	 */
	protected abstract void onLoadBypass();
	
	/**
	 * @param cr
	 * @param key
	 */
//	protected abstract void encryptSecret(ContentResolver cr, SecretKey key);
	
	/**
	 * @param key
	 */
//	protected abstract void decryptSecret(SecretKey key);
	
	/**
	 * 
	 * @param isVerified
	 */
	protected void pinVerified(boolean isVerified) {
		if (D) Log.i(TAG, "pinVerified: " + isVerified);
		resetPin();
	}
	
	/**
	 * 
	 * @param isSaved
	 */
	protected void pinSaved(boolean isSaved) {
		if (D) Log.i(TAG, "pinSaved: " + isSaved);
	}
	
	/**
	 * 
	 */
	protected void deletePin() {
		PreferenceManager.getDefaultSharedPreferences(PinActivity.this).edit()
			.remove(KEY_HASHED_PASS).commit();
	}
	
	/**
	 * 
	 * @return
	 */
	private void checkPin() {
		if (mPin.length()!=4) {
			Log.i(TAG, "checkPin: Pin must be 4 digits long.");
			return;
		}
		
//		if (mIsCreateMode) {
//			if (mIsFirstPin) {
//				mTxtInfo.setText("Please enter pin again to confirm.");
//				mTxtInfo.setVisibility(View.VISIBLE);
//				
//				mPinFirst = mPin;
//
//				resetPin();
//				
//				mIsFirstPin = false;
//				return;
//			}
//			
//			if (mPinFirst.equals(mPin)) {
//				new AsyncTask<String, Void, String>() {
//					@Override
//					protected void onPreExecute() {
//						super.onPreExecute();							
//						if (mProgress!=null)
//							mProgress.setVisibility(View.VISIBLE);
//					}
//
//					@Override
//					protected String doInBackground(String... params) {
//						String pin = params[0];
//						PBKDF2Hash hasher;
//						try {
//							// Hash pin
//							hasher = new PBKDF2Hash(10000, 256);
//							String hash = hasher.hashPassword(pin, hasher.generateSalt(), true);
//							
//							// Generate SecretKey to protect the secret
//							KeyGen generator = new KeyGen(PinActivity.this, pin, 10000, 256);
//							SecretKey key = generator.generateAndSave();
//							
//							encryptSecret(PinActivity.this.getContentResolver(), key);
//							
//							return hash;
//						} catch (NoSuchAlgorithmException e) {
//							e.printStackTrace();
//						} catch (InvalidKeySpecException e) {
//							e.printStackTrace();
//						} catch (NoSuchProviderException e) {
//							e.printStackTrace();
//						} finally {
//							pin = null;
//							hasher = null;
//						}
//						return null;
//					}
//
//					@Override
//					protected void onPostExecute(String result) {
//						super.onPostExecute(result);							
//						if (mProgress!=null && mProgress.getVisibility()==View.VISIBLE)
//							mProgress.setVisibility(View.INVISIBLE);
//						
//						if (!result.isEmpty()) {
//							PreferenceManager.getDefaultSharedPreferences(PinActivity.this).edit()
//								.putString(KEY_HASHED_PASS, result).commit();
//							pinSaved(true);
//						} else {
//							pinSaved(false);
//						}
//					
//					}
//				}.execute(mPin);
//			}
//			return;
//		} else {
			new AsyncTask<String, Void, Boolean>() {
				@Override
				protected void onPreExecute() {
					super.onPreExecute();							
					if (mProgress!=null)
						mProgress.setVisibility(View.VISIBLE);
				}

				@Override
				protected Boolean doInBackground(String... params) {
					
					PBKDF2Hash hasher;
					try {
						
						hasher = new PBKDF2Hash(10000, 256);						
						return hasher.verifyPassword(mPin, params[1], params[0]);
						
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch (InvalidKeySpecException e) {
						e.printStackTrace();
					} catch (NoSuchProviderException e) {
						e.printStackTrace();
					} catch (Base64DecodingException e) {
						e.printStackTrace();
					} finally {
						hasher = null;
					}					
					return false;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					super.onPostExecute(result);							
					if (mProgress!=null && mProgress.getVisibility()==View.VISIBLE)
						mProgress.setVisibility(View.INVISIBLE);
					
					if (!result) {
						mFailCount++;
						mTxtInfo.setText(R.string.wrong_pin);
						mTxtInfo.setVisibility(View.VISIBLE);
						mTxtInfo.postDelayed(new Runnable() {
							@Override
							public void run() {
								mTxtInfo.setVisibility(View.INVISIBLE);
							}
						}, 1500);
					}
					
					pinVerified(result);
				}
			}.execute(mHashedPin.split(":"));
//		}
	}
	
	/**
	 * 
	 */
	private void resetPin() {
		for (int i=mPinIndex;i>=0;i--) {
			pinRemoved();
		}
		mPin = "";
	}
	
	/**
	 * 
	 */
	private void pinEntered() {
		switch (mPinIndex) {
		case 0:
			mCircleOne.setEntered(true);
			break;
		case 1:
			mCircleTwo.setEntered(true);
			break;
		case 2:
			mCircleThree.setEntered(true);
			break;
		case 3:
			mCircleFour.setEntered(true);
			break;

		default:
			break;
		}
		if (mPinIndex<4)
			mPinIndex++;
	}
	
	/**
	 * 
	 */
	private void pinRemoved() {
		if (mPinIndex>0)
			mPinIndex--;
		
		switch (mPinIndex) {
		case 0:
			mCircleOne.setEntered(false);
			break;
		case 1:
			mCircleTwo.setEntered(false);
			break;
		case 2:
			mCircleThree.setEntered(false);
			break;
		case 3:
			mCircleFour.setEntered(false);
			break;

		default:
			break;
		}
		
		if (mPin.length()>0)
			mPin = mPin.substring(0, mPin.length()-1);
	}
	
	/**
	 * 
	 * @param index must be 0, 1, 2, 3. Use -1 to set all fill colors the same.
	 * @param color The color that you want to fill the circle.
	 */
	protected void setCustomFillColors(int index, int color) {
		if (index>3) {
			Log.e(TAG, "Index must be 0, 1, 2, 3. Use -1 to set all fill colors the same.");
		}
		
		if (index==-1) {
			mCircleOne.setCustomFillColor(color);
			mCircleTwo.setCustomFillColor(color);
			mCircleThree.setCustomFillColor(color);
			mCircleFour.setCustomFillColor(color);
			return;
		}
		
		switch (index) {
		case 0:
			mCircleOne.setCustomFillColor(color);
			break;
		case 1:
			mCircleTwo.setCustomFillColor(color);
			break;
		case 2:
			mCircleThree.setCustomFillColor(color);
			break;
		case 3:
			mCircleFour.setCustomFillColor(color);
			break;

		default:
			break;
		}
	}
	
	/**
	 * 
	 */
	private void init() {
		mProgress = (ProgressBar) findViewById(R.id.proHash);
		
		mTxtInfo = (TextView) findViewById(R.id.txtWrongPin);
		
		mCircleOne = (CircleView) findViewById(R.id.circle_one);
		mCircleTwo = (CircleView) findViewById(R.id.circle_two);
		mCircleThree = (CircleView) findViewById(R.id.circle_three);
		mCircleFour = (CircleView) findViewById(R.id.circle_four);
		
		findViewById(R.id.key_0).setOnClickListener(this);
		findViewById(R.id.key_1).setOnClickListener(this);
		findViewById(R.id.key_2).setOnClickListener(this);
		findViewById(R.id.key_3).setOnClickListener(this);
		findViewById(R.id.key_4).setOnClickListener(this);
		findViewById(R.id.key_5).setOnClickListener(this);
		findViewById(R.id.key_6).setOnClickListener(this);
		findViewById(R.id.key_7).setOnClickListener(this);
		findViewById(R.id.key_8).setOnClickListener(this);
		findViewById(R.id.key_9).setOnClickListener(this);
		findViewById(R.id.key_delete).setOnClickListener(this);
		findViewById(R.id.key_delete).setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View v) {				
				if (mPinIndex>0) {
					resetPin();
				}				
				return true;
			}
		});
		findViewById(R.id.key_return).setOnClickListener(this);
	}
	
	/**
	 * @return The number of times the user has entered the incorrect pin.
	 */
	public int getFailCount() {
		return mFailCount;
	}
	
	/**
	 * @return The current pin.
	 */
	protected String getPin() {
		return mPin;
	}
	
	/**  */
	private boolean mIsFirstPin = true;

	/**  */
	public static final String KEY_HASHED_PASS = "hashed_pass";
	
	/**  */
	private boolean mIsCreateMode = true;
	
	/**  */
	private ProgressBar mProgress;
	
	/**  */
	private TextView mTxtInfo;
	
	/**  */
	private String mPin = "";
	
	/**  */
	private String mPinFirst = "";
	
	/**  */
	private String mHashedPin;
	
	/**  */
	private CircleView mCircleOne;

	/**  */
	private CircleView mCircleTwo;

	/**  */
	private CircleView mCircleThree;

	/**  */
	private CircleView mCircleFour;
	
	/**  */
	private int mFailCount = 0;
	
	/**  */
	private int mPinIndex = 0;

	/**  */
	private final String TAG = PinActivity.class.getSimpleName();
	
	/**  */
	private final boolean D = BuildConfig.DEBUG;
}
