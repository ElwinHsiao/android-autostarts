package com.elsdoerfer.android.autostarts;

import java.util.ArrayList;

import com.elsdoerfer.android.autostarts.ReceiverReader.OnLoadProgressListener;
import com.elsdoerfer.android.autostarts.db.ComponentInfo;
import com.elsdoerfer.android.autostarts.db.IntentFilterInfo;


// TODO: We could speed this up (probably not too much) by returning
// only the newly found apps during progress report. I don't think
// the list itself cares when notifyDatasetChanged() is called, but
// at least we don't need to re-filter the whole list on every progress
// report, but can only apply the filter to what comes in new.
class LoadTask extends ActivityAsyncTask<ListActivity, Object, Object,
    ArrayList<IntentFilterInfo>> {

	Integer mCurrentProgress = 0;

	public LoadTask(ListActivity initialConnect) {
		super(initialConnect);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWrapped.setProgressBarIndeterminateVisibility(true);
		// Note that we have the current progress remembered (there is
		// no getProgress() apparently), and we need it so that when a
		// new Activity connects after an orientation change, we can
		// display the current progress right away, rather than it taking
		// the time until the next publishProgress() call before we update
		// the progress bar.
		mWrapped.setProgress(mCurrentProgress);
		mWrapped.setProgressBarVisibility(true);
		if (mWrapped.mReloadItem != null)
			mWrapped.mReloadItem.setEnabled(false);
		mWrapped.updateEmptyText();
	}

	@Override
	protected ArrayList<IntentFilterInfo> doInBackground(Object... params) {
		ReceiverReader reader = new ReceiverReader(mWrapped, new OnLoadProgressListener() {

			@Override
			public void onProgress(float progress) {
				publishProgress(progress);
			}

			@Override
			public void onNewIntentFilterInfo(IntentFilterInfo info) {
				publishProgress(info);
			}

			@Override
			public void onNewReceiver(ComponentInfo receiver) {
				// TODO Auto-generated method stub
				
			}
			
			
		});
		return reader.load();
	}

	@Override
	protected void processPostExecute(ArrayList<IntentFilterInfo> result) {
//		mWrapped.mEvents = result;
//		mWrapped.apply();


		mWrapped.onLoadFinished(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onProgressUpdate(Object... values) {
		if (mWrapped != null) {
			if (values[0] instanceof Float) {
				mCurrentProgress = (int)(((Float)values[0])*10000);
				mWrapped.setProgress(mCurrentProgress);
			} else if (values[0] instanceof IntentFilterInfo) {
//				mWrapped.mEvents = (ArrayList<IntentFilterInfo>)values[0];				
//				mWrapped.apply();
				mWrapped.onNewIntentFilterInfo((IntentFilterInfo) values[0]);
			}
			
		}
	}
}