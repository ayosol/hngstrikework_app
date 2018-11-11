package com.example.solom.hotel_csv_app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.solom.hotel_csv_app.adapter.CsvAdapter;

import java.util.ArrayList;

public class ReadAndDisplayActivity extends AppCompatActivity {
    public static final String SMS_SENT_ACTION = "com.example.solom.hotel_csv_app.SMS_SENT";
    public static final String SMS_DELIVERED_ACTION = "com.example.solom.hotel_csv_app.SMS_DELIVERED";
    public static final String EXTRA_NUMBER = "number";
    public static final String EXTRA_MESSAGE = "message";
    private IntentFilter intentFilter;
    private BroadcastReceiver resultsReceiver;

    RecyclerView recyclerView;
    CsvAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private SmsManager smsManager;
    private ArrayList<DataCsv> dataCopy;
    private ArrayList<DataCsv> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_and_display);
        Bundle extras = getIntent().getExtras();
        smsManager = SmsManager.getDefault();
        resultsReceiver = new SmsResultReceiver();

        intentFilter = new IntentFilter(SMS_SENT_ACTION);
        intentFilter.addAction(SMS_DELIVERED_ACTION);

        data = new ArrayList<>();
        recyclerView = findViewById(R.id.rv_csv);
        adapter = new CsvAdapter(data, this);
        layoutManager = new LinearLayoutManager(this);


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        CsvAdapter.OnItemClickListener onItemClickListener = new CsvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int adapterPosition) {
                displayDetailsDialog(adapterPosition);
            }
        };
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        assert extras != null;
        data.addAll(CsvParser.readCsv(extras.getString(MainActivity.EXTRAS_CSV_PATH_NAME)));
        adapter.notifyDataSetChanged();
        findViewById(R.id.send_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataCopy = (ArrayList<DataCsv>) data.clone();
                sendNextSMS();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(resultsReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(resultsReceiver);
    }

    public void delaySms(final int i, final ArrayList<DataCsv> contact) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                smsManager.sendTextMessage(contact.get(i).getPhone(), null, contact.get(i).getMessage(), null, null);
            }
        }, 2000);
    }

    private void sendMultipleSMS(ArrayList<DataCsv> selectedData) {
        //	displayDialog();
        for (int i = 0; i < selectedData.size(); i++) {
            delaySms(i, selectedData);
        }
        //dismissDialog();
    }

    private void sendSingleSMS(int pos) {
        //	displayDialog();
        smsManager.sendTextMessage(data.get(pos).getPhone(), null, data.get(pos).getMessage(), null, null);
        //dismissDialog();
    }

    //Sends SMS to all numbers in CSV
    private void sendNextSMS() {
        // We're going to remove numbers and messages from
        // the lists as we send, so if the lists are empty, we're done.
        if (dataCopy.size() == 0) {
            return;
        }

        // The list size is a sufficiently unique request code,
        // for the PendingIntent since it decrements for each send.
        int requestCode = dataCopy.size();

        String number = dataCopy.get(0).getPhone();
        String message = dataCopy.get(0).getMessage();

        // The Intents must be implicit for this example,
        // as we're registering our Receiver dynamically.
        Intent sentIntent = new Intent(SMS_SENT_ACTION);
        Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION);

        // We attach the recipient's number and message to
        // the Intents for easy retrieval in the Receiver.
        sentIntent.putExtra(EXTRA_NUMBER, number);
        sentIntent.putExtra(EXTRA_MESSAGE, message);
        deliveredIntent.putExtra(EXTRA_NUMBER, number);
        deliveredIntent.putExtra(EXTRA_MESSAGE, message);

        // Construct the PendingIntents for the results.
        // FLAG_ONE_SHOT cancels the PendingIntent after use so we
        // can safely reuse the request codes in subsequent runs.
        PendingIntent sentPI = PendingIntent.getBroadcast(this,
                requestCode,
                sentIntent,
                PendingIntent.FLAG_ONE_SHOT);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this,
                requestCode,
                deliveredIntent,
                PendingIntent.FLAG_ONE_SHOT);

        // Send our message.
        smsManager.sendTextMessage(number, null, message, sentPI, deliveredPI);

        // Remove the number and message we just sent to from the lists.
        dataCopy.remove(0);
    }

    private void displayDetailsDialog(final int pos) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setView();
        builder.setTitle(data.get(pos).getMessage());
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendSingleSMS(pos);
            }
        });
        builder.setNeutralButton("Schedule", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ReadAndDisplayActivity.this, "Coming Soon...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class SmsResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // A simple result Toast text.
            String result = null;

            // Get the result action.
            String action = intent.getAction();

            // Retrieve the recipient's number and message.
            String number = intent.getStringExtra(EXTRA_NUMBER);
            String message = intent.getStringExtra(EXTRA_MESSAGE);

            // This is the result for a send.
            if (SMS_SENT_ACTION.equals(action)) {
                int resultCode = getResultCode();
                result = "Send result : " + translateSentResult(resultCode);
                // The current send is complete. Send the next one.
                sendNextSMS();
            }
            // This is the result for a delivery.
            else if (SMS_DELIVERED_ACTION.equals(action)) {
                SmsMessage sms = null;

                // A delivery result comes from the service
                // center as a simple SMS in a single PDU.
                byte[] pdu = intent.getByteArrayExtra("pdu");
                String format = intent.getStringExtra("format");

                // Construct the SmsMessage from the PDU.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && format != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        sms = SmsMessage.createFromPdu(pdu, format);
                    }
                } else {
                    sms = SmsMessage.createFromPdu(pdu);
                }

                // getResultCode() is not reliable for delivery results.
                // We need to get the status from the SmsMessage.
                result = "Delivery result : " + translateDeliveryStatus(sms.getStatus());
            }

            result = number + ", " + message + "\n" + result;
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }

        String translateSentResult(int resultCode) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    return "Activity.RESULT_OK";
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    return "SmsManager.RESULT_ERROR_GENERIC_FAILURE";
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    return "SmsManager.RESULT_ERROR_RADIO_OFF";
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    return "SmsManager.RESULT_ERROR_NULL_PDU";
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    return "SmsManager.RESULT_ERROR_NO_SERVICE";
                default:
                    return "Unknown error code";
            }
        }

        String translateDeliveryStatus(int status) {
            switch (status) {
                case Telephony.Sms.STATUS_COMPLETE:
                    return "Sms.STATUS_COMPLETE";
                case Telephony.Sms.STATUS_FAILED:
                    return "Sms.STATUS_FAILED";
                case Telephony.Sms.STATUS_PENDING:
                    return "Sms.STATUS_PENDING";
                case Telephony.Sms.STATUS_NONE:
                    return "Sms.STATUS_NONE";
                default:
                    return "Unknown status code";
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.other_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(ReadAndDisplayActivity.this, SettingsActivity.class));
                break;
            case R.id.menu_help:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
