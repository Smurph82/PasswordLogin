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

package com.smurph.passwordlogindemo;

import javax.crypto.SecretKey;

import android.content.ContentResolver;
import android.view.Menu;

import com.smurph.passwordlogin.PinActivity;
import com.smurph.passwordlogin.RandomColor;

public class MainActivity extends PinActivity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		// Set custom fill color for all circles
		setCustomFillColors(-1, RandomColor.getRandomColor());
		
		// Set each circle to have a different fill color
//		setCustomFillColors(0, RandomColor.getRandomColor());
//		setCustomFillColors(1, RandomColor.getRandomColor());
//		setCustomFillColors(2, RandomColor.getRandomColor());
//		setCustomFillColors(3, RandomColor.getRandomColor());
		
		return true;
	}

	@Override
	protected void onLoadBypass() {
	}

	@Override
	protected void encryptSecret(ContentResolver cr, SecretKey key) {
	}

	@Override
	protected void decryptSecret(SecretKey key) {
	}

}
