package com.example.solom.hotel_csv_app;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solom.hotel_csv_app.adapter.CsvAdapter;
import com.example.solom.hotel_csv_app.models.DataCsv;

import java.util.ArrayList;

public class ReadAndDisplayActivity extends AppCompatActivity {
    public static final String SMS_SENT_ACTION = "com.example.solom.hotel_csv_app.SMS_SENT";
    public static final String SMS_DELIVERED_ACTION = "com.example.solom.hotel_csv_app.SMS_DELIVERED";
    public static final String EXTRA_NUMBER = "number";
    public static final String EXTRA_MESSAGE = "message";
    private static final String PERMISSION_TAG = "PERMISSION";
    public static final int SMS_PERMISSION_CODE = 102;
    private final int DIALOG_TYPE_LOADING = 1;
    private final int DIALOG_TYPE_CONFIRMATION = 2;

    private IntentFilter intentFilter;
    private BroadcastReceiver resultsReceiver;

    RecyclerView recyclerView;
    CsvAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private SmsManager smsManager;
    private ArrayList<DataCsv> data;
    private ArrayList<DataCsv> dataCopy = new ArrayList<>();
    private ArrayList<DataCsv> failedSMS = new ArrayList<>();

    private int smsSendingindex = 0;
    private int smsToSendSize = 0;
    private TextView loadingProgressTv;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_and_display);
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String csvPath = extras.getString(MainActivity.EXTRAS_CSV_PATH_NAME);
        String csvFileName = extras.getString(MainActivity.EXTRAS_CSV_FILE_NAME);
        setupActionBar(csvFileName);

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
        data.addAll(CsvParser.readCsv(csvPath));
        adapter.notifyDataSetChanged();
        findViewById(R.id.send_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataCopy = (ArrayList<DataCsv>) data.clone();
                smsToSendSize = dataCopy.size();
                displayDialog(DIALOG_TYPE_LOADING);
                sendNextSMS();
            }
        });
    }

    private void setupActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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

    public void displayDialog(int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view;
        switch (type) {
            case DIALOG_TYPE_LOADING:
                view = LayoutInflater.from(this).inflate(R.layout.loading_dialog_layout, null);
                loadingProgressTv = view.findViewById(R.id.loading_dialog_progress);
                builder.setView(view);
                builder.setCancelable(false);
                break;
            case DIALOG_TYPE_CONFIRMATION:
                view = LayoutInflater.from(this).inflate(R.layout.confirmation_dialog_layout, null);
                Button okBtn = view.findViewById(R.id.confirmation_dialog_ok_btn);
                TextView successfulSmsTv = view.findViewById(R.id.confirmation_successful_txt);
                TextView failedSmsTv = view.findViewById(R.id.confirmation_failed_txt);
                int sent = smsToSendSize - failedSMS.size();
                successfulSmsTv.setText("" + sent + " Sent");
                failedSmsTv.setText("" + failedSMS.size() + " Failed");
                okBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View p1) {
                        dialog.dismiss();
                    }
                });
                builder.setView(view);
                builder.setCancelable(false);
                break;
        }
        dialog = builder
                .create();
        dialog.show();
    }

    private void displayDetailsDialog(final int pos) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.items_detials_dialog, null);

        TextView phoneTv = view.findViewById(R.id.details_phone_num);
        TextView msgTv = view.findViewById(R.id.details_message);
        phoneTv.setText(data.get(pos).getPhone());
        msgTv.setText(data.get(pos).getMessage());
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        view.findViewById(R.id.details_send_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSingleSMS(pos);
                dialog.dismiss();

            }
        });
        view.findViewById(R.id.details_schedule_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ReadAndDisplayActivity.this, "Coming Soon...", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
        view.findViewById(R.id.details_cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void sendMultipleSMS(ArrayList<DataCsv> selectedData) {
        if (checkPermission(Manifest.permission.SEND_SMS, SMS_PERMISSION_CODE)) {

            //TODO:	Display Loading Dialog
            displayDialog(DIALOG_TYPE_LOADING);
            if (!dataCopy.isEmpty()) dataCopy.clear();
            dataCopy = selectedData;
            smsToSendSize = dataCopy.size();
            sendNextSMS();
        }
    }

    private void sendSingleSMS(int pos) {
        if (checkPermission(Manifest.permission.SEND_SMS, SMS_PERMISSION_CODE)) {
            //TODO:Display Loading Dialog
            displayDialog(DIALOG_TYPE_LOADING);
            dataCopy.clear();
            dataCopy.add(data.get(pos));
            smsToSendSize = dataCopy.size();
            sendNextSMS();
            Toast.makeText(ReadAndDisplayActivity.this, "SMS sent!", Toast.LENGTH_SHORT).show();
        }
    }


    //Sends SMS to all numbers in dataCopy
    private void sendNextSMS() {
        if (checkPermission(Manifest.permission.SEND_SMS, SMS_PERMISSION_CODE)) {
            // We're going to remove numbers and messages from
            // the lists as we send, so if the lists are empty, we're done.
            if (dataCopy.size() == 0) {
                //TODO:Display Confirmation Dialog
                dialog.dismiss();
                displayDialog(DIALOG_TYPE_CONFIRMATION);
                smsSendingindex = 0;
                smsToSendSize = 0;
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

            smsSendingindex++;
            loadingProgressTv.setText(String.format("%d/%d", smsSendingindex, smsToSendSize));
            //Toast.makeText(this, "Sending... " + smsSendingindex + "/" + smsToSendSize, Toast.LENGTH_SHORT).show();

            // Remove the number and message we just sent to from the lists.
            dataCopy.remove(0);
        }
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
                Log.v("SMS_SENT_ACTION", result);

                //Keeping Track of SMS not sent
                if (!translateSentResult(resultCode).equals(getString(R.string.sms_action_sent_Activity_RESULT_OK))) {
                    failedSMS.add(new DataCsv(number, message));
                }

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
                result = number + ", " + message + " " + result;
                Log.v("SMS_DELIVERED_ACTION", result);
            }
        }

        String translateSentResult(int resultCode) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    return getString(R.string.sms_action_sent_Activity_RESULT_OK);
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    return getString(R.string.sms_action_sent_SmsManager_RESULT_ERROR_GENERIC_FAILURE);
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    return getString(R.string.sms_action_sent_SmsManager_RESULT_ERROR_RADIO_OFF);
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    return getString(R.string.sms_action_sent_SmsManager_RESULT_ERROR_NULL_PDU);
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    return getString(R.string.sms_action_sent_SmsManager_RESULT_ERROR_NO_SERVICE);
                default:
                    return getString(R.string.unknown_error_code);
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


    public boolean checkPermission(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    permission) == PackageManager.PERMISSION_GRANTED) {
                Log.v(PERMISSION_TAG, "Permission is granted");
                return true;
            } else {
                Log.v(PERMISSION_TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
                return false;
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(PERMISSION_TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(PERMISSION_TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            Toast.makeText(ReadAndDisplayActivity.this, "Permission Granted,Please Resend the SMS...", Toast.LENGTH_LONG).show();
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
