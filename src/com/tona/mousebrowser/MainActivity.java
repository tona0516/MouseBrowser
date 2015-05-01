package com.tona.mousebrowser;

import android.annotation.SuppressLint;
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
import android.view.animation.AlphaAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private ProgressBar mProgressBar;
	private WebView mWebView;
	private RelativeLayout mLayout;
	private ImageView ivMouseCursor;
	private Button btnClick;
	private ToggleButton btnEnable;
	private static final String HOME = "https://www.google.co.jp/";
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private boolean isCursorEnabled = false;
	private boolean isScrollMode = false;

	private SharedPreferences pref;
	private Cursor cursor;
	private float downX, downY;


	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		mLayout = (RelativeLayout) findViewById(R.id.root_layout);

		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

		mWebView = (WebView) findViewById(R.id.webview);
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);

		mWebView.setWebViewClient(new WebViewClient());
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO 自動生成されたメソッド・スタブ
				super.onProgressChanged(view, newProgress);
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setProgress(newProgress);
				if (newProgress == 100) {
					mProgressBar.setProgress(0);
					mProgressBar.setVisibility(View.INVISIBLE);
				}
			}
		});
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		if (url != null) {
			mWebView.loadUrl(url);
		} else {
			mWebView.loadUrl(HOME);
		}

		btnEnable = (ToggleButton) findViewById(R.id.btn_enable);
		btnEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isCursorEnabled) {
					btnClick.setVisibility(View.VISIBLE);
					AlphaAnimation aa1 = new AlphaAnimation(0, 1);
					aa1.setDuration(500);
					btnClick.setAnimation(aa1);
					mWebView.setOnTouchListener(new myOnSetTouchListener());
					isCursorEnabled = true;
					btnEnable.setText("ON");
					createCursorImage();
				} else {
					btnClick.setVisibility(View.INVISIBLE);
					AlphaAnimation aa2 = new AlphaAnimation(1, 0);
					aa2.setDuration(500);
					btnClick.setAnimation(aa2);
					mWebView.setOnTouchListener(null);
					isCursorEnabled = false;
					mLayout.removeView(ivMouseCursor);
					btnEnable.setText("OFF");
				}
			}
		});

		btnClick = (Button) findViewById(R.id.btn_click);
		btnClick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWebView.setOnTouchListener(null);
				MotionEvent ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_DOWN, cursor.getX(), cursor.getY(), 0);
				Log.d("dispatch", "" + mWebView.dispatchTouchEvent(ev));
				ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_UP, cursor.getX(), cursor.getY(), 0);
				Log.d("dispatch", "" + mWebView.dispatchTouchEvent(ev));
				mWebView.setOnTouchListener(new myOnSetTouchListener());
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.settings) {
			startActivity(new Intent(getApplicationContext(), Pref.class));
			return true;
		} else if (id == R.id.bookmarks) {
			startActivity(new Intent(getApplicationContext(), Bookmarks.class));
		}
		return super.onOptionsItemSelected(item);
	}

	class myOnSetTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View view, MotionEvent event) {

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN :
					float x = event.getX();
					float w = cursor.getDisplaySize().x;
					// Log.d("xw", x+","+w);
					if (!isRange(x)) {
						isScrollMode = true;
						return false;
					}
					downX = event.getX();
					downY = event.getY();
					cursor.setDownX(cursor.getX());
					cursor.setDownY(cursor.getY());
					break;
				case MotionEvent.ACTION_MOVE :
					if (isScrollMode)
						return false;
					float newX = (cursor.getDownX() - (downX - event.getX()) * cursor.getV());
					float newY = (cursor.getDownY() - (downY - event.getY()) * cursor.getV());
					cursor.setX(newX);
					cursor.setY(newY);
					ivMouseCursor.setX(newX);
					ivMouseCursor.setY(newY);
					int disX = cursor.getDisplaySize().x;
					int disY = cursor.getDisplaySize().y;
					if (newX > cursor.getDisplaySize().x) {
						cursor.setX(disX);
						ivMouseCursor.setX(disX);
						cursor.setDownX(disX);
						downX = event.getX();
					}
					if (newX < 0) {
						cursor.setX(0);
						ivMouseCursor.setX(0);
						cursor.setDownX(0);
						downX = event.getX();
					}
					if (newY > disY) {
						cursor.setY(disY);
						ivMouseCursor.setY(disY);
						cursor.setDownY(disY);
						downY = event.getY();
					}
					if (newY < 0) {
						cursor.setY(0);
						ivMouseCursor.setY(0);
						cursor.setDownY(0);
						downY = event.getY();
					}
					break;
				case MotionEvent.ACTION_UP :
					isScrollMode = false;
					return false;
				default :
					break;
			}
			return true;
		}

		private boolean isRange(float x) {
			if (cursor.getOperationrange().equals("right")) {
				if (x > cursor.getDisplaySize().x / 2 && x < cursor.getDisplaySize().x) {
					return true;
				}
			}
			if (cursor.getOperationrange().equals("left")) {
				if (x > 0 && x < cursor.getDisplaySize().x / 2) {
					return true;
				}
			}
			return false;
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
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		Point size = new Point();
		disp.getSize(size);
		Log.d("size", size.x + "," + size.y);
		cursor = new Cursor(size.x, size.y);
		cursor.setV(Float.parseFloat(pref.getString("velocity", "1.0")));
		cursor.setSizeRate(Float.parseFloat(pref.getString("size_rate", "1.0")));
		cursor.setOperationrange(pref.getString("range", "right"));
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
}
