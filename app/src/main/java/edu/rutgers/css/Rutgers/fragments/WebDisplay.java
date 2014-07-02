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
import android.webkit.WebViewClient;
import android.widget.ShareActionProvider;
import edu.rutgers.css.Rutgers2.R;

public class WebDisplay extends Fragment {

	private static final String TAG = "WebDisplay";
	
	private ShareActionProvider mShareActionProvider;
	private String mCurrentURL;
	private WebView mWebView;
	private int setupCount = 0;

	public WebDisplay() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		Bundle args = getArguments();
		if(args.getString("title") != null) {
			getActivity().setTitle(args.getString("title"));
		}
		
		if(args.getString("url") == null) {
			Log.w(TAG, "No URL supplied");
			// TODO Display failed load message
		}
		else {
			mCurrentURL = args.getString("url");
			initialIntent();
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_web_display, container, false);
		mWebView = (WebView) v.findViewById(R.id.webView);
		
		if(mCurrentURL == null) {
			String msg = getActivity().getResources().getString(R.string.failed_load);
			mWebView.loadData(msg, "text/plain", null);
			return v;
		}
		
		mWebView.getSettings().setJavaScriptEnabled(true); // XSS Warning
		final Activity mainActivity = getActivity();
		
		// Progress bar
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				mainActivity.setProgress(progress * 1000);
			}
		});
		
		// Intercept URL loads so it doesn't pop to external browser
		mWebView.setWebViewClient(new WebViewClient() {
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    	mCurrentURL = url;
		        mWebView.loadUrl(mCurrentURL);
		        setShareIntent(mCurrentURL);
		        return true;
		    }
		});
		
		mWebView.loadUrl(mCurrentURL);
		
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.web_menu, menu);

		MenuItem shareItem = menu.findItem(R.id.action_share);
		if(shareItem != null) {
			mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
			initialIntent();
		}
		else {
			Log.w(TAG, "Could not find Share menu item");
		}
	}
	
	/**
	 * Set up the first Share intent only after the URL and share handler have been set.
	 */
	private synchronized void initialIntent() {
		setupCount++;
		if(setupCount == 2) {
			setShareIntent(mCurrentURL);
		}
	}
	
	/**
	 * Create intent for sharing a link
	 * @param url URL string
	 */
	private void setShareIntent(String url) {
		if(mShareActionProvider != null && mCurrentURL != null) {
			Intent intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, url);
			mShareActionProvider.setShareIntent(intent);
		}
		else {
			if(mCurrentURL == null) Log.w(TAG, "No URL set");
			if(mShareActionProvider == null) Log.w(TAG, "Tried to set intent before action provider was set");
		}
	}
	/**
	 * This is called by MainActivity when back button is hit. Use it to go back in browser
	 * history if possible.
	 * Fragment must be added with "www" tag for this to be called.
	 */
	public boolean backPress() {
		if(mWebView != null && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		
		return false;
	}
	
}