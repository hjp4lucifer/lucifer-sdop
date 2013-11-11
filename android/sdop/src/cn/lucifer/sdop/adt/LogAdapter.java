package cn.lucifer.sdop.adt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LogAdapter extends ArrayAdapter<String> {

	private final String space = " ";

	private final DateFormat timeFormat = new SimpleDateFormat(
			"M月d日 HH:mm:ss.SSS");

	public LogAdapter(Context context, int resource) {
		super(context, resource);
	}

	protected Spanned getText(String text) {
		return Html.fromHtml(timeFormat.format(new Date()) + space + text);
	}

	private final int padding = 5;
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv = new TextView(getContext());
		tv.setPadding(padding, padding, padding, padding);
		tv.setBackgroundColor(position % 2 == 1 ? Color.LTGRAY : Color.GRAY);
		tv.setText(getText(getItem(position)));
		// return super.getView(position, convertView, parent);
		return tv;
	}

}
