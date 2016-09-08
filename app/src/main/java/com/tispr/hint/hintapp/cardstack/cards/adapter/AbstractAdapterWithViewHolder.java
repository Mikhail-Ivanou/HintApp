package com.tispr.hint.hintapp.cardstack.cards.adapter;

import android.content.Context;
import android.view.View;

import java.util.HashMap;
import java.util.List;

public abstract class AbstractAdapterWithViewHolder<T> extends
		AbstractAdapter<T> {

	public AbstractAdapterWithViewHolder(Context c, int pItemResource,
			List<T> pList) {
		super(c, pItemResource, pList);
	}

	public void init(View convertView, T item) {
		Object tag = convertView.getTag();
		ViewHolder holder = null;
		if (tag instanceof ViewHolder) {
			holder = (ViewHolder) tag;
		}
		if (holder == null) {
			holder = createHolder(convertView);
			convertView.setTag(holder);
		}
		init(holder, item);
	}

	protected ViewHolder createHolder(View convertView) {
		return new ViewHolder(convertView);
	};

	public abstract void init(ViewHolder view, T item);

	public static class ViewHolder {

		private View view;

		private HashMap<Integer, View> storage;

		private Object tag;
		
		public ViewHolder(View view) {
			this.view = view;
			storage = new HashMap<>();
			this.setTag(view.getTag());
			this.view.setTag(this);
		}

		public View findViewById(int id) {
			if (storage.containsKey(id)) {
				return storage.get(id);
			}
			View viewById = view.findViewById(id);
			storage.put(id, viewById);
			return viewById;
		}

		public View getView() {
			return view;
		}

		public Object getTag() {
			return tag;
		}

		public void setTag(Object tag) {
			this.tag = tag;
		}
		
	}

}
