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

import android.content.Context;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.smurph.passwordlogin.BuildConfig;
import com.smurph.passwordlogin.R;

public abstract class BaseDialogPreference extends DialogPreference {
	
	public BaseDialogPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.dialogPreferenceStyle);
	}

	public BaseDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
//		setPersistent(false);
		setDialogLayoutResource(R.layout.dialog_preference_pin);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		mProgress = (ProgressBar) view.findViewById(R.id.progressBar1);
		super.onBindDialogView(view);
	}
	
	/**
	 * 
	 */
	protected abstract void verifyPin();
	
	/**
	 * 
	 */
	protected void resetEditText() {
		mResetting = true;
		
		mEdtFour.setText("");
		mEdtThree.setText("");
		mEdtTwo.setText("");
		mEdtOne.setText("");

		mResetting = false;
		
		mEdtOne.requestFocus();
	}
	
	/**
	 * @param which
	 */
	protected void resetEditText(int which) {
		if (which==0) {
			resetEditText();
			return;
		}

		mResetting = true;
		switch (which) {
		case 1:
			mEdtTwo.setText("");
		case 2:
			mEdtThree.setText("");
		case 3:
			mEdtFour.setText("");

		default:
			break;
		}
		mResetting = false;
	}
	
	/**
	 * 
	 */
	protected OnFocusChangeListener mFocusChange = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (v.getId()==R.id.editText1 && hasFocus) {
				if (mIsFirst && mPin.length()>0) {
					mPin = "";
				} else if (!mIsFirst && mPin2.length()>0) {
					mPin2 = "";
				} else {
					return;
				}
				resetEditText(0);
				return;
			}
			
			if (v.getId()==R.id.editText2 && hasFocus) {
				if (mIsFirst && mPin.length()>1) {
					mPin = mPin.substring(0, 1);
				} else if (!mIsFirst && mPin2.length()>1) {
					mPin2 = mPin2.substring(0, 1);
				} else {
					return;
				}
				resetEditText(1);
				return;
			}
			
			if (v.getId()==R.id.editText3 && hasFocus) {
				if (mIsFirst && mPin.length()>2) {
					mPin = mPin.substring(0, 2);
				} else if (!mIsFirst && mPin2.length()>2) {
					mPin2 = mPin2.substring(0, 2);
				} else {
					return;
				}
				resetEditText(2);
				return;
			}
			
			if (v.getId()==R.id.editText4 && hasFocus) {
				if (mIsFirst && mPin.length()>3) {
					mPin = mPin.substring(0, 3);
				} else if (!mIsFirst && mPin2.length()>3) {
					mPin2 = mPin2.substring(0, 3);
				} else {
					return;
				}
				resetEditText(3);
				return;
			}
		}
	};
	
	/**
	 * Called to reset the varibles when dialog is closing
	 */
	protected void clean() {
		mIsFirst = true;
		mResetting = false;
		mPin = "";
		mPin2 = "";
	}
	
	/**
	 * 
	 * @author Ben
	 *
	 */
	protected class SimpleTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			mIsDeleting = (count == 1 && after==0);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void afterTextChanged(Editable s) { }
		
	}

	/**
	 * @param listener
	 */
	public void setOnDialogActionListener(OnDialogActionListener listener) {
		mListener = listener;
	}

	/**  */
	public interface OnDialogActionListener {
		/**  */
		void onCanceled();
		
		/**
		 * @param pin
		 */
		void onCompleted(String pin);
		/**
		 * @param pin
		 */
		void onVerified(String pin);
		
		public class SimpleOnDialogActionListener implements OnDialogActionListener {

			@Override
			public void onCanceled() {
			}

			@Override
			public void onCompleted(String pin) {
			}

			@Override
			public void onVerified(String pin) {
			}
			
		}
	}
	
	/**  */
	protected OnDialogActionListener mListener;
	
	/**  */
	protected boolean mIsFirst = true;
	
	/**  */
	protected boolean mResetting = false;
	
	/**  */
	protected boolean mIsSave = false;
	
	/**  */
	protected ProgressBar mProgress;
	
	/**  */
	protected EditText mEdtOne;

	/**  */
	protected EditText mEdtTwo;

	/**  */
	protected EditText mEdtThree;

	/**  */
	protected EditText mEdtFour;
	
	/**  */
	protected String mPin = "";
	
	/**  */
	protected String mPin2 = "";
	
	/**  */
	protected final boolean D = BuildConfig.DEBUG;
}
