package com.elsdoerfer.android.autostarts.compat;

import com.elsdoerfer.android.autostarts.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

	public CheckableRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setChecked(boolean checked) {
		Checkable checkableView = (Checkable) findViewById(R.id.checkable);
		if (checkableView != null) {
			checkableView.setChecked(checked);
		}
	}

	@Override
	public boolean isChecked() {
		Checkable checkableView = (Checkable) findViewById(R.id.checkable);
		if (checkableView != null) {
			return checkableView.isChecked();
		}
		return false;
	}

	@Override
	public void toggle() {
		Checkable checkableView = (Checkable) findViewById(R.id.checkable);
		if (checkableView != null) {
			setChecked(!checkableView.isChecked());
		}
	}

}
