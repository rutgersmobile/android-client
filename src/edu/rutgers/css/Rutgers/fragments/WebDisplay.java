package edu.rutgers.css.Rutgers.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ShareActionProvider;
import edu.rutgers.css.Rutgers2.R;

public class WebDisplay extends Fragment {

	private static final String TAG = "WebDisplay";
	
	private ShareActionProvider mShareActionProvider;
	
	public WebDisplay() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_web_display, container, false);
		WebView webView = (WebView) v.findViewById(R.id.webView);
		
		Bundle args = getArguments();
		if(args.getString("title") != null) {
			getActivity().setTitle(args.getString("title"));
		}
		
		if(args.getString("url") == null) {
			Log.w(TAG, "No URL supplied");
			// TODO Display failed load message
			String msg = getActivity().getResources().getString(R.string.failed_load);
			webView.loadData(msg, "text/plain", null);
			return v;
		}
		
		webView.getSettings().setJavaScriptEnabled(false);
		final Activity mainActivity = getActivity();
		
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				mainActivity.setProgress(progress * 1000);
			}
		});
		
		setShareIntent(args.getString("url"));
		webView.loadUrl(args.getString("url"));
		
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		getActivity().getMenuInflater().inflate(R.menu.web_menu, menu);
		
		MenuItem shareItem = menu.findItem(R.id.action_share);
		mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
	}
	
	/**
	 * Create intent for sharing a link
	 * @param url URL string
	 */
	private void setShareIntent(String url) {
		if(mShareActionProvider != null) {	
			Intent intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, url);
			mShareActionProvider.setShareIntent(intent);
		}
		else {
			Log.w(TAG, "Tried to set intent before action provider");
		}
	}
	
}