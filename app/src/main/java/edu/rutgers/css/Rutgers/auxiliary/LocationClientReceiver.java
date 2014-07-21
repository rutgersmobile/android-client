package edu.rutgers.css.Rutgers.auxiliary;

import android.os.Bundle;

/**
 * Created by jamchamb on 7/21/14.
 */
public interface LocationClientReceiver {

    public void onConnected(Bundle dataBundle);
    public void onDisconnected();

}
