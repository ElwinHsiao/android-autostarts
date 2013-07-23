package com.elsdoerfer.android.autostarts;

import java.util.ArrayList;
import java.util.List;

import com.elsdoerfer.android.autostarts.db.ComponentInfo;
import com.elsdoerfer.android.autostarts.db.IntentFilterInfo;

import android.Manifest.permission;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewInstalledAppActivity extends android.app.ListActivity {
	private static final String TAG = "NewInstalledAppActivity";
	private ArrayList<ComponentInfo> mReiceivers;
	public Activity mActivity = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    	mReiceivers = NewPackageInstallReceiver.getReceivers();
    	if (mReiceivers == null) {
    		Log.e(TAG, "mReiceivers is null");
    		finish();
    		return;
    	}
    	
    	String receiverNames = "";
    	for (ComponentInfo receiver :mReiceivers) {
    		receiverNames += "receiver-" + receiver.getLabel() + "(";
    		for (IntentFilterInfo action: receiver.intentFilters) {
        		receiverNames += receiver.intentFilters.get(0).action + ",";    			
    		}
    		receiverNames += ")";
    	}
    	
//    	setContentView(R.layout.new_installed_package);
//    	TextView tv = (TextView) findViewById(R.id.textView1);
//    	tv.setText("receivers count =" + mReiceivers.size() + " actions=" + receiverNames);
    	Log.d(TAG, "receivers count =" + mReiceivers.size() + " actions=" + receiverNames);
    	
    	String installedAppLable = mReiceivers.get(0).packageInfo.getLabel();
    	setTitle(String.format(getTitle().toString(), installedAppLable));
    	setListAdapter(new ReceiverListAdapter());
//    	getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//    	for (int i = 0; i < getListAdapter().getCount(); ++i) {
//    		ComponentInfo receiver = (ComponentInfo) getListAdapter().getItem(i);
//    		getListView().setItemChecked(i, receiver.isCurrentlyEnabled());
//    	}
    }
	
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		Log.d(TAG, "in onListItemClick, position=" + position + " checked=" + l.isItemChecked(position));
//		boolean switcher = l.isItemChecked(position);
//		ComponentInfo receiver = (ComponentInfo) getListAdapter().getItem(position);
//		new ComponentTask().execute(receiver, switcher);
//	}
//	
//	protected void toggleComponent(ComponentInfo receiver, ComponentInfo enable) {
//		
//	}
	
	private class ReceiverListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mReiceivers == null ? 0 : mReiceivers.size();
		}

		@Override
		public Object getItem(int position) {
			return mReiceivers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.new_installed_package, null);
			}
			
			final ComponentInfo receiver = (ComponentInfo) getItem(position);
			
			TextView titleView = (TextView) convertView.findViewById(R.id.label);
			titleView.setText(receiver.getLabel());

			ImageView iconView = (ImageView) convertView.findViewById(R.id.receiver_icon);
			iconView.setImageDrawable(receiver.packageInfo.icon);
			
			SpannableStringBuilder spanString = new SpannableStringBuilder();			
			int index = 0;
//			spanString.append(receiverName);
//			spanString.setSpan(new ForegroundColorSpan(Color.WHITE), index, spanString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			
//			spanString.append("\n");
//			List<String> actionLables = new ArrayList<String>();
			for (IntentFilterInfo action: receiver.intentFilters) {
				Object[] actionDetail = Actions.MAP.get(action.action);
				if (actionDetail != null) {
					String actionLable = getString((Integer) actionDetail[1]);
					actionLable = (actionLable != null) ? actionLable : action.action;
//					actionLables.add(actionLable);
					index = spanString.length();
					spanString.append("·\t" + actionLable + "\n");
					spanString.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC) , index, spanString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
					if (!receiver.isCurrentlyEnabled()) {
						spanString.setSpan(new StrikethroughSpan() , index, spanString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
					}
				}
			}
			if (spanString.length() > 0) {
				spanString = (SpannableStringBuilder) spanString.subSequence(0, spanString.length()-1);				
			}
			TextView actionsView = (TextView) convertView.findViewById(R.id.actions);
			actionsView.setText(spanString);
			
			CompoundButton checkableView = (CompoundButton) convertView.findViewById(R.id.checkable);
			checkableView.setChecked(receiver.isCurrentlyEnabled());
			checkableView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					new ComponentTask().execute(receiver, isChecked);	//TODO: may be the wrong state will be occur when user arouse the second click before the first task has been done. 
				}
			});
//			((ListView)parent).setItemChecked(position, receiver.isCurrentlyEnabled());
			
			return convertView;
		}
		
	}
	
	
	public class ComponentTask extends AsyncTask<Object, Object, Boolean> {
		private ComponentInfo mComponent;


//		private ProgressDialog mPg;


		@Override
		protected void onPreExecute() {
//			mPg = new ProgressDialog(mActivity);
//			mPg.setIndeterminate(true);
//			mPg.setMessage(mActivity.getResources().getString(R.string.please_wait));
//			mPg.setCancelable(false);
//			mPg.show();
			setProgressBarIndeterminateVisibility(true);

		}

		@Override
		protected Boolean doInBackground(Object... params) {
			final Activity activity = mActivity;

			mComponent = (ComponentInfo) params[0];
			// We could also read this right now, but we want to ensure
			// we always do the state change that we announced to the user
			// through the menu item caption (it's unlikely but possible
			// that the component state changed in the background while
			// the user decided what to do).
			Boolean doEnable = (Boolean) params[1];

			Log.i(TAG, "Asking package manger to "
					+ "change component state to "
					+ (doEnable ? "enabled" : "disabled"));

			// As described above, in the rare case we are allowed to use
			// setComponentEnabledSetting(), we should do so.
			if (activity
					.checkCallingOrSelfPermission(permission.CHANGE_COMPONENT_ENABLED_STATE) == PackageManager.PERMISSION_GRANTED) {
				return Utils.setConponentEnable(activity, mComponent, doEnable);
			} else {
				return Utils.setConponentEnableByRoot(activity, mComponent, doEnable);
			}
		}


		@Override
		protected void onPostExecute(Boolean result) {
//			if (mPg != null)
//				mPg.cancel();
			setProgressBarIndeterminateVisibility(false);
			((BaseAdapter)getListAdapter()).notifyDataSetChanged();

			if (!result) {
				Toast.makeText(mActivity, R.string.state_change_failed, Toast.LENGTH_SHORT).show();	
			} else {
				Log.i(TAG, mComponent.getLabel() + " changed state to " + mComponent.isCurrentlyEnabled());
			}
			
		}
	}
}
