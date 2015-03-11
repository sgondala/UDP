package com.example.sgondala.udp;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class MainActivity extends ActionBarActivity {

    DatagramSocket clientSocket = null;
    InetAddress inetAddress = null;
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

        EditText e = (EditText) findViewById(R.id.partnerIP);
        String ip = e.getText().toString();
        InetAddress inetAddressTemp = null;
        try {
            inetAddressTemp = InetAddress.getByName(ip);
        }
        catch (UnknownHostException e1) {
            System.out.println("Unknown host exception in string IP thing");
        }

        if(!inetAddressTemp.equals(inetAddress)) {

            try {
                clientSocket = new DatagramSocket();
                inetAddress = inetAddressTemp;
                String outPut = inetAddress.toString();
                Toast.makeText(getApplicationContext(), "Connected, " + outPut, Toast.LENGTH_SHORT).show();
            } catch (SocketException e2) {
                System.out.println("A:Socket Exception");
            }
        }

        new sendUDPTask().execute();
        Toast.makeText(getApplicationContext(), "Sent successfully", Toast.LENGTH_SHORT).show();
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

                final String str = new String(dp.getData(),0,dp.getLength());

               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       TextView displayBox = (TextView) findViewById(R.id.displayBox);
                       displayBox.setText(str);
                   }
               });
           }
            //return null;
        }

    }

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




