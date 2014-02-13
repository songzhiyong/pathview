package com.example.picpin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ShowPathActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(new PathView(this));
	}
}

class PathView extends View {
	private Context mCtx;
	private Path[] mPath;
	private Bitmap[] mBmp;
	private Matrix[] matrixs;
	private boolean[] mBmpFlag;
	private float[][] mPathLT;
	private float[][] mPathOffset;
	int mPathNum = 3;
	private int border = 6;
	private int viewWdh, viewHgt, itemHgt;
	public static int PICIDS[] = { R.drawable.catarina, R.drawable.lake,
			R.drawable.m74hubble, R.drawable.sunset, R.drawable.tahiti };

	public PathView(Context context) {
		super(context);
		mCtx = context;
		scaleGestureDetector = new ScaleGestureDetector(context,
				new ScaleListener());
		initialView();
		initPath();
	}

	private void initPath() {
		// mPathNum = (int) (Math.random() * 10);
		mPath = new Path[mPathNum];
		for (int i = 0; i < mPathNum; i++) {
			mPath[i] = new Path();
		}
		mBmpFlag = new boolean[mPathNum];

		mPathLT = new float[mPathNum][2];
		mPathOffset = new float[mPathNum][2];
		for (int i = 0; i < mPathNum; i++) {
			mBmpFlag[i] = false;
			mPathLT[i][0] = 0f;
			mPathLT[i][1] = 0f;
			mPathOffset[i][0] = 0f;
			mPathOffset[i][1] = 0f;
		}

		for (int i = 0; i < mPathNum; i++) {
			mPathLT[i][0] = 0;
			mPathLT[i][1] = (itemHgt + border) * i;
			mPath[i].moveTo(mPathLT[i][0], mPathLT[i][1]);
			mPath[i].lineTo(viewWdh, mPathLT[i][1]);
			mPath[i].lineTo(viewWdh, mPathLT[i][1] + itemHgt);
			mPath[i].lineTo(0, mPathLT[i][1] + itemHgt);
			mPath[i].close();
		}

		// get bitmap
		mBmp = new Bitmap[mPathNum];
		for (int i = 0; i < mPathNum; i++) {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(getResources(), PICIDS[i], opt);
			int bmpWdh = opt.outWidth;
			int bmpHgt = opt.outHeight;

			int size = caculateSampleSize(bmpWdh, bmpHgt, viewWdh, itemHgt);
			opt.inJustDecodeBounds = false;
			opt.inSampleSize = size;
			mBmp[i] = BitmapFactory.decodeResource(getResources(), PICIDS[i],
					opt);
		}
	}

	private int caculateSampleSize(int picWdh, int picHgt, int showWdh,
			int showHgt) {
		// 如果此时显示区域比图片大，直接返回
		if ((showWdh < picWdh) || (showHgt < picHgt)) {
			int wdhSample = picWdh / showWdh;
			int hgtSample = picHgt / showHgt;
			// 利用小的来处理
			int sample = wdhSample > hgtSample ? hgtSample : wdhSample;
			int minSample = 2;
			while (sample > minSample) {
				minSample *= 2;
			}
			return minSample >> 1;
		} else {
			return 1;
		}
	}

	private void initialView() {
		DisplayMetrics display = mCtx.getResources().getDisplayMetrics();
		viewWdh = display.widthPixels;
		itemHgt = display.heightPixels >> 2;
		viewHgt = itemHgt * 3 + 2 * border;
		Log.d("screen parameter", "wdh = " + viewWdh + ":hgt = "
				+ display.heightPixels);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(viewWdh, viewHgt);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.GRAY);// 显示背景颜色
		Paint paint = new Paint();
		paint.setColor(Color.GRAY);
		canvas.drawPaint(paint);
		// draw1(canvas);
		draw2(canvas, paint);
	}

	private void draw1(Canvas canvas) {
		for (int i = 0; i < mPathNum; i++) {
			canvas.save();
			canvas.clipPath(mPath[i]);
			BitmapDrawable mDrawable = new BitmapDrawable(mBmp[i]);
			float minX = mPathLT[i][0] + mPathOffsetX + mPathOffset[i][0];
			float minY = mPathLT[i][1] + mPathOffsetY + mPathOffset[i][1];
			mDrawable.setBounds((int) minX, (int) minY, viewWdh,
					(itemHgt + border) * i + itemHgt);
			mDrawable.draw(canvas);
			canvas.restore();
		}
	}

	private void draw2(Canvas canvas, Paint paint) {
		for (int i = 0; i < mPathNum; i++) {
			canvas.save();
			// canvas.scale(scaleFactor, scaleFactor);
			drawScene(canvas, paint, i);
			canvas.restore();
		}
	}

	private void drawScene(Canvas canvas, Paint paint, int idx) {
		canvas.clipPath(mPath[idx]);
		canvas.drawColor(Color.GRAY);
		if (mBmpFlag[idx]) {
			canvas.drawBitmap(mBmp[idx], mPathLT[idx][0] + mPathOffsetX
					+ mPathOffset[idx][0], mPathLT[idx][1] + mPathOffsetY
					+ mPathOffset[idx][1], paint);
		} else {
			canvas.drawBitmap(mBmp[idx], mPathLT[idx][0] + mPathOffset[idx][0],
					mPathLT[idx][1] + mPathOffset[idx][1], paint);
		}

	}

	float ptx, pty;
	float mPathOffsetX, mPathOffsetY;
	private float scaleFactor = 1.0f;
	private ScaleGestureDetector scaleGestureDetector;

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scaleFactor *= detector.getScaleFactor();
			scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
			invalidate();
			return true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		scaleGestureDetector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			for (int i = 0; i < mPathNum; i++) {
				mBmpFlag[i] = false;
			}
			ptx = event.getRawX();
			pty = event.getRawY();
			mPathOffsetX = 0;
			mPathOffsetY = 0;
			int cflag = 0;
			for (cflag = 0; cflag < mPathNum; cflag++) {
				if (contains(mPath[cflag], ptx, pty)) {
					mBmpFlag[cflag] = true;
					break;
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			mPathOffsetX = event.getRawX() - ptx;
			mPathOffsetY = event.getRawY() - pty;
			invalidate();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			break;
		case MotionEvent.ACTION_UP:
			for (int i = 0; i < mPathNum; i++) {
				if (mBmpFlag[i]) {
					mPathOffset[i][0] += event.getRawX() - ptx;
					mPathOffset[i][1] += event.getRawY() - pty;

					if (mPathOffset[i][0] > 0) {
						mPathOffset[i][0] = 0;
					}
					if (mPathOffset[i][0] < -(mBmp[i].getWidth() - viewWdh)) {
						mPathOffset[i][0] = -(mBmp[i].getWidth() - viewWdh);
					}
					if (mPathOffset[i][1] > 0) {
						mPathOffset[i][1] = 0;
					}
					if (mPathOffset[i][1] < -(mBmp[i].getHeight() - itemHgt)) {
						mPathOffset[i][1] = -(mBmp[i].getHeight() - itemHgt);
					}
					mBmpFlag[i] = false;
					break;
				}
			}
			invalidate();
			break;
		default:
			break;
		}

		return true;
	}

	private boolean contains(Path paramPath, float pointx, float pointy) {
		RectF localRectF = new RectF();
		paramPath.computeBounds(localRectF, true);
		Region localRegion = new Region();
		localRegion.setPath(paramPath, new Region((int) localRectF.left,
				(int) localRectF.top, (int) localRectF.right,
				(int) localRectF.bottom));
		return localRegion.contains((int) pointx, (int) pointy);
	}

}