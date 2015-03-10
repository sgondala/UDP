package com.example.sgondala.udp;

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
          try{

            EditText e = (EditText) findViewById(R.id.messageBox);
            String message = e.getText().toString();

            //String message = "Hello";

            byte[] sendBuffer = new byte[1024];
            sendBuffer = message.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, inetAddress, 4444);

            clientSocket.send(sendPacket);

            Toast.makeText(getApplicationContext(), "Sent successfully", Toast.LENGTH_SHORT).show();
        }

        catch(IOException e){
            //Toast.makeText(getApplicationContext(), "IOException in sending", Toast.LENGTH_SHORT).show();
        }
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
            //Toast.makeText(getApplicationContext(), "Socket Exception", Toast.LENGTH_SHORT).show();
        }

        catch (UnknownHostException e){
            //Toast.makeText(getApplicationContext(), "Unknown host exception", Toast.LENGTH_SHORT).show();
        }
        //Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
    }
}
