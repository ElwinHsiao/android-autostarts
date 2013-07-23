/**
 *
 */
package com.elsdoerfer.android.autostarts;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.drm.DrmStore.Action;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.elsdoerfer.android.autostarts.db.ComponentInfo;
import com.elsdoerfer.android.autostarts.db.IntentFilterInfo;
import com.elsdoerfer.android.autostarts.db.PackageInfo;

/**
 * ListAdapter used by the ListActivity. Has it's own top-level file to keep
 * file sizes small.
 */
public class MyExpandableListAdapter extends BaseExpandableListAdapter {

	static final public int GROUP_BY_ACTION = 0;
	static final public int GROUP_BY_PACKAGE = 1;
	
	public static final int FILTER_HIDE_SYSTEM_APP = 				0x00000001; 
	public static final int FILTER_HIDE_UNKNOWN_EVENTS = 			0x00000010;
	public static final int FILTER_HIDE_DISABLED_COMPONENENT = 	0x00000100;
	public static final int FILTER_HIDE_ENABLED_COMPONENENT = 	0x00001000;
	public static final int FILTER_SHOW_CHANGED_ONLY = 			0x00010000;
	

	private ListActivity mActivity;
	private ArrayList<IntentFilterInfo> mDataAll = new ArrayList<IntentFilterInfo>();
	private GroupingImpl mGroupDisplay;
	private int mCurrentGrouping;

//	private boolean mHideSystemApps = false;
//	private boolean mHideUnknownEvents = false;
//	private boolean mShowChangedOnly = false;

	private LayoutInflater mInflater;

	public MyExpandableListAdapter(ListActivity activity, int grouping) {
		mActivity = activity;
		mCurrentGrouping = grouping;
		mInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setData(new ArrayList<IntentFilterInfo>());
	}

	public void setData(ArrayList<IntentFilterInfo> data) {
		mDataAll = data;
		rebuildGroupDisplay(mCurrentGrouping);
	}
	

	public void addData(IntentFilterInfo info) {
		mDataAll.add(info);
		mGroupDisplay.addData(info);
	}

	public void setGroupMode(int groupMode) {
		if (mCurrentGrouping != groupMode) {
			mCurrentGrouping = groupMode;
			rebuildGroupDisplay();
		}
	}

	public int getGrouping() {
		return mCurrentGrouping;
	}

	private boolean checkAgainstFilters(IntentFilterInfo info) {
		ComponentInfo comp = info.componentInfo;
		if (isFilterHideSystemApps() && comp.packageInfo.isSystem)
			return false;
		if (isFilterShowChangedOnly()
				&& comp.isCurrentlyEnabled() == comp.defaultEnabled)
			return false;
		if (isFilterHideUnknownEvents() && !Actions.MAP.containsKey(info.action))
			return false;
		if (isFilterHideDisabled() && !comp.isCurrentlyEnabled())
			return false;
		if (isFilterHideEnabled() && comp.isCurrentlyEnabled())
			return false;
		return true;
	};

	private WeakReference<GroupByActionImpl> mGroupByActionImpl;
	private WeakReference<GroupByPackageImpl> mGroupByPackageImpl;
	
	private void rebuildGroupDisplay(int group) {
		switch (group) {
		case GROUP_BY_ACTION:
			mGroupDisplay = new GroupByActionImpl(mDataAll, this);
			break;
		case GROUP_BY_PACKAGE:
			mGroupDisplay = new GroupByPackageImpl(mDataAll, this);
			break;
		}
	}
	
	/**
	 * Rebuild the grouping-mode specific rendering object. This re-applies the
	 * filters.
	 * 
	 * TODO: Add a way to init all filters (setFilterFOO calls) without updating
	 * the data once for every filter option. Simplest way: generate this on
	 * demand?
	 */
	private void rebuildGroupDisplay() {
		rebuildGroupDisplay(mCurrentGrouping);
	}

	public Object getChild(int groupPosition, int childPosition) {
		return mGroupDisplay.getChild(groupPosition, childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return mGroupDisplay.getChildId(groupPosition, childPosition);
	}

	public int getChildrenCount(int groupPosition) {
		return mGroupDisplay.getChildrenCount(groupPosition);
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		return mGroupDisplay.getChildView(groupPosition, childPosition,
				isLastChild, convertView, parent);
	}

	public Object getGroup(int groupPosition) {
		return mGroupDisplay.getGroup(groupPosition);
	}

	public int getGroupCount() {
		return mGroupDisplay.getGroupCount();
	}

	public long getGroupId(int groupPosition) {
		return mGroupDisplay.getGroupId(groupPosition);
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		return mGroupDisplay.getGroupView(groupPosition, isExpanded,
				convertView, parent);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	private int mFilter;
	public void setFilter(int filter) {
		if (filter != mFilter) {
			mFilter = filter;
			rebuildGroupDisplay(mCurrentGrouping);
		}
	}
	
	public int getFilter() {
		return mFilter;
	}
	
	/**
	 * Return true if any filters are active.
	 */
	public boolean isFiltered() {
//		return mHideSystemApps || mShowChangedOnly || mHideUnknownEvents || isHideDisabled || isHideEnabled;
		return mFilter > 0;
	}

	/**
	 * Allow owner to hide (and show) the system applications.
	 * 
	 * Returns True if the list is filtered.
	 * 
	 * Expects the caller to also call notifyDataSetChanged(), if necessary.
	 */
//	public boolean toggleFilterSystemApps() {
//		setFilterSystemApps(!mHideSystemApps);
//		return mHideSystemApps;
//	}

	/**
	 * Manually decide whether to filter out system applications.
	 * 
	 * Expects the caller to also call notifyDataSetChanged(), if necessary.
	 */
	public void setFilterSystemApps(boolean newState) {
//		if (newState != mHideSystemApps) {
//			mHideSystemApps = newState;
//			rebuildGroupDisplay(mCurrentGrouping, true);
//		}
		if (newState) {
			setFilter(getFilter() | FILTER_HIDE_SYSTEM_APP);			
		} else {
			setFilter(getFilter() & (~FILTER_HIDE_SYSTEM_APP));
		}
	}

	public boolean isFilterHideSystemApps() {
//		return mHideSystemApps;
		return (mFilter & FILTER_HIDE_SYSTEM_APP) > 0;
	}

	public void setFilterShowChangedOnly(boolean newState) {
//		if (newState != mShowChangedOnly) {
//			mShowChangedOnly = newState;
//			rebuildGroupDisplay(mCurrentGrouping, true);
//		}
		if (newState) {
			setFilter(getFilter() | FILTER_SHOW_CHANGED_ONLY);			
		} else {
			setFilter(getFilter() & (~FILTER_SHOW_CHANGED_ONLY));
		}
	}

	public boolean isFilterShowChangedOnly() {
//		return mShowChangedOnly;
		return (mFilter & FILTER_SHOW_CHANGED_ONLY) > 0;
	}

	public void setFilterHideUnknownEvents(boolean newState) {
//		if (newState != mHideUnknownEvents) {
//			mHideUnknownEvents = newState;
//			rebuildGroupDisplay(mCurrentGrouping, true);
//		}
		if (newState) {
			setFilter(getFilter() | FILTER_HIDE_UNKNOWN_EVENTS);			
		} else {
			setFilter(getFilter() & (~FILTER_HIDE_UNKNOWN_EVENTS));
		}
	}

	public boolean isFilterHideUnknownEvents() {
//		return mHideUnknownEvents;
		return (mFilter & FILTER_HIDE_UNKNOWN_EVENTS) > 0;
	}
	
//	private boolean isHideDisabled;
	public void setFilterHideDisabled(boolean newState) {
//		if (this.isHideDisabled != isShowEnableOnly) {
//			this.isHideDisabled = isShowEnableOnly;
//			rebuildGroupDisplay(mCurrentGrouping, true);
//		}
		if (newState) {
			setFilter(getFilter() | FILTER_HIDE_DISABLED_COMPONENENT);			
		} else {
			setFilter(getFilter() & (~FILTER_HIDE_DISABLED_COMPONENENT));
		}
	}

	public boolean isFilterHideDisabled() {
//		return isHideDisabled;
		return (mFilter & FILTER_HIDE_DISABLED_COMPONENENT) > 0;
	}
	
//	private boolean isHideEnabled;
	public void setFilterHideEnabled(boolean newState) {
//		if (this.isHideEnabled != isShowDisableOnly) {
//			this.isHideEnabled = isShowDisableOnly;	
//			rebuildGroupDisplay(mCurrentGrouping, true);
//		}
		if (newState) {
			setFilter(getFilter() | FILTER_HIDE_ENABLED_COMPONENENT);			
		} else {
			setFilter(getFilter() & (~FILTER_HIDE_ENABLED_COMPONENENT));
		}
	}

	public boolean isFilterHideEnabled() {
//		return isHideEnabled;
		return (mFilter & FILTER_HIDE_ENABLED_COMPONENENT) > 0;
	}

	@SuppressWarnings("serial")
	static class MapOfIntents<K> extends
			HashMap<K, ArrayList<IntentFilterInfo>> {
		/**
		 * Simplified put() that will automatically create the list object that
		 * is the TreeMap value, and appends to that list.
		 */
		public K put(K key, IntentFilterInfo value) {
			if (!this.containsKey(key)) {
				this.put(key, new ArrayList<IntentFilterInfo>());
			}
			this.get(key).add(value);
			return key;
		}
	}

	/**
	 * Abstract a "group view". We want to allow our data be be shown in
	 * different group modes: group by package, or group by action.
	 * 
	 * Rather than using two ExpandableListAdapter implementations (where we
	 * would have to keep the applied filter options etc. in sync), we instead
	 * use a single adapter and abstracting out the code that is specific to a
	 * grouping mode.
	 * 
	 */
	static private abstract class GroupingImpl {
		private static final String TAG = "MyExpandableListAdapter";
		
		public abstract View getGroupView(int groupPosition,
				boolean isExpanded, View convertView, ViewGroup parent);

		public abstract View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent);
		
		public abstract void setData(ArrayList<IntentFilterInfo> data);		
		public abstract void addData(IntentFilterInfo info);
		
		GroupingImpl(MyExpandableListAdapter parent) {
			mParent = parent;
		}
	
		protected MyExpandableListAdapter mParent;

		protected MyExpandableListAdapter getParent() {
			return mParent;
		}
		
		private ArrayList<Object> mGroups = new ArrayList<Object>();
		protected ArrayList<Object> getGroups() {
			return mGroups;
		}
		
		private MapOfIntents<Object> mChildren = new MapOfIntents<Object>();
		protected MapOfIntents<Object> getChildren() {
			return mChildren;
		}

		public int getGroupCount() {
			return mGroups.size();
		}

		public Object getGroup(int groupPosition) {
			return mGroups.get(groupPosition);
		}

		public long getGroupId(int groupPosition) {
			return getGroup(groupPosition).hashCode();
		}

		public int getChildrenCount(int groupPosition) {
			Object key = getGroup(groupPosition);
			ArrayList<IntentFilterInfo> child = mChildren.get(key);
			if (child == null) {
				return 0;
			}
			return child.size();
		}

		public long getChildId(int groupPosition, int childPosition) {
			return getChild(groupPosition, childPosition).hashCode();
		}

		public Object getChild(int groupPosition, int childPosition) {
			return mChildren.get(getGroup(groupPosition)).get(childPosition);
		}



		/**
		 * Helper for child classes to return a view for a list item.
		 * 
		 * If "existing" has a tag that matches "tag", it will be re-used (this
		 * is necessary due to different grouping modes using different
		 * layouts). Otherwise, a new view is created based on "layout", as a
		 * child of "parent".
		 */
		protected View getView(View existing, String tag, int layout,
				ViewGroup parent) {
			if (existing == null || !existing.getTag().equals(tag)) {
				// Log.d(TAG, "in GroupingImpl-getView(), new a view, existing "
				// + (existing == null?"=":"!") + "= null");
				existing = mParent.mInflater.inflate(layout, parent, false);
				existing.setTag(tag);
				/*if (tag.contains("child")) {
					CompoundButton compoundButton = new CheckBox(getParent().mActivity);
					compoundButton.setGravity(Gravity.RIGHT);
					compoundButton.setFocusable(false);
					compoundButton.setId(3);
					ViewGroup group = (ViewGroup) existing; 
					group.addView(compoundButton);
				}*/
			}

			return existing;
		}
		
		protected void setupSwitch(View v, final ComponentInfo componentInfo) {
			CompoundButton compoundButton = (CompoundButton) v.findViewById(3);
			if (compoundButton == null) {
				return;
			}
			compoundButton.setChecked(componentInfo.isCurrentlyEnabled());
			compoundButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked == componentInfo.isCurrentlyEnabled()) {
						Log.e(TAG, "inconsistent state between view and data");
						return;
					}
					new ToggleTask(getParent().mActivity).execute(componentInfo, !componentInfo.isCurrentlyEnabled());
				}
			});
		}


		/**
		 * Helper for child classes to initialize a "show info" button that
		 * would display information about am event.
		 */
		protected void setActionInfo(View root, final String action) {
			View v = root.findViewById(R.id.show_info);
			if (!Actions.MAP.containsKey(action))
				v.setVisibility(View.GONE);
			else {
				v.setOnClickListener(new OnClickListener() {
					public void onClick(View _v) {
						mParent.mActivity.showInfoToast(action);
					}
				});
			}
		}

		/**
		 * Helper to set the text style for a list item based on whether it
		 * represents a system app, something that disabled etc.
		 * 
		 * Arguments can be null.
		 */
		protected void setTextStyle(TextView t, PackageInfo pkg,
				ComponentInfo comp) {
			if (pkg != null && pkg.isSystem)
				t.setTextColor(Color.YELLOW);
			else
				t.setTextColor(mParent.mActivity.getResources().getColor(
						android.R.color.primary_text_dark));

			if (comp != null
					&& comp.isCurrentlyEnabled() != comp.defaultEnabled)
				t.setTypeface(Typeface.DEFAULT_BOLD);
			else
				t.setTypeface(Typeface.DEFAULT);
		}

		/**
		 * Helper for child classes to set the text of an item that represents a
		 * component. This adds the component label to "base" in the rare case
		 * one exists, and also makes sure to strike the text if the component
		 * is disabled.
		 */
		protected void setComponentText(TextView t, ComponentInfo comp,
				String base) {
			SpannableStringBuilder fullText = new SpannableStringBuilder();
			fullText.append(base);
			if (comp.componentLabel != null && !comp.componentLabel.equals(""))
				fullText.append(" (" + comp.componentLabel + ")");
			if (!comp.isCurrentlyEnabled()) {
				fullText.setSpan(new StrikethroughSpan(), 0, fullText.length(),0);
				fullText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, fullText.length(), 0);
			}

			t.setText(fullText);
		}
	}

	/**
	 * Group by Action.
	 */
	static private class GroupByActionImpl extends GroupingImpl {

		ArrayList<Object> mGroups;
		MapOfIntents<Object> mChildren;

		GroupByActionImpl(ArrayList<IntentFilterInfo> data,
				MyExpandableListAdapter adapter) {
			super(adapter);

			mGroups = getGroups();
			mChildren = getChildren();
			
			addStandardActions();

			setData(data);
		}
		
		public GroupByActionImpl(MyExpandableListAdapter parent) {
			super(parent);
			mGroups = getGroups();
			mChildren = getChildren();
			
			addStandardActions();
		}

		private void addStandardActions() {
			for(Object[] action: Actions.ALL) {
				mGroups.add((String) action[0]);
			}
		}

		@Override
		public void setData(ArrayList<IntentFilterInfo> data) {
			for (IntentFilterInfo info : data) {
				addData(info);
			}

			// Sort by order of actions in our known action database.
//			Collections.sort(mGroups, new Comparator<String>() {
//				public int compare(String action1, String action2) {
//					return Actions.compare(action1, action2);
//				}
//			});
			// Sort children by descending priority
//			for (ArrayList<IntentFilterInfo> group : mChildren.values()) {
//				Collections.sort(group, new Comparator<IntentFilterInfo>() {
//					@Override
//					public int compare(IntentFilterInfo object1,
//							IntentFilterInfo object2) {
//						return -Float.compare(object1.priority,
//								object2.priority);
//					}
//				});
//			}
		}
		
		@Override
		public void addData(IntentFilterInfo info) {
			if (getParent().checkAgainstFilters(info)) {
				if (!mGroups.contains(info.action))		// TODO: orderly HashSet
					mGroups.add(info.action);
				mChildren.put(info.action, info);
			}
		}
		
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			String action = (String) getGroup(groupPosition);

			View v = getView(convertView, "act-group",
					R.layout.by_act_group_row, parent);
			setActionInfo(v, action);

			((TextView) v.findViewById(R.id.title)).setText(mParent.mActivity
					.getIntentName(action) + "(" + getChildrenCount(groupPosition) + ")");

			return v;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View v = getView(convertView, "act-child",
					R.layout.by_act_child_row, parent);

			IntentFilterInfo info = (IntentFilterInfo) getChild(groupPosition,
					childPosition);
			ComponentInfo comp = info.componentInfo;

			// Set the icon
			ImageView img = ((ImageView) v.findViewById(R.id.icon));
			img.setImageDrawable(comp.packageInfo.icon);

			// Set the text
			TextView title = ((TextView) v.findViewById(R.id.title));
			setTextStyle(title, comp.packageInfo, comp);
			setComponentText(title, comp, comp.getLabel());
			
			setupSwitch(v, info.componentInfo);

			return v;
		}


	}

	/**
	 * Group events by package.
	 */
	static private class GroupByPackageImpl extends GroupingImpl {

		ArrayList<Object> mGroups;
		MapOfIntents<Object> mChildren;

		public GroupByPackageImpl(ArrayList<IntentFilterInfo> data,
				MyExpandableListAdapter adapter) {
			super(adapter);
			setData(data);
		}

		@Override
		public void setData(ArrayList<IntentFilterInfo> data) {
			mGroups = getGroups();
			mChildren = getChildren();

			for (IntentFilterInfo info : data) {
				addData(info);
			}
		}
		
		@Override
		public void addData(IntentFilterInfo info) {
			if (getParent().checkAgainstFilters(info)) {
				if (!mGroups.contains(info.componentInfo.packageInfo))
					mGroups.add(info.componentInfo.packageInfo);
				mChildren.put(info.componentInfo.packageInfo, info);
			}
			
			// Sort groups alphabetically
//			Collections.sort(mGroups, new Comparator<Object>() {
//				@Override
//				public int compare(Object object1, Object object2) {
//					return -(int) (((PackageInfo)object1).lastUpdateTime - ((PackageInfo)object2).lastUpdateTime);
//				}
//			});
//			// Sort children by our action ordering.
//			for (ArrayList<IntentFilterInfo> group : mChildren.values()) {
//				Collections.sort(group, new Comparator<IntentFilterInfo>() {
//					@Override
//					public int compare(IntentFilterInfo object1,
//							IntentFilterInfo object2) {
//						return Actions.compare(object1.action, object2.action);
//					}
//				});
//			}
		}


		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			PackageInfo pkg = (PackageInfo) getGroup(groupPosition);
			View v = getView(convertView, "pkg-group",
					R.layout.by_pkg_group_row, parent);

			// Set the icon
			ImageView img = ((ImageView) v.findViewById(R.id.icon));
			img.setImageDrawable(pkg.icon);

			// Set the text (app name)
			TextView textView = (TextView) v.findViewById(R.id.title);
			textView.setText(pkg.getLabel() + "(" + getChildrenCount(groupPosition) + ")");
			setTextStyle(textView, pkg, null);

			return v;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			IntentFilterInfo info = (IntentFilterInfo) getChild(groupPosition,
					childPosition);

			View v = getView(convertView, "pkg-child",
					R.layout.by_pkg_child_row, parent);
			setActionInfo(v, info.action);

			TextView text = ((TextView) v.findViewById(R.id.title));
			setTextStyle(text, null, info.componentInfo);
			setComponentText(text, info.componentInfo,
					mParent.mActivity.getIntentName(info.action));
			
			setupSwitch(v, info.componentInfo);

			return v;
		}
	}


}