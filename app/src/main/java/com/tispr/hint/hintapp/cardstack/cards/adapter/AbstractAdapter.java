package com.tispr.hint.hintapp.cardstack.cards.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public abstract class AbstractAdapter<T> extends ArrayAdapter<T> {

	private static final int MESSAGE_CODE = 888;

	private final Context mContext;

	private final List<T> mList;

	private final int mItemResource;

	private int currentPosition = 0;

	public AbstractAdapter(Context c, int pItemResource, List<T> pList) {
		super(c, pItemResource, 0, pList);
		mList = pList;
		mContext = c;
		mItemResource = pItemResource;
	}

	public int getCount() {
		if (mList == null)
			return 0;
		return mList.size();
	}

	public T getItem(int position) {
		return mList.get(position);
	}

	public long getItemId(int position) {
		return Long.valueOf(position);
	}

	private long lastGetViewTime = System.currentTimeMillis();

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		lastGetViewTime = System.currentTimeMillis();
		if (convertView == null) {
			convertView = createView();
		}
		final T item = getItem(position);
		init(convertView, item);
		return convertView;
	}


	public abstract void init(View convertView, T item);


	public View createView() {
		return View.inflate(mContext, mItemResource, null);
	}

	public Context getContext() {
		return mContext;
	}

	public List<T> getList() {
		return mList;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_CODE) {
				notifyDataSetChanged();
			} else {
				super.handleMessage(msg);
			}
		}

	};

	private int prevCount = -1;

	@Override
	public void notifyDataSetChanged() {
		handler.removeMessages(MESSAGE_CODE);
		if (prevCount == getCount()) {
			long dim = System.currentTimeMillis() - lastGetViewTime;
			if (dim > 300L) {
				sendUpdateBitmapsBroadcast();
				super.notifyDataSetChanged();
			} else {
				handler.sendEmptyMessageDelayed(MESSAGE_CODE, dim + 1);
			}
		} else {
			prevCount = getCount();
			super.notifyDataSetChanged();
		}
	}

	protected void sendUpdateBitmapsBroadcast() {

	}

}
