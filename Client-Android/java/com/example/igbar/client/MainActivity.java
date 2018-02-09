package com.example.igbar.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    public static String SERVERIP ;
    public static int SERVERPORT ;

    public PrintWriter out;
    public BufferedReader in;
    public Socket socket = null;
    public String serverMessage;
    public boolean flagRefreshList = false;
    public static  View v;
    ArrayList<listItem> arrayList;
    myCustomAdapter adapter;


    String MessageOk = null;
    Boolean flagConnect = false;
    DateFormat sdf = new SimpleDateFormat("hh:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //region config
        final String CONFIG_FILE_NAME = "config.properties";
        Properties prop = new Properties();
        InputStream input = null;
        OutputStream output = null;
       // scan = new Scanner(System.in);
        String PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
        File file =new File(PATH+CONFIG_FILE_NAME);
        try {
            if(file.exists())
            {
                input = new FileInputStream(CONFIG_FILE_NAME);
                // load a properties file
                prop.load(input);
                // get the property value and print it out
                SERVERPORT = Integer.parseInt(prop.getProperty("port"));
                SERVERIP =  prop.getProperty("ipAddress");

            }else
            {

                //file.createNewFile();
                // check port
                //output = new FileOutputStream(file,false);
                SERVERPORT = 3333;
                SERVERIP = "192.168.1.102";

                // set the properties value
                prop.setProperty("port", String.valueOf(SERVERPORT));
                prop.setProperty("ipAddress", SERVERIP);

                // save properties to project root folder
               // prop.store(output, null);
            }




        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("IOException File");
            return;
        }
        //endregion config



        final EditText txtMsg = (EditText) findViewById(R.id.editText);
        final EditText txtTopic = (EditText) findViewById(R.id.txtTopic);
        final Button leaveButton = (Button) findViewById(R.id.leave_button);
        final Button registerButton = (Button) findViewById(R.id.register_button);
        FloatingActionButton sendButton = (FloatingActionButton) findViewById(R.id.fab);
        v = (View)findViewById(R.id.Main);

        // List View for the items ShomMessageByIO
        ListView listv = (ListView) findViewById(R.id.list);
        arrayList = new ArrayList<listItem>();
        // adapter MessageList To ItemList
        adapter = new myCustomAdapter(arrayList);
        listv.setAdapter(adapter);

        //region SEND button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String message = txtMsg.getText().toString();
                //add the text in the arrayList
                if(flagConnect == false) {
                    Snackbar.make(view, "You are deconnect", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //sends the message to the server
                        synchronized (this)
                        {
                            if (out != null && !out.checkError() && (!message.isEmpty()))
                                if(message.indexOf(" ")>0 && message.split(" ").length>1) {
                                    String msg[] = message.split(" ", 2);

                                    arrayList.add(new listItem("SEND",msg[0],msg[1],"MyMessage",false,sdf.format(new Date())));
                                    out.println("SEND "+ message);
                                    out.flush();

                                }else if(message.toUpperCase().equals("CLOSE") && socket != null && out != null ) {
                                    out.println("CLOSE "+ socket.getInetAddress().toString());
                                    out.flush();
                                }
                        }
                    }
                }).start();
                if (socket != null && out != null && !out.checkError() && (!message.isEmpty()) && message.indexOf(" ")>0 && message.split(" ").length>1 || message.toUpperCase().equals("CLOSE")) {
                    adapter.notifyDataSetChanged();
                    txtMsg.setText("");
                }
                //refresh the list
            }
        });
        //endregion SEND button

        //region REGISTER button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = txtTopic.getText().toString();
                if(flagConnect == false) {
                    Snackbar.make(view, "You are deconnect", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //sends the message to the server
                        if (out != null && !out.checkError() && (!message.isEmpty()) && (!" ".equals(message))) {
                            out.println("REGISTER "+ message);
                            out.flush();
                        }
                    }
                }).start();
                //refresh the list
                adapter.notifyDataSetChanged();
                txtTopic.setText("");
            }
        });
        //endregion REGISTER button

        //region LEAVE button
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = txtTopic.getText().toString();
                if(flagConnect == false) {
                    Snackbar.make(view, "You are deconnect", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //sends the message to the server
                        if (out != null && !out.checkError() && (!message.isEmpty()) && (!" ".equals(message))) {
                            out.println("LEAVE "+ message);
                            out.flush();
                        }else{
                            Log.e("Error","Input");
                        }
                    }
                }).start();
                //refresh the list
                adapter.notifyDataSetChanged();
                txtTopic.setText("");
            }
        });

        //endregion LEAVE button

        // refresh ListItems to check if have messages - Up date
        //region Handler flagRefreshList
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                if(flagRefreshList) {
                    flagRefreshList = false;
                    adapter.notifyDataSetChanged();
                }
                if(MessageOk != null)
                {
                    Toast.makeText(MainActivity.this, "OK - Operation succeed ", Toast.LENGTH_SHORT).show();

                    MessageOk =null;
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
        //endregion Handler flagRefreshList

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    // Settings Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //region connection / disconnection
        if (id == R.id.connect) {
            if(socket==null) {
                new connect();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(socket!=null && socket.isConnected()) {
                    flagConnect =true;
                    forword forword = new forword();
                    adapter.notifyDataSetChanged();
                }else
                    Snackbar.make(MainActivity.v, "Error connect", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
            }
            else if(flagConnect)
            {
                Snackbar.make(MainActivity.v, "You are connect", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }else
            {
                flagConnect =true;
                new forword();
                flagRefreshList = true;
            }
        } else if (id == R.id.dsconnect) {
            if(flagConnect == true ) {
                flagConnect = false;
                Snackbar.make(MainActivity.v, "Stop Forword", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }else
            {
                Snackbar.make(MainActivity.v, "You are deconnect", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        } else if (id == R.id.config) {
            Intent intent = new Intent(this,Config.class);
            startActivity(intent);

        }
            //endregion connection
        return super.onOptionsItemSelected(item);
    }


    // class to adapter the Messages to ListItem - And Barker the messages How to show that

    class  myCustomAdapter extends BaseAdapter{
        ArrayList<listItem> arrayList = new ArrayList<listItem>();
        ArrayList<View> sendList = new ArrayList<View>();
        int count =0;
        myCustomAdapter(ArrayList<listItem> list)
        {
            this.arrayList =list;
        }
        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public listItem getItem(int i) {
            return arrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            try {
                if(i<=getCount()) {
                    LayoutInflater linflater = getLayoutInflater();
                    View v1 = linflater.inflate(R.layout.message_item, null);

                    TextView topic = (TextView) v1.findViewById(R.id.name);
                    TextView msg = (TextView) v1.findViewById(R.id.msg);
                    TextView ipAddress = (TextView)v1.findViewById(R.id.ipAddress);
                    TextView time = (TextView)v1.findViewById(R.id.time);
                    CheckBox checkBox = (CheckBox) v1.findViewById(R.id.checkBox);
                    topic.setGravity(Gravity.CENTER);
                    topic.setTextSize(15);

                    switch (arrayList.get(i).type)
                    {
                        //region FORWORD
                        case "FORWORD":
                            topic.setText(arrayList.get(i).topic);
                            msg.setText(arrayList.get(i).message);
                            ipAddress.setText(arrayList.get(i).ipAddress);
                            time.setText(arrayList.get(i).time);
                            checkBox.setEnabled(false);
                            break;
                        //endregion FORWORD

                        //region ERROR
                        case "ERROR":
                            topic.setText(arrayList.get(i).topic);
                            msg.setText(arrayList.get(i).message);
                            msg.setGravity(Gravity.CENTER);

                            msg.setTextSize(20);
                            ipAddress.setEnabled(false);
                            time.setEnabled(false);
                            checkBox.setEnabled(false);
                            v1.setBackgroundColor(Color.RED);
                            break;
                        //endregion ERROR

                        //region CONNECT
                        case "CONNECT":
                            topic.setText(arrayList.get(i).topic);
                            msg.setText(arrayList.get(i).message);
                            ipAddress.setText(arrayList.get(i).ipAddress);
                            //time.setText();
                            checkBox.setEnabled(false);
                            v1.setBackgroundColor(Color.MAGENTA);

                            break;
                        //endregion CONNECT

                        //region SEND
                        case "SEND":
                            topic.setText(arrayList.get(i).topic);
                            msg.setText(arrayList.get(i).message);
                            ipAddress.setText(arrayList.get(i).ipAddress);
                            time.setText(arrayList.get(i).time);
                            if(arrayList.get(i).checkBox) {
                                checkBox.setEnabled(true);
                                checkBox.setChecked(true);
                            }
                            v1.setBackgroundColor(Color.GRAY);
                            sendList.add(v1);
                            break;
                        //endregion SEND

                    }
                    return v1;
                }
            }catch (Exception e )
            {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return view;
        }
    }

    class connect extends Thread{
        connect(){
            this.start();
        }
        @Override
        public void run() {
            super.run();
            int timeOut = 4000;

            try {
                //socket = new Socket(SERVERIP,SERVERPORT);
                socket = new Socket();
                //InetSocketAddress(InetAddress addr, int port)

                SocketAddress socketAddress = new InetSocketAddress(SERVERIP, SERVERPORT);
                socket.connect(socketAddress,timeOut);

                if(socket.isConnected()) {
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    arrayList.add(new listItem("CONNECT","Client","Connection successful: \n  Your connection is successful","",null,sdf.format(new Date())));
                    flagRefreshList =true;
                }
            } catch (SocketTimeoutException e) {
                socket = null;
                timeOut = 3000;
                arrayList.add(new listItem("ERROR","Error", "'Connection Failed'\n Your connection is Failed Time out", "SocketTimeoutException", true,sdf.format(new Date())));
                flagRefreshList =true;
                e.printStackTrace();
            } catch (IOException e) {
                socket = null;
                arrayList.add(new listItem("ERROR","Error", "'Connection Failed'\n Your connection is Failed IO " + e.getMessage(), "IOException", true,sdf.format(new Date())));
                flagRefreshList =true;
                e.printStackTrace();
            }
        }
    }

    class forword extends Thread{
        public forword() {
            super();
            this.start();
        }

        @Override
        public void run() {
            super.run();
            try {
                synchronized (this){

                }
                int i = 0;
                while (flagConnect) {
                    if (socket != null && socket.getInputStream() != null)
                        //while (socket.getInputStream().available()>0)
                        synchronized (this) {
                            if ((null != (serverMessage = in.readLine())))
                                if (!serverMessage.isEmpty() && flagConnect) {
                                    switch (serverMessage.split(" ", 2)[0]) {
                                        //region FORWORD
                                        case "FORWORD":
                                            arrayList.add(new listItem(serverMessage));
                                            flagRefreshList = true;
                                            break;
                                        //endregion FORWORD

                                        //region ERROR
                                        case "ERROR":
                                            String msg[] = serverMessage.split(" ", 2);
                                            arrayList.add(new listItem("ERROR", msg[0], msg[1], "", null, sdf.format(new Date())));
                                            flagRefreshList = true;
                                            break;
                                        //endregion FORWORD

                                        //region SENDERROR
                                        case "SENDERROR":
                                            for (; i < arrayList.size(); i++) {
                                                if (arrayList.get(i).type == "SEND" && arrayList.get(i).checkBox == false) {
                                                    Thread.sleep(300);
                                                    arrayList.get(i).checkBox = false;
                                                    arrayList.get(i).message = serverMessage.split(" ", 2)[1];
                                                    arrayList.get(i).type = "ERROR";
                                                    break;

                                                }
                                            }

                                            flagRefreshList = true;
                                            break;
                                        //endregion SENDERROR

                                        //region OK
                                        case "OK":
                                            //arrayList.add(new listItem("OK","","","",true,""));
                                            boolean f = false;
                                            for (; i < arrayList.size(); i++) {
                                                if (arrayList.get(i).type == "SEND" && arrayList.get(i).checkBox == false) {
                                                    Thread.sleep(300);
                                                    arrayList.get(i).checkBox = true;
                                                    //arrayList.get(i).time = sdf.format(new Date());
                                                    f= true;
                                                    break;
                                                }
                                            }
                                            if(f==false)
                                                MessageOk = "OK";


                                            flagRefreshList = true;
                                            break;
                                        //endregion OK

                                        //region CLOSE
                                        case "CLOSE":
                                            socket.close();
                                            in.close();
                                            out.close();
                                            socket=null;
                                            in=null;
                                            out=null;
                                            flagConnect = false;
                                            arrayList.add(new listItem("FORWORD","Closed","it's Closed","",false,""));
                                            break;
                                        //endregion CLOSE

                                    }
                                }
                        }
                    serverMessage = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }



}
