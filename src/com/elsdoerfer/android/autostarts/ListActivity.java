package com.elsdoerfer.android.autostarts;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elsdoerfer.android.autostarts.compat.ExpandableListFragmentActivity;
import com.elsdoerfer.android.autostarts.db.IntentFilterInfo;

public class ListActivity extends ExpandableListFragmentActivity {

	static final String TAG = "Autostarts-ListActivity";
	static final Boolean LOGV = false;

	static final private int MENU_VIEW = 1;
	static final private int MENU_EXPAND_COLLAPSE = 2;
	static final private int MENU_RELOAD = 3;
	static final private int MENU_HELP = 4;
	static final private int MENU_GROUPING = 5;

	static final protected int DIALOG_CONFIRM_SYSAPP_CHANGE = 2;
	static final private int DIALOG_VIEW_OPTIONS = 4;
	static final protected int DIALOG_CONFIRM_GOOGLE_TALK_WARNING = 6;
	static final int DIALOG_STATE_CHANGE_FAILED = 7;

	static final private String PREFS_NAME = "common";
	static final private String PREF_FILTER_KEY = "filter";
	static final private String PREF_GROUPING_KEY = "grouping";
//	static final private String PREF_FILTER_SYS_APPS = "filter-sys-apps";
//	static final private String PREF_FILTER_SHOW_CHANGED = "show-changed-only";
//	static final private String PREF_FILTER_UNKNOWN = "filter-unknown-events";
//	protected static final String PREF_FILTER_HIDE_DISABLED = "show-enable-only";
//	protected static final String PREF_FILTER_HIDE_ENABLED = "show-disable-only";

	private MenuItem mExpandCollapseToggleItem;
	private MenuItem mGroupingModeItem;
	MenuItem mReloadItem;
	private Toast mInfoToast;

	MyExpandableListAdapter mListAdapter;
	ArrayList<IntentFilterInfo> mEvents;
//	private DatabaseHelper mDb;
	private SharedPreferences mPrefs;
	private Boolean mExpandSuggested = true;
	protected ToggleTask mToggleTask;
	private LoadTask mLoadTask;

	// Due to Android deficiencies (can't pass data to showDialog()),
	// we need to store that data globally.
	// TODO: I believe Honeycomb has a solution for this; Fix once
	// it's feasible for us to require it.
	private IntentFilterInfo mLastSelectedEvent;
	protected boolean mLastChangeRequestDoEnable;
	protected boolean mUninstallWarningShown;

	@Override
	public void onCreate(final Bundle saved) {
		super.onCreate(saved);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.list);

		// Set everything up.
		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		int grouping = mPrefs.getInt(PREF_GROUPING_KEY, MyExpandableListAdapter.GROUP_BY_ACTION);
		int filter = mPrefs.getInt(PREF_FILTER_KEY, MyExpandableListAdapter.FILTER_HIDE_SYSTEM_APP|MyExpandableListAdapter.FILTER_HIDE_UNKNOWN_EVENTS);
//		mDb = new DatabaseHelper(this);
		// This is just to workaround "Can't upgrade read-only database..."
		// exceptions, when an upgrade is necessary.
//		mDb.getWritableDatabase().close();
		mListAdapter = new MyExpandableListAdapter(this, grouping);
		mListAdapter.setFilter(filter);
		setListAdapter(mListAdapter);
		
		getExpandableListView().setItemsCanFocus(false);
		

		// Restore preferences
//		mListAdapter.setFilterSystemApps(
//				mPrefs.getBoolean(PREF_FILTER_SYS_APPS, false));
//		mListAdapter.setFilterShowChangedOnly(
//				mPrefs.getBoolean(PREF_FILTER_SHOW_CHANGED, false));
//		mListAdapter.setFilterHideUnknownEvents(
//				mPrefs.getBoolean(PREF_FILTER_UNKNOWN, true));
//		mListAdapter.setFilterHideDisabled(
//				mPrefs.getBoolean(PREF_FILTER_HIDE_DISABLED, false));
//		mListAdapter.setFilterHideEnabled(
//				mPrefs.getBoolean(PREF_FILTER_HIDE_ENABLED, false));
//		mListAdapter.setGroupMode(
//				? MyExpandableListAdapter.GROUP_BY_PACKAGE
//				: MyExpandableListAdapter.GROUP_BY_ACTION);

		// Init/restore retained and instance data. If we have data
		// retained, we can speed things up significantly by not having
		// to do a load.
		Object retained = getLastCustomNonConfigurationInstance();
		if (retained != null) {
			// Be careful not to copy any objects that reference the
			// activity itself, or we would leak it!
			ListActivity oldActivity = (ListActivity) retained;
			mLastSelectedEvent = oldActivity.mLastSelectedEvent;
			mLastChangeRequestDoEnable = oldActivity.mLastChangeRequestDoEnable;
			mEvents = oldActivity.mEvents;
			mUninstallWarningShown = oldActivity.mUninstallWarningShown;
			mToggleTask = oldActivity.mToggleTask;
			mLoadTask = oldActivity.mLoadTask;
			// Display what we have immediately.
			if (mEvents != null)
				apply();
			// Continue loading in case we're not done yet.
			if (mLoadTask != null)
				mLoadTask.connectTo(this);
			if (mToggleTask != null)
				mToggleTask.connectTo(this);
		}
		// Otherwise, we are going to have to init certain data
		// ourselves, and load some from from instance state, if
		// available.
		else {
			if (saved != null) {
				mLastSelectedEvent = saved.getParcelable("selected-event");
				mLastChangeRequestDoEnable = saved.getBoolean("change-do-enable");
				mUninstallWarningShown = saved.getBoolean("uninstall-warning-shown");
			}
			else { /* here's the place to load some prefs, if necessary */ }

			// Initial load.
			loadAndApply();
		}

		// This depends both on preferences on loading status.
		updateEmptyText();
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		// Retain the full activity; it's just simpler as opposed to
		// copying a bunch of stuff to an array; but we need to be very
		// careful here not to leak it when we restore.
		return this;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Storing the last selected event (needed for dialog persistence)
		// is problematic because saving and loading it as a parcable will
		// break the identify of the ComponentInfo/PackageInfo relations.
		// In other words, the attached ComponentInfo will not be the same
		// object as the one we use to render the list items, so changing
		// the "disabledState" of a mLastSelectedEvent restored in this way
		// will not affect the GUI display. There are different approaches
		// can take to solve this (TODO):
		//   - A complicated scheme where IntentFilterInfo stores the
		//     package and component names as strings, and upon restore
		//     from a bundle will look up the actual ComponentInfo parent
		//     as soon as it becomes available. A kind of delayed-loaded
		//     weak reference.
		//   - Since we're only talking about a single property that is
		//     modified by use (disabled state), this data could be stored
		//     by the Activity as a kind of "overlay", as a list of changes
		//     made by the user. We then would no longer need the
		//     ComponentInfo/PackageInfo relations to have a shared identity.
		//   - The ToggleTask, rather than modifing the ComponentInfo
		//     object, could have the Activity's callback method deal with
		//     the state change. If the component is already loaded, it's
		//     state is changed, otherwise, we can assume it will be loaded
		//     correctly (we can assume? would there be race conditions)?
		//   - We could simply store the list of events globally, bypassing
		//     all these problems.
		outState.putBoolean("change-do-enable", mLastChangeRequestDoEnable);
		outState.putParcelable("selected-event", mLastSelectedEvent);
		// TODO: Note that we do not store the event list. In cases where
		// onRetainNonConfigurationInstance() is not available, we will
		// need to reload all events. It would not be *that* hard to store
		// the events list (of course, this only works if loading is
		// finished): We'd just store a list of PackageInfo parcelables,
		// with all ComponentInfo and IntentFilterInfo children, and upon
		// load would simply have to connect each child with it's parent
		// again. However, in the hope that the new Loader API will allow
		// us to avoid all these problems, we don't bother to implement
		// this now.
		// In case the Loader API should prove to be insufficient, I think
		// we would want to switch to a global state.
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mInfoToast != null)
			mInfoToast.cancel();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "in onDestroy");
//		if (mDb != null)
//			mDb.close();
		// Unattach the activity from a potentially running task.
		if (mToggleTask != null)
			mToggleTask.connectTo(null);
		if (mLoadTask != null)
			mLoadTask.connectTo(null);
		super.onDestroy();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		mGroupingModeItem =
			menu.add(0, MENU_GROUPING, 0, R.string.group_by_action).
			    setIcon(R.drawable.ic_menu_windows);
		menu.add(0, MENU_VIEW, 0, R.string.view_options).
			setIcon(R.drawable.ic_menu_view);
		mExpandCollapseToggleItem =
			menu.add(0, MENU_EXPAND_COLLAPSE, 0, R.string.expand_all).
				setIcon(R.drawable.ic_collapse_expand);
		mReloadItem = menu.add(0, MENU_RELOAD, 0, R.string.reload).
			setIcon(R.drawable.ic_menu_refresh);
		mReloadItem.setEnabled(mLoadTask == null || mLoadTask.getStatus() != AsyncTask.Status.RUNNING);
		menu.add(0, MENU_HELP, 0, R.string.help).
			setIcon(R.drawable.ic_menu_help);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Proper title for the grouping mode toggle item.
		if (mListAdapter.getGrouping() == MyExpandableListAdapter.GROUP_BY_ACTION)
			mGroupingModeItem.setTitle(R.string.group_by_package);
		else
			mGroupingModeItem.setTitle(R.string.group_by_action);
		// Decide whether we want to offer the option to collapse, or
		// expand, depending on the current group expansion count.
		ExpandableListView lv = getExpandableListView();
		int expandCount = 0;
		int numGroups = mListAdapter.getGroupCount();
		for (int i=numGroups-1; i>=0; i--) {
			if (lv.isGroupExpanded(i))
				expandCount += 1;
		}
		if (expandCount / (float)numGroups >= 0.5) {
			mExpandCollapseToggleItem.setTitle(R.string.collapse_all);
			mExpandSuggested = false;
		}
		else {
			mExpandCollapseToggleItem.setTitle(R.string.expand_all);
			mExpandSuggested = true;
		}
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_GROUPING:
			int groupingPref = 1 - mListAdapter.getGrouping();
			mListAdapter.setGroupMode(groupingPref);
			mPrefs.edit().putInt(PREF_GROUPING_KEY, groupingPref).commit();
			mListAdapter.notifyDataSetChanged();
			return true;

		case MENU_VIEW:
			showDialog(DIALOG_VIEW_OPTIONS);
			mListAdapter.notifyDataSetChanged();
			return true;

		case MENU_EXPAND_COLLAPSE:
			ExpandableListView lv = getExpandableListView();
			for (int i=mListAdapter.getGroupCount()-1; i>=0; i--)
				if (mExpandSuggested)
					lv.expandGroup(i);
				else
					lv.collapseGroup(i);
			return true;

		case MENU_RELOAD:
			loadAndApply();
			return true;

		case MENU_HELP:
			startActivity(new Intent(this, HelpActivity.class));
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_CONFIRM_SYSAPP_CHANGE)
		{
			return new AlertDialog.Builder(ListActivity.this)
				.setTitle(R.string.warning)
				.setMessage(R.string.confirm_sys_disable)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mToggleTask = new ToggleTask(ListActivity.this);
						mToggleTask.execute(
							mLastSelectedEvent.componentInfo,
							mLastChangeRequestDoEnable);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create();
		}

		else if (id == DIALOG_CONFIRM_GOOGLE_TALK_WARNING)
		{
			return new AlertDialog.Builder(ListActivity.this)
				.setTitle(R.string.warning)
				.setMessage(R.string.confirm_google_talk)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mToggleTask = new ToggleTask(ListActivity.this);
						mToggleTask.execute(
							mLastSelectedEvent.componentInfo,
							mLastChangeRequestDoEnable);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create();
		}

		else if (id == DIALOG_STATE_CHANGE_FAILED)
		{
			return new AlertDialog.Builder(ListActivity.this)
				.setMessage(R.string.state_change_failed)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.error)
				.setPositiveButton(android.R.string.ok, null).create();
		}

		else if (id == DIALOG_VIEW_OPTIONS)
		{
			// We are just hoping that the state of these vars can
			// never change without going through this dialog;
			// otherwise, we'd need to find a solution to ensure
			// that when the instance is reused for a showing at a
			// later point, that the state is still up-to-date, i.e.
			// setting the current state in onPrepareDialog. It's not
			// clear if making the state array a class member and
			// updating it would be enough, or if we actually have to
			// find the correct views and switch them...
			boolean[] initState = new boolean[] {
				mListAdapter.isFilterHideSystemApps(),
				mListAdapter.isFilterHideUnknownEvents(),
				mListAdapter.isFilterHideDisabled(),
				mListAdapter.isFilterHideEnabled(),
				mListAdapter.isFilterShowChangedOnly(),
			};

			return new AlertDialog.Builder(this)
				.setMultiChoiceItems(new CharSequence[] {
						getString(R.string.hide_sys_apps),
						getString(R.string.hide_unknown),
						getString(R.string.hide_disable_only),
						getString(R.string.hide_enable_only),
						getString(R.string.show_changed_only),
					},
					initState,
					new OnMultiChoiceClickListener() {
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							switch (which) {
							case 0:
								mListAdapter.setFilterSystemApps(isChecked);
								mListAdapter.notifyDataSetChanged();
								updateEmptyText();
//								mPrefs.edit().putBoolean(PREF_FILTER_SYS_APPS, isChecked).commit();
								break;
							case 1:
								mListAdapter.setFilterHideUnknownEvents(isChecked);
								mListAdapter.notifyDataSetChanged();
								updateEmptyText();
//								mPrefs.edit().putBoolean(PREF_FILTER_UNKNOWN, isChecked).commit();
								break;
							case 2:								
								mListAdapter.setFilterHideDisabled(isChecked);
								mListAdapter.notifyDataSetChanged();
								updateEmptyText();
//								mPrefs.edit().putBoolean(PREF_FILTER_HIDE_DISABLED, isChecked).commit();

								break;
							case 3:
								mListAdapter.setFilterHideEnabled(isChecked);
								mListAdapter.notifyDataSetChanged();
								updateEmptyText();
//								mPrefs.edit().putBoolean(PREF_FILTER_HIDE_ENABLED, isChecked).commit();
								break;
							case 4:
								mListAdapter.setFilterShowChangedOnly(isChecked);
								mListAdapter.notifyDataSetChanged();
								updateEmptyText();
//								mPrefs.edit().putBoolean(PREF_FILTER_SHOW_CHANGED, isChecked).commit();
								break;
							}
							mPrefs.edit().putInt(PREF_FILTER_KEY, mListAdapter.getFilter()).commit();

						}

					})
				.setPositiveButton(android.R.string.ok, null).create();
		}

		else
			return super.onCreateDialog(id);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		mLastSelectedEvent =
			(IntentFilterInfo) mListAdapter.getChild(groupPosition, childPosition);
		showEventDetails();
		return super.onChildClick(parent, v, groupPosition, childPosition, id);
	}

	void apply() {
		mListAdapter.setData(mEvents);
		mListAdapter.notifyDataSetChanged();
	}
	
	public void onNewIntentFilterInfo(IntentFilterInfo info) {
		mListAdapter.addData(info);
		mListAdapter.notifyDataSetChanged();
	}
	
	public void onLoadFinished(int state) {
		setProgressBarIndeterminateVisibility(false);
		setProgressBarVisibility(false);
		if (mReloadItem != null)
			mReloadItem.setEnabled(true);
		updateEmptyText(true);
	}

	private void loadAndApply() {
		// Never start two tasks at once.
		if (mLoadTask != null && mLoadTask.getStatus() == AsyncTask.Status.RUNNING)
			return;

		mLoadTask = new LoadTask(this);
		mLoadTask.execute();
	}

	/**
	 * The "loadIsFinished" argument is needed for the call from LoadTask,
	 * because when it's postExecute() handler is run the task status
	 * has not yet been switched to FINISHED (it's still RUNNING).
	 */
	protected void updateEmptyText(boolean loadIsFinished) {
		TextView emptyText = (TextView) findViewById(android.R.id.empty);
		if (mLoadTask != null && mLoadTask.getStatus() == AsyncTask.Status.RUNNING && !loadIsFinished)
			emptyText.setText(R.string.still_loading);
		else if (!mListAdapter.isFiltered())
			emptyText.setText(R.string.no_receivers);
		else {
			// Unfortunately, we cannot link directly from the resource
			// string, and neither can we use {@see Linkify}. In both
			// cases, we end up with a URLSpan instance in the text (the
			// class is apparently hardcoded), which only allows link
			// resolving via Intent. We however want to use our own
			// URLSpan subclass which can trigger an event.
			//
			// We cannot override {@see Linkify} either, really - it
			// makes the one method we could override to easily inject
			// our custom subclass "final" - aaarg.
			CharSequence base = getString(R.string.no_receivers_filtered) +
				"\n\n";
			SpannableString full = new SpannableString(
					base +
					getString(R.string.change_filter_settings)
			);
			full.setSpan(new InternalURLSpan(new OnClickListener() {
					public void onClick(View v) {
						showDialog(DIALOG_VIEW_OPTIONS);
					}
				}), base.length(), full.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			emptyText.setText(full);

			// Copied from "Linkify.addLinkMovementMethod" (which is private).
			MovementMethod m = emptyText.getMovementMethod();
			if ((m == null) || !(m instanceof LinkMovementMethod)) {
				if (emptyText.getLinksClickable()) {
					emptyText.setMovementMethod(LinkMovementMethod.getInstance());
				}
			}
		}
	}

	protected void updateEmptyText() {
		updateEmptyText(false);
	}

	protected void showEventDetails() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("details");
		if (prev != null)
			ft.remove(prev);
		ft.addToBackStack(null);

		DialogFragment newFragment = EventDetailsFragment.newInstance(mLastSelectedEvent);
		newFragment.show(ft, "details");
	}

	// TODO: Instead of showing a toast, fade in a custom info bar, then
	// fade out. This would be an improvement because we could control
	// it better: Show it longer, but have it disappear when the user
	// clicks on it (toasts don't receive clicks).
	public void showInfoToast(String action) {
		Object[] data = Actions.MAP.get(action);
		if (mInfoToast == null) {
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.detail_toast,
					(ViewGroup) findViewById(R.id.root));
			mInfoToast = new Toast(this);
			mInfoToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
			mInfoToast.setDuration(Toast.LENGTH_LONG);
			mInfoToast.setView(layout);
		}
		((TextView)mInfoToast.getView().findViewById(R.id.title)).setText(getIntentName(action));
		TextView message = ((TextView)mInfoToast.getView().findViewById(android.R.id.message));
		CharSequence info = "";
		if (data == null) {
			message.setVisibility(View.GONE);
		}
		else {
			if (data[2] != null)  // Hide info text both for null and empty string values.
				info = getResources().getText((Integer)data[2]);
			if (!info.equals("")) {
				message.setText(info);
				message.setVisibility(View.VISIBLE);
			} else {
				message.setVisibility(View.GONE);
			}
		}
		mInfoToast.show();
	}

	/**
	 * Return a name for the given intent; tries the pretty name,
	 * if available, and falls back to the raw class name.
	 */
	String getIntentName(String action) {
		Object[] data = Actions.MAP.get(action);
		if (data == null)
			return action;
		return (data[1] != null) ?
				getResources().getString((Integer)data[1]) :
				(String)data[0];
	}

	/**
	 * URLSpan class that supports a custom onClick listener, rather than
	 * sending everything through an Intent.
	 */
	static class InternalURLSpan extends ClickableSpan {
		OnClickListener mListener;

		public InternalURLSpan(OnClickListener listener) {
			mListener = listener;
		}

		@Override
		public void onClick(View widget) {
			mListener.onClick(widget);
		}
	}
}