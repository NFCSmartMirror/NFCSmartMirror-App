package com.mirror.nfc.nfcsmartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * Created by mukesh on 19/5/15.
 */
public class ServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                System.out.println("incomingNumber : " + incomingNumber);
                //int duration = Toast.LENGTH_LONG;
                //Toast toast = Toast.makeText(context,"incomingNumber: "+ incomingNumber, duration);
                //Toast.makeText("random").
                //toast.show();
                Toast.makeText(context, "incomingNumber: "+ incomingNumber +  "State: " + state, Toast.LENGTH_LONG).show();
                Intent msgrcv = new Intent("Msg");
                msgrcv.putExtra("package", "");
                msgrcv.putExtra("ticker", incomingNumber);
                msgrcv.putExtra("title", incomingNumber);
                msgrcv.putExtra("text", "");
                System.setOut("msgrv");
                LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }
}