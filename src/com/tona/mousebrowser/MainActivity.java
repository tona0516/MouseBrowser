package com.tona.mousebrowser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

	private WebView mWebView;
	private RelativeLayout mLayout;
	private ImageView ivMouseCursor;
	private Button btnClick, btnLongClick, btnDoubleClick, btnEnable;
	private static  final String HOME = "https://www.google.co.jp/";
	private String mStateUrl;
	private int mStateX,mStateY;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private boolean isCursorEnabled = false;

	private SharedPreferences pref;
	private Cursor cursor;
	private float downX, downY, cursorX, cursorY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ページ読み込み中にぐるぐる回す
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		mLayout = (RelativeLayout) findViewById(R.id.root_layout);
		
		if(savedInstanceState !=null){
			mStateUrl = savedInstanceState.getString("url");
			mStateX = savedInstanceState.getInt("x",0);
			mStateY = savedInstanceState.getInt("y",0);
			mWebView.loadUrl(mStateUrl);
			mWebView.scrollTo(mStateX, mStateY);
		}else{
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.52 Safari/537.36");
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				// プロセスバーの表示終了
				setProgressBarIndeterminateVisibility(false);
			}

			// ページの読み込み時に呼ばれる
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				// プロセスバーの表示開始
				setProgressBarIndeterminateVisibility(true);
			}
		});
		mWebView.loadUrl(HOME);
		}

		btnClick = (Button) findViewById(R.id.btn_click);
		btnClick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.setOnTouchListener(null);
				MotionEvent ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_DOWN, ivMouseCursor.getX(), ivMouseCursor.getY(), 0);
				Log.d("dispatch", "" + mWebView.dispatchTouchEvent(ev));
				ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_UP, ivMouseCursor.getX(), ivMouseCursor.getY(), 0);
				Log.d("dispatch", "" + mWebView.dispatchTouchEvent(ev));
				mWebView.setOnTouchListener(new myOnSetTouchListener());
			}
		});

		btnLongClick = (Button) findViewById(R.id.btn_longclick);
		btnLongClick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.setOnTouchListener(null);
				MotionEvent ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 1000, MotionEvent.ACTION_DOWN, ivMouseCursor.getX(), ivMouseCursor.getY(), 0);
				Log.d("dispatch", "" + mWebView.dispatchTouchEvent(ev));
				mWebView.setOnTouchListener(new myOnSetTouchListener());
			}
		});

		btnDoubleClick = (Button) findViewById(R.id.btn_doubleclick);
		btnDoubleClick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.setOnTouchListener(null);
				MotionEvent ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_DOWN, ivMouseCursor.getX(), ivMouseCursor.getY(), 0);
				Log.d("dispatch", "" + mWebView.dispatchTouchEvent(ev));
				ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_UP, ivMouseCursor.getX(), ivMouseCursor.getY(), 0);
				Log.d("dispatch", "" + mWebView.dispatchTouchEvent(ev));
				ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_DOWN, ivMouseCursor.getX(), ivMouseCursor.getY(), 0);
				Log.d("dispatch", "" + mWebView.dispatchTouchEvent(ev));
				ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_UP, ivMouseCursor.getX(), ivMouseCursor.getY(), 0);
				Log.d("dispatch", "" + mWebView.dispatchTouchEvent(ev));
				mWebView.setOnTouchListener(new myOnSetTouchListener());
			}
		});

		btnEnable = (Button) findViewById(R.id.btn_enable);
		btnEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isCursorEnabled) {
					mWebView.setOnTouchListener(new myOnSetTouchListener());
					isCursorEnabled = true;
					btnEnable.setText("disable");
					btnClick.setEnabled(true);
					btnLongClick.setEnabled(true);
					btnDoubleClick.setEnabled(true);
					createCursorImage();
				} else {
					mWebView.setOnTouchListener(null);
					isCursorEnabled = false;
					mLayout.removeView(ivMouseCursor);
					btnEnable.setText("enable");
					btnClick.setEnabled(false);
					btnLongClick.setEnabled(false);
					btnDoubleClick.setEnabled(false);
				}
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.settings) {
			startActivity(new Intent(getApplicationContext(), Pref.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class myOnSetTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN :
					downX = event.getX();
					downY = event.getY();
					cursorX = cursor.getX();
					cursorY = cursor.getY();
					break;
				case MotionEvent.ACTION_MOVE :
					float newX = (cursorX - (downX - event.getX()) * cursor.getV());
					float newY = (cursorY - (downY - event.getY()) * cursor.getV());
					cursor.setX(newX);
					cursor.setY(newY);
					ivMouseCursor.setX(newX);
					ivMouseCursor.setY(newY);

					if (newX > mLayout.getWidth()) {
						cursor.setX(mLayout.getWidth());
						ivMouseCursor.setX(mLayout.getWidth());
						cursorX = mLayout.getWidth();
						downX = event.getX();
					}
					if (newX < 0) {
						cursor.setX(0);
						ivMouseCursor.setX(0);
						cursorX = 0;
						downX = event.getX();
					}
					if (newY > mLayout.getHeight()) {
						cursor.setY(mLayout.getHeight());
						ivMouseCursor.setY(mLayout.getHeight());
						cursorY = mLayout.getHeight();
						downY = event.getY();
					}
					if (newY < 0) {
						cursor.setY(0);
						ivMouseCursor.setY(0);
						cursorY = 0;
						downY = event.getY();
					}

					break;
				case MotionEvent.ACTION_UP :
					break;
				default :
					break;
			}
			Log.d("position", (int) ivMouseCursor.getX() + "," + (int) ivMouseCursor.getY());
			return true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { // バックボタンが押されたら、前のページに戻る
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// oWebview.goBack();
			if (mWebView.canGoBack())
				mWebView.goBack();
			else
				finish();
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("LifeCycle", "onResume");
		if (cursor == null) {
			WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
			Display disp = wm.getDefaultDisplay();
			Point size = new Point();
			disp.getSize(size);
			Log.d("size", size.x + "," + size.y);
			cursor = new Cursor(size.x, size.y);
		}
		cursor.setV(Float.parseFloat(pref.getString("velocity", "1.0")));
		cursor.setSizeRate(Float.parseFloat(pref.getString("size_rate", "1.0")));

		createCursorImage();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLayout.removeView(ivMouseCursor);
	}

	private void createCursorImage() {
		ivMouseCursor = new ImageView(getApplicationContext());
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
		Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, (int) cursor.getWidth(), (int) cursor.getHeight(), false); // 13:16で調整
		ivMouseCursor.setImageBitmap(bmp2);
		ivMouseCursor.setLayoutParams(new LayoutParams(WC, WC));
		// ivMouseCursor.setBackgroundColor(Color.RED);
		ivMouseCursor.setX(cursor.getX());
		ivMouseCursor.setY(cursor.getY());
		if (isCursorEnabled)
			ivMouseCursor.setVisibility(View.VISIBLE);
		else
			ivMouseCursor.setVisibility(View.INVISIBLE);
		mLayout.addView(ivMouseCursor);
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("url", mWebView.getUrl());
		outState.putInt("x", mWebView.getScrollX());
		outState.putInt("y", mWebView.getScrollY());
	}
}
