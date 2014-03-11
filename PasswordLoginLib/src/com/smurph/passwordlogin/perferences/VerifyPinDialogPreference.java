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

package com.smurph.passwordlogin.perferences;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.smurph.passwordhasher.PBKDF2Hash;
import com.smurph.passwordhasher.exceptions.Base64DecodingException;
import com.smurph.passwordlogin.BaseEncryptActivity;
import com.smurph.passwordlogin.KeyGen;
import com.smurph.passwordlogin.PinActivity;
import com.smurph.passwordlogin.R;

public class VerifyPinDialogPreference extends BaseDialogPreference {

	public VerifyPinDialogPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.dialogPreferenceStyle);
	}
	
	public VerifyPinDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		builder.setTitle(R.string.enter_pin)
				.setNegativeButton(null, null)
				.setPositiveButton(null, null);
		super.onPrepareDialogBuilder(builder);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (mIsVerified) {
			if (getContext() instanceof BaseEncryptActivity) {
				new AsyncTask<String, Void, Boolean>() {

					private ProgressDialog mPro;
					
					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						
						mPro = new ProgressDialog(getContext());
						mPro.setMessage(getContext().getString(R.string.removing_pin_protection));
						mPro.show();
					}
					
					@Override
					protected Boolean doInBackground(String... params) {						
						SecretKey key = KeyGen.getKey(getContext(), params[0]);
						boolean result = ((BaseEncryptActivity)getContext()).decryptSecret(getContext().getContentResolver(), key);
						
						File dir = getContext().getFilesDir();
						File keyStore = new File(dir, "PinPro.bks");
						if (keyStore.exists())
							keyStore.delete();
						
						return result;
					}
					
					@Override
					protected void onPostExecute(Boolean result) {
						super.onPostExecute(result);
						if (mPro != null && mPro.isShowing())
							mPro.dismiss();
						
						if (result) {
							PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
								.putBoolean(KeyGen.HAS_KEYSTORE, false)
								.remove(PinActivity.KEY_HASHED_PASS)
								.remove("pref_pin").commit();
							
							if (mListener!=null) {
								mListener.onCompleted(null);
							}
						}
						
						clean();
					}
					
				}.execute(mPin);
			}
		}
		super.onDialogClosed(positiveResult);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		View holder = view.findViewById(R.id.btnHolder);
		holder.setVisibility(View.VISIBLE);

		holder.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onCanceled();
					
					clean();
				}
				getDialog().cancel();
			}
		});
		
		holder.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPin.length()==4) {
					verifyPin();
				} else {
					Log.e("VerifyPinDialogPreference", "onBindDialogView: Pin must be 4 characters long.");
				}
			}
		});
		
		mEdtOne = (EditText) view.findViewById(R.id.editText1);
		mEdtOne.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mResetting)
					return;
				
				mPin += mEdtOne.getText().toString();
				
				mEdtTwo.requestFocus();
			}
		});
		mEdtOne.requestFocus();
		mEdtOne.setOnFocusChangeListener(mFocusChange);
		mEdtOne.postDelayed(new Runnable() {
			@Override
			public void run() {
				((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mEdtOne, 0);
			}
		}, 200L);
		
		mEdtTwo = (EditText) view.findViewById(R.id.editText2);
		mEdtTwo.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mResetting)
					return;
				
				mPin += mEdtTwo.getText().toString();
				
				mEdtThree.requestFocus();
			}
		});
		mEdtTwo.setOnFocusChangeListener(mFocusChange);

		mEdtThree = (EditText) view.findViewById(R.id.editText3);
		mEdtThree.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mResetting)
					return;
				
				mPin += mEdtThree.getText().toString();
				
				mEdtFour.requestFocus();
			}
		});
		mEdtThree.setOnFocusChangeListener(mFocusChange);

		mEdtFour = (EditText) view.findViewById(R.id.editText4);
		mEdtFour.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mResetting)
					return;
				
					mPin += mEdtFour.getText().toString();
			}
			
		});
		mEdtFour.setOnFocusChangeListener(mFocusChange);
		super.onBindDialogView(view);
	}

	@Override
	protected void verifyPin() {
		String hashedPin = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(PinActivity.KEY_HASHED_PASS, null);
		String[] values = new String[3];
		String[] hSplit = hashedPin.split(":");
		values[0] = hSplit[0];
		values[1] = hSplit[1];
		values[2] = mPin;
		
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
					return hasher.verifyPassword(params[2], params[1], params[0]);
					
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
				}
				
				pinVerified(result);
			}
		}.execute(values);
	}

	/**
	 * @param isVerified
	 */
	private void pinVerified(boolean isVerified) {
		mIsVerified = isVerified;
		if (isVerified) {
			if (mListener!=null) {
				mListener.onVerified(mPin);
			}
			getDialog().dismiss();
		} else {
			Toast.makeText(getContext(), getContext().getString(R.string.of_5_attempts, mFailCount), Toast.LENGTH_SHORT).show();
			
			if (mFailCount==5) {
				getDialog().cancel();
			}
		}
	}

	/**  */
	private int mFailCount = 0;
	
	/**  */
	private boolean mIsVerified = false;
}
