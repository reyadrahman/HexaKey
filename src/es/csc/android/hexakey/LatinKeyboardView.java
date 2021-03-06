/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.csc.android.hexakey;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.InputMethodSubtype;

public class LatinKeyboardView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    static final int DEFAULT_BACKGROUND_COLOR = 0xff000000;
    
    private boolean isBackgroundColorCaptured = false;
    private int backgroundColor;
    
    private boolean isBackgroundCaptured = false;
    

    public LatinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LatinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    public void onDraw (Canvas canvas) {
    	if(!isBackgroundCaptured) {
    		if (!isBackgroundColorCaptured) {
    			isBackgroundColorCaptured = true;
    			captureBackgroundColor();
    		}
    		
    		isBackgroundCaptured = true;

            Bitmap bitmap = createBitmapForCanvas(canvas);   
            
            canvas.setBitmap(bitmap);
            
            setBackgroundColor(canvas);
            
            super.onDraw(canvas);
                                   
            BitmapDrawable newBackground = getKeyboardScreenshot(bitmap);                        
            this.setBackground(newBackground);                       
    	}
    	else {
    		super.onDraw(canvas);
    	}
    }

	private Bitmap createBitmapForCanvas(Canvas canvas) {
		int width = Math.max(1, canvas.getWidth());
		int height = Math.max(1, canvas.getHeight()); 				
		
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}

	private void captureBackgroundColor() {
		try {
			ColorDrawable background = (ColorDrawable) this.getBackground();
			backgroundColor = background.getColor();
		}
		catch (ClassCastException e) {
			Log.w("Hexakey", "The background is not a color, setting default color");
			backgroundColor = DEFAULT_BACKGROUND_COLOR;
		}
	}
	
	private void setBackgroundColor(Canvas canvas) {
		int alpha = (backgroundColor & 0xf0000000) >> 24;
		int red = (backgroundColor & 0x00f00000) >> 16;
		int green = (backgroundColor & 0x0000f000) >> 8;
		int blue = (backgroundColor & 0x000000f0);
		
		canvas.drawARGB(alpha, red, green, blue);
	}

	private BitmapDrawable getKeyboardScreenshot(Bitmap bitmap) {
		Bitmap KeyboardScreenshot = null;
		if (isPortraitOrientation(bitmap)) {
			KeyboardScreenshot = bitmap;
		}
		else {
			KeyboardScreenshot = cutKeyboardArea(bitmap);				
		}
		
		return new BitmapDrawable(getResources(), KeyboardScreenshot);
	}
	


	public void clearBackground() {
		isBackgroundCaptured = false;
	}

	private boolean isPortraitOrientation(Bitmap bitmap) {
		return bitmap.getWidth() < 700;
	}
	
	private Bitmap cutKeyboardArea(Bitmap bitmap) {
		return Bitmap.createBitmap(bitmap, 
									0, bitmap.getHeight() - getHeight(),
									getWidth(), getHeight());
	}


    @Override
    protected boolean onLongPress(Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } 
        else {
            return super.onLongPress(key);
        }
    }
	
	public void autoAdjustPadding(int maxWidth) {
		final float keyWidth = getResources().getFraction(R.fraction.key_width, 1, 1);
		final float totalKeysWidth = keyWidth * getResources().getInteger(R.integer.maxKeysPerRow);
		final int remainingWidth = (int) (maxWidth * (1 - totalKeysWidth));
		final int lateralPadding = remainingWidth  >> 1;
		this.setPadding(lateralPadding, getPaddingTop(), lateralPadding, getPaddingBottom());
	}

    void setSubtypeOnSpaceKey(final InputMethodSubtype subtype) {
        invalidateAllKeys();
    }
}
