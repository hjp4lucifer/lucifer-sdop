package cn.lucifer.sdop.adt;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LogAdapter extends BaseAdapter {

	protected Context context;
	protected LinkedList<String> msgs = new LinkedList<String>();

	public LogAdapter(Context context) {
		this.context = context;
	}

	protected Spanned getText(String text) {
		return Html.fromHtml(text);
	}

	private final int padding = 5;

	public void addFirst(String text) {
		msgs.addFirst(text);
		notifyDataSetChanged();
	}

	public void addLast(String text) {
		msgs.addLast(text);
		notifyDataSetChanged();
	}

	public void clear() {
		msgs.clear();
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv = new TextView(context);
		tv.setPadding(padding, padding, padding, padding);
		tv.setBackgroundColor(position % 2 == 1 ? Color.LTGRAY : Color.GRAY);
		tv.setText(getText(getItem(position)));
		// return super.getView(position, convertView, parent);
		return tv;
	}

	@Override
	public int getCount() {
		return msgs.size();
	}

	@Override
	public String getItem(int arg0) {
		// TODO Auto-generated method stub
		return msgs.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
