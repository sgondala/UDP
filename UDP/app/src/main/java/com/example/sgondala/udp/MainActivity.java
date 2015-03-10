package com.example.sgondala.udp;

import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class MainActivity extends ActionBarActivity {

    DatagramSocket clientSocket;
    InetAddress inetAddress;
    Boolean isServer = false;
    DatagramSocket serverSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clickedSend(View v){ //Default port of 4444
        new sendUDPTask().execute();
        //TextView displayBox = (TextView) findViewById(R.id.displayBox);
        //displayBox.setText("Sent Successfully");
        Toast.makeText(getApplicationContext(), "Sent successfully", Toast.LENGTH_SHORT).show();
    }

    public void clickedConnect(View v){
        //Toast.makeText(getApplicationContext(), "Selected", Toast.LENGTH_SHORT).show();

        try {
            clientSocket = new DatagramSocket();
            EditText e = (EditText) findViewById(R.id.partnerIP);
            String ip = e.getText().toString();
            //String ip = "localhost";
            inetAddress= InetAddress.getByName(ip);
            String outPut = inetAddress.toString();
            Toast.makeText(getApplicationContext(), "Connected, "+outPut, Toast.LENGTH_SHORT).show();
        } catch (SocketException e) {
            System.out.println("A:Socket Exception");
        }

        catch (UnknownHostException e){
            System.out.println("A:Unknown Host Exception");
        }
        //Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
    }

    public void selectedServer(){
        try {
            serverSocket = new DatagramSocket(4444);
            System.out.println("Created server socket");
        } catch (SocketException e) {
            System.out.println("Error in creating new socket");
        }

        new receiveUDPTask().execute();
    }

    private class receiveUDPTask extends AsyncTask<Void, Void, Void>{

        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, 1024);


        @Override
        protected Void doInBackground(Void... params) {

           while(true){
                System.out.println("Listening for packets");
                try {
                    serverSocket.receive(dp);
                    System.out.println("Got packet!!!!!");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                //String str = new String(dp.getData(),0,dp.getLength());
                //changed = true;
                //doPrint(str);
                //TextView displayBox = (TextView) findViewById(R.id.displayBox);
                //displayBox.setText(str);
           }

           // return null;
        }

    }

    /*
    public void doPrint(String str){

        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        TextView displayBox= (TextView) findViewById(R.id.displayBox);
        displayBox.setText(str);

    }
    */

    private class sendUDPTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try{
                EditText e = (EditText) findViewById(R.id.messageBox);
                String message = e.getText().toString();
                byte[] sendBuffer = new byte[1024];
                sendBuffer = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, inetAddress, 4444);
                clientSocket.send(sendPacket);
            }

            catch(IOException e){
                System.out.println("IOException in sending");
            }
            return null;
        }

    }

    public void clickedRadioButton(View view){
        Button btn = (Button) findViewById(R.id.sendButton);
        switch (view.getId()) {
            case R.id.clientButton:
                System.out.println("Selected Client");
                isServer = false;
                btn.setEnabled(true);
                break;
            case R.id.serverButton:
                System.out.println("Selected Server");
                isServer = true;
                btn.setEnabled(false);
                selectedServer();
                break;
        }
    }

}




