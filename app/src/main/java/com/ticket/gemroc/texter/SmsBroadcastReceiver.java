package com.ticket.gemroc.texter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by cted on 9/3/15.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    private String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        //String data = intent.getData().toString();
        Log.i(TAG, "Bundle object is " + bundle);
        //Log.i(TAG, "Data is " + data);
        String senderNumber = null, message = null;
        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    senderNumber = currentMessage.getDisplayOriginatingAddress();

                    message = currentMessage.getDisplayMessageBody();

                    Log.i(TAG, "Sender Number is " + senderNumber);
                    Log.i(TAG, "Message is " + message);
                    saveTextTimeInLog(message);



                    // Show alert
                    /*int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, "senderNum: "+ senderNum + ", message: " + message, duration);
                    toast.show();*/

                } // end for loop

            }

        } catch (Exception e) {
            Log.e(TAG, "Exception smsReceiver " +e);

        }
    }

    private void saveTextTimeInLog(String message){
        String reversedMessage = new StringBuilder(message).reverse().toString();
        Date currentDate = new Date();
        String currentDateString = DateFormat.getDateTimeInstance().format(currentDate);
        Log.i(TAG, "Current Date time >> " + currentDateString);
        OutputStreamWriter outputStream = null;
        try{
            outputStream = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator+"texttimecloudlogsfrom.txt", true));
            CharSequence charSequence = (currentDateString + "  " + reversedMessage +"\n");
            outputStream.append(charSequence);


        }catch(FileNotFoundException exception){
            Log.i(TAG, "File Not Found Exception >> " + exception);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
