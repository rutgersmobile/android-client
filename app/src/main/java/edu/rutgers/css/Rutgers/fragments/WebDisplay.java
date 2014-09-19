package edu.rutgers.css.Rutgers.fragments;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import edu.rutgers.css.Rutgers2.R;

public class WebDisplay extends Fragment {

	private static final String TAG = "WebDisplay";
    public static final String HANDLE = "www";

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
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_web_display, container, false);
        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mWebView = (WebView) v.findViewById(R.id.webView);
        Bundle args = getArguments();

        if(args.getString("title") != null) {
            getActivity().setTitle(args.getString("title"));
        }

        // Check for saved web view state first
        if(savedInstanceState != null && savedInstanceState.containsKey("mCurrentURL")) {
            mCurrentURL = savedInstanceState.getString("mCurrentURL");
            mWebView.restoreState(savedInstanceState);
        }
        // Check initially supplied URL
        else {
            if(args.getString("url") != null) {
                mCurrentURL = args.getString("url");
                initialIntent();
                mWebView.loadUrl(mCurrentURL);
            }
            else {
                Log.w(TAG, "No URL supplied");
            }
        }

        if(mCurrentURL == null) {
			String msg = getString(R.string.failed_no_url);
			mWebView.loadData(msg, "text/plain", null);
			return v;
		}
		
		mWebView.getSettings().setJavaScriptEnabled(true);

		// Progress bar
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if(progress != 100 && progressBar.getVisibility() == View.GONE) progressBar.setVisibility(View.VISIBLE);
                else if(progress == 100) progressBar.setVisibility(View.GONE);
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

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.w(TAG, "WebViewClient error: code " + errorCode + ": " + description + " @ " + failingUrl);

                // Can't handle this URI scheme, try a view intent
                if(errorCode == WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(failingUrl));
                    try {
                        startActivity(intent);
                        String handleIt = String.format(getString(R.string.www_handle_uri), failingUrl);
                        mWebView.loadData(handleIt, "text/plain", null);
                    } catch(ActivityNotFoundException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }

		});

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long length) {
                Log.i(TAG, "Downloading an " + mimeType);

                // Add cookies from current page to download request to maintain session
                // (otherwise download may fail)
                String cookies = CookieManager.getInstance().getCookie(url);

                // Set up download request
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.addRequestHeader("COOKIE",cookies);

                // Start download
                DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                downloadManager.enqueue(request);

                Toast.makeText(getActivity(), R.string.www_start_dl, Toast.LENGTH_SHORT).show();
            }
        });
		
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                if(mWebView != null) {
                    mWebView.loadUrl("javascript:window.location.reload(true)");
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mWebView != null) {
            outState.putString("mCurrentURL", mCurrentURL);
            mWebView.saveState(outState);
        }
    }

	/**
	 * Set up the first Share intent only after the URL and share handler have been set.
	 */
	private synchronized void initialIntent() {
		if(setupCount < 2) setupCount++;
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
