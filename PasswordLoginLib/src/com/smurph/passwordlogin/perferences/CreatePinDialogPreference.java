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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;

import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.smurph.passwordhasher.PBKDF2Hash;
import com.smurph.passwordlogin.BaseEncryptActivity;
import com.smurph.passwordlogin.KeyGen;
import com.smurph.passwordlogin.PinActivity;
import com.smurph.passwordlogin.R;

public class CreatePinDialogPreference extends BaseDialogPreference {

	public CreatePinDialogPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.dialogPreferenceStyle);
	}

	public CreatePinDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		builder.setTitle(R.string.enter_pin)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (mListener != null) {
									mListener.onCanceled();
									
									clean();
								}
							}
						})
				.setPositiveButton(null, null);
		super.onPrepareDialogBuilder(builder);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (mIsSave) {
			if (getContext() instanceof BaseEncryptActivity) {
				new AsyncTask<String, Void, Bundle>() {
					
					private ProgressDialog mPro;
					
					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						
						mPro = new ProgressDialog(getContext());
						mPro.setMessage(getContext().getString(R.string.protecting));
						mPro.show();
					}

					@Override
					protected Bundle doInBackground(String... params) {
						String pin = params[0];
						PBKDF2Hash hasher;
						try {
							// Hash pin
							hasher = new PBKDF2Hash(10000, 256);
							String hash = hasher.hashPassword(pin, hasher.generateSalt(), true);
							
							// Generate SecretKey to protect the secret
							KeyGen generator = new KeyGen(getContext(), pin, 10000, 256);
							SecretKey key = generator.generateAndSave();
							
							((BaseEncryptActivity)getContext()).encryptSecret(getContext().getContentResolver(), key);
							
							Bundle bundle = new Bundle(3);
							bundle.putString("hash", hash);
							bundle.putString("algorithm", key.getAlgorithm());
							bundle.putByteArray("encoded", key.getEncoded());
							
							return bundle;
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						} catch (InvalidKeySpecException e) {
							e.printStackTrace();
						} catch (NoSuchProviderException e) {
							e.printStackTrace();
						} finally {
							pin = null;
							hasher = null;
						}
						return null;
					}

					@Override
					protected void onPostExecute(Bundle result) {
						super.onPostExecute(result);
						if (mPro != null && mPro.isShowing())
							mPro.dismiss();
						
						
						if (result!=null && !result.isEmpty()) {
//							persistBoolean(true);
							PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
								.putString(PinActivity.KEY_HASHED_PASS, result.getString("hash"))
								.putBoolean("pref_pin", true).commit();
							
							if (mListener!=null) {
								mListener.onCompleted(mPin);
							}
						}
						
						clean();
					}
				}.execute(mPin);
			}
		} else {
			clean();
		}
		super.onDialogClosed(positiveResult);
	}

	@Override
	protected void onBindDialogView(View view) {
		mEdtOne = (EditText) view.findViewById(R.id.editText1);
		mEdtOne.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mResetting)
					return;
				
				if (mIsFirst)
					mPin += mEdtOne.getText().toString();
				else {
					mPin2 += mEdtOne.getText().toString();
				Log.d("CreatePinDialogPreference", "afterTextChanged: Pin: " + mPin);
			}
				
//				if (!mIsDeleting) {
//					mEdtOne.focusSearch(View.FOCUS_RIGHT).requestFocus();
					mEdtTwo.requestFocus();
//				}
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
				
				if (mIsFirst)
					mPin += mEdtTwo.getText().toString();
				else {
					mPin2 += mEdtTwo.getText().toString();
					Log.d("CreatePinDialogPreference", "afterTextChanged: Pin: " + mPin);
				}
				
//				if (!mIsDeleting) {
//					mEdtTwo.focusSearch(View.FOCUS_RIGHT).requestFocus();
					mEdtThree.requestFocus();
//				} else {
////					mEdtTwo.focusSearch(View.FOCUS_LEFT).requestFocus();
//					mEdtOne.requestFocus();
//				}
			}
		});
		mEdtTwo.setOnFocusChangeListener(mFocusChange);

		mEdtThree = (EditText) view.findViewById(R.id.editText3);
		mEdtThree.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mResetting)
					return;
				
				if (mIsFirst)
					mPin += mEdtThree.getText().toString();
				else {
					mPin2 += mEdtThree.getText().toString();
				Log.d("CreatePinDialogPreference", "afterTextChanged: Pin: " + mPin);
			}
				
//				if (!mIsDeleting) {
//					mEdtThree.focusSearch(View.FOCUS_RIGHT).requestFocus();
					mEdtFour.requestFocus();
//				} else {
////					mEdtThree.focusSearch(View.FOCUS_LEFT).requestFocus();
//					mEdtTwo.requestFocus();
//				}
			}
		});
		mEdtThree.setOnFocusChangeListener(mFocusChange);

		mEdtFour = (EditText) view.findViewById(R.id.editText4);
		mEdtFour.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mResetting)
					return;
				
//				if (!mIsDeleting) {
					if (mIsFirst) {
						mPin += mEdtFour.getText().toString();
						
						Log.d("CreatePinDialogPreference", "afterTextChanged: Pin: " + mPin);
						
						if (mPin.length()==4) {
							getDialog().setTitle(R.string.reenter_pin);
							
							mIsFirst = false;
							
							resetEditText();
						}
					} else {
						mPin2 += mEdtFour.getText().toString();
						verifyPin();
					}
//				} else {
////					mEdtFour.focusSearch(View.FOCUS_LEFT).requestFocus();
//					mEdtThree.requestFocus();
//				}
			}
			
		});
		mEdtFour.setOnFocusChangeListener(mFocusChange);
		super.onBindDialogView(view);
	}
	
	/**
	 * 
	 */
	@Override
	protected void verifyPin() {
		if (mPin.equals(mPin2)) {			
			mIsSave = true;
			
			Log.i("PinDialogPerference", "verifyPin: Pin Mathced!");
			getDialog().dismiss();
		} else {
			getDialog().setTitle(R.string.enter_pin);
			
			mIsFirst = true;
			mPin2 = "";
			mPin = "";
			
			resetEditText();
		}
	}
}
