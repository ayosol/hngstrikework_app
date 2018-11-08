package com.example.solom.hotel_csv_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResponseActivity extends AppCompatActivity {
    private static final int CSV_UPLOAD_REQUEST_CODE=107;
    @BindView(R.id.readcsvfile)
    Button readcsvfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        ButterKnife.bind(this);

         readcsvfile.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, CSV_UPLOAD_REQUEST_CODE);
    }
});
        }

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch (requestCode) {
        case CSV_UPLOAD_REQUEST_CODE:
        if (resultCode == RESULT_OK) {
        String PathHolder = data.getData().getPath();
        Toast.makeText(getApplicationContext(), PathHolder, Toast.LENGTH_LONG).show();
        }
        break;
        }
        }



}
