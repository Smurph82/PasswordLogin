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

public class RandomColor {

	private RandomColor() {
		
	}
	
	/**
	 * This will generate a random color.
	 * @return The random color as an <code>int</code>.
	 */
	public static int getRandomColor() {
		int red = (int)(Math.random() * 128 + 127);
        int green = (int)(Math.random() * 128 + 127);
        int blue = (int)(Math.random() * 128 + 127);
        return 0xff << 24 | (red << 16) | (green << 8) | blue;
	}
}
