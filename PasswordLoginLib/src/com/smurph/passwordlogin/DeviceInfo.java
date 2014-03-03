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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;

import com.smurph.passwordhasher.HexCoder;

public class DeviceInfo {

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static final String getDeviceId(Context context) {
		String id = Settings.Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		String device = Build.DEVICE;
		id = device + id;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA1");
			byte[] bytes = id.getBytes("UTF-8");
			md.update(bytes, 0, bytes.length);

			return HexCoder.toHex(md.digest());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets the hardware serial number of this device.
	 * 
	 * @return serial number or {@code null} if not available.
	 */
	private static String getDeviceSerialNumber() {
		// We're using the Reflection API because Build.SERIAL is only available
		// since API Level 9 (Gingerbread, Android 2.3).
		try {
			return (String) Build.class.getField("SERIAL").get(null);
		} catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * 
	 * @return
	 */
	public static byte[] getBuildFingerprintAndDeviceSerial() {
		StringBuilder result = new StringBuilder();
		String fingerprint = Build.FINGERPRINT;
		if (fingerprint != null) {
			result.append(fingerprint);
		}
		String serial = getDeviceSerialNumber();
		if (serial != null) {
			result.append(serial);
		}
		try {
			return result.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 encoding not supported");
		}
	}
}
