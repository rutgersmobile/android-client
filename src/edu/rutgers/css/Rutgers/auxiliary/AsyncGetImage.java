package edu.rutgers.css.Rutgers.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/*
 * Download an image in a separate thread.
 */
public class AsyncGetImage extends AsyncTask<URL, Integer, Bitmap> {

	private static final String TAG = "AsyncGetImage";
	private ImageView targetImageView;
	
	/*
	 * ImageView to fill with downloaded image.
	 */
	public AsyncGetImage(ImageView targetImageView) {
		this.targetImageView = targetImageView;
	}
	
	@Override
	protected Bitmap doInBackground(URL... urls) {
		Bitmap result = null;
		URL imgUrl = urls[0];
		
		try {
			URLConnection con = imgUrl.openConnection();
			con.setUseCaches(true);
			InputStream in = con.getInputStream();
			result = BitmapFactory.decodeStream(in);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		
		return result;
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		targetImageView.setImageBitmap(result);
	}
	
}
