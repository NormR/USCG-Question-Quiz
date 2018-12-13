package com.normsstuff.uscg_questions;

import java.io.InputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

public class ShowDiagramView extends View {
	Bitmap bitmap;
	PopupWindow puw;

	//-------------------------------------------------------------------
	public ShowDiagramView(Context context, AttributeSet attrs) {
		super(context, attrs);
		System.out.println("ShowDiagramView constructor");
		drawPaint.setAntiAlias(false);    // Will these fix the blurred image???
		drawPaint.setFilterBitmap(false);
	}
	
	public void setBitmap(Bitmap bitmap, PopupWindow puw, float pxlDensity) {
		this.bitmap =  bitmap;
		this.puw = puw;    // save for touch to call dismiss
		//  Get image's size
		srcRect.right = bitmap.getWidth();
		srcRect.bottom = bitmap.getHeight();
		trgtRect.right = (int)(srcRect.right * pxlDensity);  // enlarge as per density
		trgtRect.bottom =  (int)(srcRect.bottom * pxlDensity);
		System.out.println("setBitmap() srcRect="+srcRect);
 	}
	
	// Define fields for onDraw
	Paint drawPaint = new Paint();
	Rect srcRect = new Rect();  // show full image
	Rect trgtRect = new Rect();   // this will be filled in onDraw()


	
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
			
		if(bitmap == null) {
			System.out.println("onDraw bitmap null");
			return;
		}
    	canvas.drawBitmap(bitmap, srcRect, trgtRect, drawPaint);
	
	}
	
	int dx;
	int dy;
	
	@Override
	public boolean onTouchEvent (MotionEvent me) {
//		Log.d("SCV.oTouchEvt","cardLoc="+Arrays.toString(cardLoc));
		int rx = (int)me.getRawX();
		int ry = (int)me.getRawY();
		System.out.println("touched at rx="+rx +", ry="+ry);

        switch (me.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	            dx = (int) me.getRawX();    // Change to us Use getRaw 
	            dy = (int) me.getRawY();
	            Log.d("onTouchEvent DOWN", "dx: " + dx + " dy: " + dy);
	            break;
	        case MotionEvent.ACTION_MOVE:
	            int x = (int) me.getRawX();
	            int y = (int) me.getRawY();
	            int left =  (x - dx);
	            int top =   (y - dy);
	            Log.d("onTouchEvent MOVE", "x: " + left + " y: " + top);
	            puw.update(left, top, -1, -1, true);
	            break;
	        case MotionEvent.ACTION_UP:
//	    		puw.dismiss();  // close window when touched released TESTING
	        	break;
        }

		return true;
	}
/*
	//------------------------------------------------------------------
	//  Show a message in an Alert box
	private void showMsg(String msg) {

		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setMessage(msg);
		ad.setButton(DialogInterface.BUTTON_POSITIVE, "Clear messsge", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();                    
		    }
		});
		ad.show();
	}
*/
}
