package com.example.igbar.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Config extends AppCompatActivity {
    EditText ip;
    EditText port;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        ip= (EditText)findViewById(R.id.ip);
        port= (EditText)findViewById(R.id.port);

    }

    public void cancel(View view) {
        ip.setText("");
        port.setText("");
        finish();

    }

    public void save(View view) {
        MainActivity.SERVERIP = ip.getText().toString();
        MainActivity.SERVERPORT = Integer.parseInt(port.getText().toString());
        finish();
    }
}
