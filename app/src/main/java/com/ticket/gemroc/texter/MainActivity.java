package com.ticket.gemroc.texter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cted on 6/12/15.
 */
public class MainActivity extends Activity {
    private String TAG = getClass().getSimpleName();
    BootstrapEditText phoneNumberEditText, minutesEditText, numberOfMessagesEditText;
    BootstrapButton sendMessageButton, messagesFileButton;
    TextView fileNameTextView;
    String phoneNumber, minutes, numberOfMessages;
    int smsCounter = 0;
    Timer smsTimer = new Timer();
    File messagesFile;
    static final int request = 100;
    int fileLineCOunt = 0;
    ArrayList<String> messagesArray = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messagesFileButton = (BootstrapButton) findViewById(R.id.messages_file_button);
        phoneNumberEditText = (BootstrapEditText) findViewById(R.id.phone_number);
        minutesEditText = (BootstrapEditText) findViewById(R.id.time_taken);
        fileNameTextView = (TextView) findViewById(R.id.file_name);
        // numberOfMessagesEditText = (BootstrapEditText) findViewById(R.id.number_of_messages);
        sendMessageButton = (BootstrapButton) findViewById(R.id.send_button);

        File callLogFile = new File(Environment.getExternalStorageDirectory(), "texttimecloudlogsto.txt");
        File callLogFileReturn = new File(Environment.getExternalStorageDirectory(), "texttimecloudlogsfrom.txt");
        // if(!callLogFile.exists()){
        try {
            if(callLogFile.createNewFile()){
                Log.i(TAG, "File created");
            }else{
                Log.i(TAG, "File Already exists");
            }

            if(callLogFileReturn.createNewFile()){
                Log.i(TAG, "File created");
            }else{
                Log.i(TAG, "File Already exists");
            }
        } catch (IOException e) {
            Log.i(TAG, "Create New File exception >> " + e);
            e.printStackTrace();
        }
        // }
        sendBroadcast((new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)).setData(Uri.fromFile(callLogFile)).setData(Uri.fromFile(callLogFileReturn)));

        messagesFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 chooseFile();
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "In OnClick");
                phoneNumber = phoneNumberEditText.getText().toString();
                minutes = minutesEditText.getText().toString();
                //numberOfMessages = numberOfMessagesEditText.getText().toString();
                Log.i(TAG, "Phone Number >> " + phoneNumber);
                Log.i(TAG, "Minutes >> " + minutes);

                Log.i(TAG, "Number Of Messages >> " + numberOfMessages);

                if (!phoneNumber.equalsIgnoreCase("") && !minutes.equalsIgnoreCase("") /*&& !numberOfMessages.equalsIgnoreCase("")*/ && fileLineCOunt!=0) {
                    Log.i(TAG, "In Valid");
                    Toast.makeText(getApplicationContext(), "Sending Messages", Toast.LENGTH_LONG).show();
                    double minutesInSeconds = Integer.parseInt(minutes) * 60;
                    //final double numberMessages = (Integer.parseInt(numberOfMessages) /*/ minutesInSecond*/);
                    double messageSendingInterval = minutesInSeconds / fileLineCOunt;
                    Log.i(TAG, "Minutes In Seconds >> " + minutesInSeconds);
                    // Log.i(TAG, "Number of Messages >> " + numberMessages);
                    Log.i(TAG, "Message sending interval Rounded >> " + Math.round(messageSendingInterval));

                    try {
                        smsTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "In run");
                                smsCounter++;
                                Log.i(TAG, "SMS >> " + smsCounter);
                                Log.i(TAG, "SMS String >> " + String.valueOf(smsCounter));
                                if (smsCounter <= fileLineCOunt) {
                                    // String message = "cloud" + String.valueOf(smsCounter);
                                    String message = messagesArray.get(smsCounter - 1);
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                                    saveTextTimeInLog(message);
                                } else {
                                    smsTimer.cancel();
                                }
                            }
                        }, new Date(), Math.round(messageSendingInterval) * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == request) {

            Uri dataUri = data.getData();
            Log.i(TAG, "Data: " + dataUri);
            File file = new File(dataUri.getPath());
            Log.i(TAG, "File : " + file);
            messagesFile = file;
            fileNameTextView.setVisibility(View.VISIBLE);
            fileNameTextView.setText(messagesFile.getName());
            Log.i(TAG, "File 2 : " + messagesFile);
            LineNumberReader reader = null;
            try {
                reader = new LineNumberReader(new FileReader(messagesFile));
                Log.i(TAG, "Buffered Reader : " + reader);
                String arrayData;
                while ((arrayData = reader.readLine()) != null){
                    Log.i(TAG, "Line : " + arrayData);
                    messagesArray.add(arrayData);
                }
                fileLineCOunt = reader.getLineNumber();


                Log.i(TAG, "File line count : " + fileLineCOunt);
                Log.i(TAG, "Messages Array  : " + messagesArray);
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if(reader!=null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }



        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void saveTextTimeInLog(String message){
        Date currentDate = new Date();
        String currentDateString = DateFormat.getDateTimeInstance().format(currentDate);
        Log.i(TAG, "Current Date time >> " + currentDateString);
        OutputStreamWriter outputStream = null;
        try{
            outputStream = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator+"texttimecloudlogsto.txt", true));
            CharSequence charSequence = (currentDateString+ "  " + message + "\n");
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

    private void chooseFile(){
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, "Choose file");
        try {
            startActivityForResult(intent, request);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }
}
