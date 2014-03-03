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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {

	public CircleView(Context context) {
		this(context, null);
	}

	public CircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		int colorB = DEFAULT_BORDER_COLOR;
		int colorUF = DEFAULT_UNFILLED_COLOR;
		int colorF = DEFAULT_FILLED_COLOR;
	    Resources.Theme theme = context.getTheme();
	    TypedArray array = theme.obtainStyledAttributes(
	        attrs, R.styleable.circleView, 0, 0);
	    if (array != null) {
	      int n = array.getIndexCount();
	      for (int i = 0; i < n; i++) {
	        int attr = array.getIndex(i);

	        switch (attr) {
	        case R.styleable.circleView_color_border:
	        	colorB = array.getColor(attr, DEFAULT_BORDER_COLOR);
	          break;
	        case R.styleable.circleView_color_filled:
	        	colorF = array.getColor(attr, DEFAULT_FILLED_COLOR);
	          break;
	        case R.styleable.circleView_color_unfilled:
	        	colorUF = array.getColor(attr, DEFAULT_UNFILLED_COLOR);
	          break;
	        }
	      }
	    }
	    
	    mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    mBorderPaint.setStrokeWidth(3); // hairline
	    mBorderPaint.setStyle(Style.STROKE);
	    mBorderPaint.setColor(colorB);
	    
	    mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    mFillPaint.setColor(colorF);
	    
	    mUnFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    mUnFillPaint.setColor(colorUF);
	    
	    theme = null;
	    array.recycle();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (mDrawingRect==null) {
			int left = getPaddingLeft();
			int top = getPaddingTop();
			int right = getWidth() - getPaddingRight();
			int botttom = getHeight() - getPaddingBottom();
			
//			mDrawingRect = new RectF(1, 1, getWidth() - 1, getHeight() - 1);
			mDrawingRect = new RectF(left, top, right, botttom);
		}
			
		if (mIsEntered) {
		    canvas.drawOval(mDrawingRect, mFillPaint);
		} else {
		    canvas.drawOval(mDrawingRect, mUnFillPaint);
		}
		
		// Draw the outer border
	    canvas.drawOval(mDrawingRect, mBorderPaint);
	}

	/**
	 * 
	 * @param isEntered
	 */
	public void setEntered(boolean isEntered) {
		mIsEntered = isEntered;
		invalidate();
	}
	
	/**
	 * 
	 * @param color
	 */
	public void setCustomFillColor(int color) {
		if (mFillPaint!=null)
			mFillPaint.setColor(color);
	}
	
	/**  */
	private RectF mDrawingRect;
	
	/**  */
	private boolean mIsEntered = false;

	/**  */
	private final Paint mFillPaint;
	
	/**  */
	private final Paint mUnFillPaint;
	
	/**  */
	private final Paint mBorderPaint;

	/** The default color of the inside of the circle */
	private static final int DEFAULT_UNFILLED_COLOR = 0xccc7c7c7;
	
	/**  */
	private static final int DEFAULT_FILLED_COLOR = 0xaae7a916;
	
	/**  */
	private static final int DEFAULT_BORDER_COLOR = 0xaa8d8d8d;
}
