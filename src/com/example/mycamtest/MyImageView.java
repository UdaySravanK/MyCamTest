package com.example.mycamtest;

import java.util.NoSuchElementException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class MyImageView extends RelativeLayout implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener,ScaleGestureDetector.OnScaleGestureListener,Runnable{

	private static final int  MOVING_DIAGONALLY = 0;
	private static final int  MOVING_LEFT       = 1;
	private static final int  MOVING_RIGHT      = 2;
	private static final int  MOVING_UP         = 3;
	private static final int  MOVING_DOWN       = 4;

	private static final int  FLING_MARGIN      = 100;

	private static final float MIN_SCALE        = 1.0f;
	private static final float MAX_SCALE        = 5.0f;
	private float             mScale     = 1.0f;
	private int               mXScroll;    // Scroll amounts recorded from events.
	private int               mYScroll;    // and then accounted for in onLayout
	private final GestureDetector mGestureDetector;
	private final ScaleGestureDetector mScaleGestureDetector;
	private final Scroller mScroller;
	private int               mScrollerLastX;
	private int               mScrollerLastY;
	private MyThread myThread;
	public MotionEvent _lastEvent;
	private boolean stopMyThread = false;
	private ImageView imageView;
	
	public MyImageView(Context context) {
		super(context);
		mGestureDetector = new GestureDetector(this);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		mScroller        = new Scroller(context);
		imageView = new ImageView(context);
		addView(imageView);
	}
	public MyImageView(Context context, AttributeSet attrs) {
		super(context,attrs);
		mGestureDetector = new GestureDetector(this);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		mScroller        = new Scroller(context);
		imageView = new ImageView(context);
		addView(imageView);
	}
	public void setImage(Bitmap bitmap)
	{
		imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		imageView.setBackgroundColor(Color.TRANSPARENT);
		imageView.setImageBitmap(bitmap);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		mScaleGestureDetector.onTouchEvent(event);
		mGestureDetector.onTouchEvent(event);
		return true;
	}
	
	// GestureDetector.OnGestureListener 
	@Override
	public boolean onDown(MotionEvent arg0) {
		mScroller.forceFinished(true);
		return true;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float velocityX,float velocityY) {
		View v = getChildAt(0);
		if (v != null) 
		{
			Rect bounds = getScrollBounds(v);
	
			mScrollerLastX = mScrollerLastY = 0;
	
			Rect expandedBounds = new Rect(bounds);
			expandedBounds.inset(-FLING_MARGIN, -FLING_MARGIN);
	
			if(withinBoundsInDirectionOfTravel(bounds, velocityX, velocityY)
					&& expandedBounds.contains(0, 0)) {
				mScroller.fling(0, 0, (int)velocityX, (int)velocityY, bounds.left, bounds.right, bounds.top, bounds.bottom);
				post(this);
			}
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		mXScroll -= distanceX;
		mYScroll -= distanceY;
		requestLayout();
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	//GestureDetector.OnDoubleTapListener 
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		_lastEvent = e;
		if(myThread!=null)
		{
			if(myThread.isAlive())
			{
				stopMyThread = true;
			}
		}
		stopMyThread = false;
		myThread = new MyThread(e.getX(),e.getY(),mScale);
		myThread.setDaemon(true);
		myThread.start();
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	//ScaleGestureDetector.OnScaleGestureListener
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		zoomToPoint(detector.getScaleFactor(),new Point((int)detector.getFocusX(),(int)detector.getFocusY()));
		return false;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
//		mScaling = true;
		// Ignore any scroll amounts yet to be accounted for: the
		// screen is not showing the effect of them, so they can
		// only confuse the user
		mXScroll = mYScroll = 0;
		// Avoid jump at end of scaling by disabling scrolling
		// until the next start of gesture
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		
	}
	
	private Rect getScrollBounds(View v) {
		// There can be scroll amounts not yet accounted for in
		// onLayout, so add mXScroll and mYScroll to the current
		// positions when calculating the bounds.
		return getScrollBounds(v.getLeft() + mXScroll,
				               v.getTop() + mYScroll,
				               v.getLeft() + v.getMeasuredWidth() + mXScroll,
				               v.getTop() + v.getMeasuredHeight() + mYScroll);
	}
	
	private Rect getScrollBounds(int left, int top, int right, int bottom) {
		int xmin = getWidth() - right;
		int xmax = -left;
		int ymin = getHeight() - bottom;
		int ymax = -top;

		// In either dimension, if view smaller than screen then
		// constrain it to be central
		if (xmin > xmax) xmin = xmax = (xmin + xmax)/2;
		if (ymin > ymax) ymin = ymax = (ymin + ymax)/2;

		return new Rect(xmin, ymin, xmax, ymax);
	}
	
	private static boolean withinBoundsInDirectionOfTravel(Rect bounds, float vx, float vy) {
		switch (directionOfTravel(vx, vy)) {
		case MOVING_DIAGONALLY: return bounds.contains(0, 0);
		case MOVING_LEFT:       return bounds.left <= 0;
		case MOVING_RIGHT:      return bounds.right >= 0;
		case MOVING_UP:         return bounds.top <= 0;
		case MOVING_DOWN:       return bounds.bottom >= 0;
		default: throw new NoSuchElementException();
		}
	}
	
	private static int directionOfTravel(float vx, float vy) {
		if (Math.abs(vx) > 2 * Math.abs(vy))
			return (vx > 0) ? MOVING_RIGHT : MOVING_LEFT;
		else if (Math.abs(vy) > 2 * Math.abs(vx))
			return (vy > 0) ? MOVING_DOWN : MOVING_UP;
		else
			return MOVING_DIAGONALLY;
	}
	
	@Override
	public void run() {
		if (!mScroller.isFinished()) {
			mScroller.computeScrollOffset();
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			mXScroll += x - mScrollerLastX;
			mYScroll += y - mScrollerLastY;
			mScrollerLastX = x;
			mScrollerLastY = y;
			requestLayout();
			post(this);
		}
	}
	private void zoomToPoint(float scale_factor, Point focus) {
		float previousScale = mScale;
		mScale = Math.min(Math.max(mScale * scale_factor, MIN_SCALE), MAX_SCALE);
		float factor = mScale/previousScale;//it decides i.e to zoomin/out

		View v = getChildAt(0);
		if (v != null) {
			// Work out the focus point relative to the view top left
			int viewFocusX = focus.x - (v.getLeft() + mXScroll);
			int viewFocusY = focus.y - (v.getTop() + mYScroll);
			// Scroll to maintain the focus point
			mXScroll += viewFocusX - viewFocusX * factor;
			mYScroll += viewFocusY - viewFocusY * factor;
			post(new Runnable() {
			    @Override
				public void run() {
			        requestLayout();
			    }
			});
		}
	}
	private class MyThread extends Thread
	{
		float x,y,scale;
		public MyThread(float x, float y, float mScale)
		{
			this.x = x;
			this.y = y;
			scale = mScale;
		}
		
		@Override
		public void run()
		{
			super.run();
			float publishProgress;
			PointF point = new PointF(x, y);
			//logic to find ,should zoom out /zoom in
			if(scale<=1)
			{
				publishProgress = 1.01f;
			}
			else
			{
				publishProgress = 0.98f;
			}
			
			if(publishProgress>1f)
			{
				while(mScale<MAX_SCALE)//zoom in upto max scale
				{
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(stopMyThread)
					{
						break;
					}
					zoomToPoint(publishProgress, new Point((Math.round(point.x)),(Math.round(point.y))));
				}
			}
			else
			{
				while(mScale>MIN_SCALE)//zoom out upto min scale
				{
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(stopMyThread)
					{
						break;
					}
					zoomToPoint(publishProgress, new Point((Math.round(point.x)),(Math.round(point.y))));
				}
			}
			_lastEvent.setAction(MotionEvent.ACTION_UP);
			onTouchEvent(_lastEvent);
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureView(getChildAt(0));
	}
	
	private void measureView(View v) {
		// See what size the view wants to be
		v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		// Work out a scale that will fit it to this view
		float scale = Math.min((float)getWidth()/(float)v.getMeasuredWidth(),
					(float)getHeight()/(float)v.getMeasuredHeight());
		// Use the fitting values scaled by our current scale factor
		v.measure(View.MeasureSpec.EXACTLY | (int)(v.getMeasuredWidth()*scale*mScale),
				View.MeasureSpec.EXACTLY | (int)(v.getMeasuredHeight()*scale*mScale));
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		View cv = getChildAt(0);

		// Ensure current view is present
		int cvLeft, cvRight, cvTop, cvBottom;

		cvLeft = cv.getLeft() + mXScroll;
		cvTop  = cv.getTop()  + mYScroll;
		// Scroll values have been accounted for
		mXScroll = mYScroll = 0;
		
		cvRight  = cvLeft + cv.getMeasuredWidth();
		cvBottom = cvTop  + cv.getMeasuredHeight();
						
		Point corr = getCorrection(getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
		cvRight  += corr.x;
		cvLeft   += corr.x;
		cvTop    += corr.y;
		cvBottom += corr.y;
		
		cv.layout(cvLeft, cvTop, cvRight, cvBottom);
	}
	private Point getCorrection(Rect bounds) {
		return new Point(Math.min(Math.max(0,bounds.left),bounds.right),
				         Math.min(Math.max(0,bounds.top),bounds.bottom));
	}
}
