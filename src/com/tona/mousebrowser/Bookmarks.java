package com.tona.mousebrowser;

import java.util.LinkedList;
import java.util.List;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Browser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class Bookmarks extends ListActivity {

	private LayoutInflater mInflater;
	private TextView mName;
	private TextView mUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmarks);
		BookmarkAdapter mAdapter = new BookmarkAdapter(this, getBookmarkList());
		setListAdapter(mAdapter);
		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Container container = (Container) arg0.getItemAtPosition(arg2);
				Intent intent = new Intent();
				intent.putExtra("url", container.url);
				intent.setClass(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
	private class BookmarkAdapter extends ArrayAdapter<Container> {
		public BookmarkAdapter(Context context, List<Container> objects) {
			super(context, 0, objects);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		// 1行ごとのビューを生成する
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if (convertView == null) {
				view = mInflater.inflate(R.layout.container, null);
			}
			// 現在参照しているリストの位置からItemを取得する
			Container container = this.getItem(position);

			if (container != null) {
				// Itemから必要なデータを取り出し、それぞれTextViewにセットする
				String name = container.name;
				mName = (TextView) view.findViewById(R.id.name);
				mName.setText(name);

				String url = container.url;
				mUrl = (TextView) view.findViewById(R.id.url);
				mUrl.setText(url);
			}
			return view;
		}
	}

	private List<Container> getBookmarkList() {
		LinkedList<Container> list = new LinkedList<Container>();
		/* 現在のActivityのContentResolverを得る */
		ContentResolver p = this.getContentResolver();
		/* URL、タイトルのリストを得る */
		android.database.Cursor cursor = p.query(Browser.BOOKMARKS_URI, new String[]{Browser.BookmarkColumns.URL, Browser.BookmarkColumns.TITLE}, "bookmark= 1", null, Browser.BookmarkColumns.TITLE+" ASC");

		/* URLの位置 */
		final int urlIndex = cursor.getColumnIndex(Browser.BookmarkColumns.URL);
		/* タイトルの位置 */
		final int titleIndex = cursor.getColumnIndex(Browser.BookmarkColumns.TITLE);

		/* リストの一番目に移動 */
		cursor.moveToFirst();
		/* リストを最後まで捜索 */
		do {
			/* タイトルを得る */
			String name = cursor.getString(titleIndex);
			/* URLを得る */
			String url = cursor.getString(urlIndex);
			/* リストへ追加 */
			Container container = new Container(name, url);
			list.add(container);
		} while (cursor.moveToNext());
		return list;
	}

	private class Container {
		private String name;
		private String url;
		public Container(String name, String url) {
			this.name = name;
			this.url = url;
		}
	}
}
