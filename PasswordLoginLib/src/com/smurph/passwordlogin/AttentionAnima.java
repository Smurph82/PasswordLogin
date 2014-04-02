/**
 * Copyright (c) Humbhi LLC. 2014 All Rights Reserved
 * Author: Ben
 * Created: Apr 2, 2014
 */

package com.smurph.passwordlogin;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;

public class AttentionAnima {

	public static ObjectAnimator nope(View view) {
		int delta = view.getResources().getDimensionPixelOffset(R.dimen.spacing_medium);

		PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofKeyframe(
				View.TRANSLATION_X, Keyframe.ofFloat(0f, 0),
				Keyframe.ofFloat(.10f, -delta), 
				Keyframe.ofFloat(.26f, delta),
				Keyframe.ofFloat(.42f, -delta), 
				Keyframe.ofFloat(.58f, delta),
				Keyframe.ofFloat(.74f, -delta), 
				Keyframe.ofFloat(.90f, delta),
				Keyframe.ofFloat(1f, 0f));

		return ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateX)
				.setDuration(500);
	}
}
