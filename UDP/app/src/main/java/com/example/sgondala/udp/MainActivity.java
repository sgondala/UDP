package com.example.sgondala.udp;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    DatagramSocket clientSocket = null;
    InetAddress inetAddress = null;
    Boolean isServer = false;
    DatagramSocket serverSocket = null;
    int sizeOfAudio = 10240;
    String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myRecording";
    MediaPlayer myMediaPlayer = new MediaPlayer();
    int fileNo = 0;
    int sendFileNo = 0;
    Queue<String> audioQueue = new LinkedList<String>();
    Queue<String> sendingAudioQueue = new LinkedList<String>();
    Boolean stopClicked = false;
    Boolean tempToBreak = false;
    sendUDPTask a = new sendUDPTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myMediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        //System.out.println("Yo man, This works properly!!");
                        if(audioQueue.size()>0) {
                            try {
                                //System.out.println("Should play now ....");
                                String temp = audioQueue.remove();
                                myMediaPlayer.reset();
                                myMediaPlayer.setDataSource(temp);
                                //System.out.println("Diladaraa..");
                                myMediaPlayer.prepare();
                                System.out.println("Playing "+ temp);
                                myMediaPlayer.start();
                            } catch (IOException e) {
                                System.out.println("I love you rachel");
                            }
                        }
                    }
                }
        );

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

        stopClicked = false;
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
                //Toast.makeText(getApplicationContext(), "Connected, " + outPut, Toast.LENGTH_SHORT).show();
            } catch (SocketException e2) {
                System.out.println("A:Socket Exception");
            }
        }

        final MediaRecorder myAudioRecorder = new MediaRecorder();

        new Thread(new Runnable() {
            @Override
            public void run() {

                while(true) {
                    System.out.println("New loop...");
                    if(stopClicked) {System.out.println("Broke the outer while...");break;}
                    myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    final String outputFileSending = outputFile + "S" + Integer.toString(sendFileNo) + ".3gp";
                    sendFileNo = (sendFileNo + 1)%30;
                    myAudioRecorder.setOutputFile(outputFileSending);
                    try {
                        myAudioRecorder.prepare();
                        myAudioRecorder.start();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        myAudioRecorder.stop();
                                        myAudioRecorder.reset();

                                        if(a.getStatus() == AsyncTask.Status.RUNNING){
                                            sendingAudioQueue.add(outputFileSending);
                                            System.out.println("Just added..");
                                        }
                                        else{
                                            sendingAudioQueue.add(outputFileSending);
                                            System.out.println("Added and executing");
                                            a = new sendUDPTask();
                                            a.execute();
                                        }
                                        tempToBreak = true;
                                    }
                                });
                            }
                        }, 500);

                        while(true){
                            if(tempToBreak){tempToBreak = false; break;}
                        }

                    } catch (IOException e1) {
                        System.out.println("Problem in recording audio");
                    }
                }
            }
        }).start();
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

        byte[] buf = new byte[sizeOfAudio];
        DatagramPacket dp = new DatagramPacket(buf, sizeOfAudio);

        @Override
        protected Void doInBackground(Void... params) {

        while(true){
            System.out.println("Listening for packets");
            try {
                serverSocket.receive(dp);
                System.out.println("Got packet!!!!!");
                String fileNumberAdding = Integer.toString(fileNo);
                fileNo = (fileNo + 1)%30;
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile + "Received" + fileNumberAdding+ ".3gp");
                fileOutputStream.write(buf);
                fileOutputStream.close();
                audioQueue.add(outputFile + "Received" + fileNumberAdding+ ".3gp");
                System.out.println("Converted into 3gp");

                    if(!myMediaPlayer.isPlaying()){
                        System.out.println("Came into this loop");
                        String temp = audioQueue.remove();
                        myMediaPlayer.reset();
                        myMediaPlayer.setDataSource(temp);
                        System.out.println("Diladaraa..");
                        myMediaPlayer.prepare();
                        System.out.println("Sajda..");
                        myMediaPlayer.start();
                        System.out.println("Exited this loop");
                    }
                    else{
                        System.out.println("Already playing, do something else");
                        System.out.println(audioQueue.size());
                    }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }
    }

    private class sendUDPTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            while(sendingAudioQueue.size()!=0) {
                try {
                    String outputFileSending = sendingAudioQueue.remove();
                    File file = new File(outputFileSending);
                    byte[] fileSendBuffer = new byte[(int) file.length()];
                    FileInputStream fileInputStream = new FileInputStream(file);
                    fileInputStream.read(fileSendBuffer);
                    fileInputStream.close();
                    DatagramPacket sendPacket = new DatagramPacket(fileSendBuffer, fileSendBuffer.length, inetAddress, 4444);
                    clientSocket.send(sendPacket);
                } catch (IOException e) {
                    System.out.println("IOException in sending");
                }
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

    public void clickedStop(View view){
        stopClicked = true;
    }

}




