package com.tona.mousebrowser;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	// component
	private WebView mWebView;
	private ProgressBar mProgressBar;
	private RelativeLayout mLayout;
	private ImageView ivMouseCursor;
	private Button btnClick;
	private ToggleButton btnEnable;
	private View mViewLeft, mViewRight, mViewBottom, mViewPointer;
	private EditText editForm;

	private boolean isCursorEnabled = false;
	private boolean isScrollMode = false;
	private boolean isNoShowCursorRange = false;
	private boolean isShowClickLocation = false;

	private SharedPreferences pref;
	private Cursor cursor;
	private float downX, downY;

	private static final String HOME = "https://www.google.co.jp/";
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		initComponent();
		initWebView();
	}

	private void initComponent() {
		mLayout = (RelativeLayout) findViewById(R.id.root_layout);
		mViewLeft = (View) findViewById(R.id.view_left);
		mViewRight = (View) findViewById(R.id.view_right);
		mViewBottom = (View) findViewById(R.id.view_bottom);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		btnEnable = (ToggleButton) findViewById(R.id.btn_enable);
		btnClick = (Button) findViewById(R.id.btn_click);
		mViewPointer = new PointerView(this);
		editForm = (EditText) findViewById(R.id.form);
		editForm.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// EnterKeyが押されたかを判定
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
					// ソフトキーボードを閉じる
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
					// 検索処理的
					String str = editForm.getText().toString();
					if (str.startsWith("http://") || str.startsWith("https://")) {
						mWebView.loadUrl(editForm.getText().toString());
					} else {
						String searchWord = "http://www.google.co.jp/search?q=" + str.replaceAll(" ", "+");
						mWebView.loadUrl(searchWord);
					}
					return true;
				}
				return false;
			}
		});
		mLayout.addView(mViewPointer);

		btnEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPointer.invalidate();
				if (!isCursorEnabled) {
					btnClick.setVisibility(View.VISIBLE);
					mWebView.setOnTouchListener(new myOnSetTouchListener());
					isCursorEnabled = true;
					btnEnable.setText("ON");
					createCursorImage();
					switchViewCursorRange();
					onSearchRequested();
				} else {
					btnClick.setVisibility(View.INVISIBLE);
					mWebView.setOnTouchListener(null);
					isCursorEnabled = false;
					btnEnable.setText("OFF");
					mLayout.removeView(ivMouseCursor);
					switchViewCursorRange();
				}
			}
		});

		btnClick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPointer.invalidate();
				mWebView.setOnTouchListener(null);
				MotionEvent ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, cursor.getX(), cursor.getY(), 0);
				mLayout.dispatchTouchEvent(ev);
				ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, cursor.getX(), cursor.getY(), 0);
				mLayout.dispatchTouchEvent(ev);
				mWebView.setOnTouchListener(new myOnSetTouchListener());
			}
		});

		btnClick.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mViewPointer.invalidate();
				mWebView.setOnTouchListener(null);
				MotionEvent ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, cursor.getX(), cursor.getY(), 0);
				mLayout.dispatchTouchEvent(ev);
				ev = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis()+1000, MotionEvent.ACTION_UP, cursor.getX(), cursor.getY(), 0);
				mLayout.dispatchTouchEvent(ev);
				mWebView.setOnTouchListener(new myOnSetTouchListener());
				return false;
			}
		});
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setUseWideViewPort(true);

		// マルチタッチズームの有効
		settings.setBuiltInZoomControls(true);
		settings.setSupportZoom(true);
		try {
			Field mWebViewField = settings.getClass().getDeclaredField("mBuiltInZoomControls");
			mWebViewField.setAccessible(true);
			mWebViewField.set(settings, false);
		} catch (Exception e) {
			e.printStackTrace();
			settings.setBuiltInZoomControls(false);
		}

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				editForm.setText(url);
			}
		});
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
		mWebView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// 長押しした箇所の情報を取得
				HitTestResult htr = mWebView.getHitTestResult();
				switch (htr.getType()) {
					case HitTestResult.IMAGE_TYPE :
						AlertDialog.Builder alertDlg = new AlertDialog.Builder(MainActivity.this);
						alertDlg.setTitle("画像押下！");
						alertDlg.setMessage(htr.getExtra());
						alertDlg.show();
						return true;
					case HitTestResult.SRC_IMAGE_ANCHOR_TYPE :
						AlertDialog.Builder alertDlg2 = new AlertDialog.Builder(MainActivity.this);
						alertDlg2.setTitle("画像押下！");
						alertDlg2.setMessage(htr.getExtra());
						alertDlg2.show();
						return true;
					default :
						break;
				}
				return false;
			}
		});
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		if (url != null) {
			mWebView.loadUrl(url);
		} else {
			mWebView.loadUrl(HOME);
		}
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
					float y = event.getY();
					if (!isCursorOperationRange(x, y)) {
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
	}

	private boolean isCursorOperationRange(float x, float y) {
		Log.d("point", "(" + x + "," + y + ")");
		if (cursor.getOperationRange().equals("right")) {
			if (x > cursor.getDisplaySize().x / 2 && x < cursor.getDisplaySize().x) {
				return true;
			}
		}
		if (cursor.getOperationRange().equals("left")) {
			if (x > 0 && x < cursor.getDisplaySize().x / 2) {
				return true;
			}
		}
		if (cursor.getOperationRange().equals("bottom")) {
			if (y > cursor.getDisplaySize().y * 2 / 3 - (mProgressBar.getHeight() + editForm.getHeight()) && y < cursor.getDisplaySize().y) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { // バックボタンが押されたら、前のページに戻る
		if (keyCode == KeyEvent.KEYCODE_BACK) {
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
		Point p = getWindowSize();
		cursor = new Cursor(p.x, p.y);
		mViewBottom.setY(cursor.getDisplaySize().y * 2 / 3);
		readPreference();
		switchViewCursorRange();
		createCursorImage();
	}

	private Point getWindowSize() {
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		Point size = new Point();
		disp.getSize(size);
		Log.d("size", size.x + "," + size.y);
		return size;
	}

	private void readPreference() {
		cursor.setV(Float.parseFloat(pref.getString("velocity", "1.0")));
		cursor.setSizeRate(Float.parseFloat(pref.getString("size_rate", "1.0")));
		cursor.setOperationRange(pref.getString("range", "right"));
		isNoShowCursorRange = pref.getBoolean("view_cursor_range", false);
		isShowClickLocation = pref.getBoolean("click_location", false);
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

	private void switchViewCursorRange() {
		if (isCursorEnabled && !isNoShowCursorRange) {
			Log.d("range", cursor.getOperationRange());
			if (cursor.getOperationRange().equals("right")) {
				mViewRight.setVisibility(View.VISIBLE);
				mViewLeft.setVisibility(View.INVISIBLE);
				mViewBottom.setVisibility(View.INVISIBLE);
			} else if (cursor.getOperationRange().equals("left")) {
				mViewLeft.setVisibility(View.VISIBLE);
				mViewRight.setVisibility(View.INVISIBLE);
				mViewBottom.setVisibility(View.INVISIBLE);
			} else if (cursor.getOperationRange().equals("bottom")) {
				mViewBottom.setVisibility(View.VISIBLE);
				mViewLeft.setVisibility(View.INVISIBLE);
				mViewRight.setVisibility(View.INVISIBLE);
			}
		} else {
			mViewLeft.setVisibility(View.INVISIBLE);
			mViewRight.setVisibility(View.INVISIBLE);
			mViewBottom.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * クリック位置を表示するView
	 *
	 * @author meem
	 *
	 */
	private class PointerView extends View {
		Paint paint;

		public PointerView(Context context) {
			super(context);
			paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStrokeWidth(3);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if (isCursorEnabled && isShowClickLocation) {
				canvas.drawLine(0, cursor.getY(), cursor.getDisplaySize().x, cursor.getY(), paint);
				canvas.drawLine(cursor.getX(), 0, cursor.getX(), cursor.getDisplaySize().y, paint);
			}
		}
	}
}
