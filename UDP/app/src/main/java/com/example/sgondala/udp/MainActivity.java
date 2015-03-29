package com.example.sgondala.udp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
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
import java.util.Arrays;
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
    Boolean stopClicked = false;
    Boolean tempToBreak = false;

    int RecordingRate = 44100;
    int Channel = AudioFormat.CHANNEL_IN_MONO;
    int Format = AudioFormat.ENCODING_PCM_16BIT;

    int BufferSize = AudioRecord.getMinBufferSize(RecordingRate,Channel, Format);
    int BufferElementsToRecord = 1024;
    int BytesPerElement = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
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
        */
        //myAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RecordingRate, Channel, Format, 2048);

        /*
        byte[] byf = new byte[100];
        int ln = byf.length;
        System.out.println()
         DatagramPacket dp;
        dp.getLength()
        */


        System.out.println(BufferSize);
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

    private static int[] mSampleRates = new int[] { 44100, 22050,11025, 8000  };

    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        //Log.d(C.TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "  + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a successd

                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        //Log.e(C.TAG, rate + "Exception, keep trying.",e);
                    }
                }
            }
        }
        return null;
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

        //final MediaRecorder myAudioRecorder = new MediaRecorder();

        //final AudioRecord
        new Thread(new Runnable() {
            @Override
            public void run() {
                AudioRecord myAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RecordingRate, Channel, Format, 2048);

                AudioRecord recorder = findAudioRecord();
                System.out.println(recorder.getAudioFormat());
                System.out.println(recorder.getChannelConfiguration());
                System.out.println(recorder.getSampleRate());
                recorder.startRecording();
                //myAudioRecorder.startRecording();
                byte[] tempByteArray = new byte[2048];
                //byte f = 0;
                //Arrays.fill(tempByteArray, f);
                while(!stopClicked){
                    try {

                        int out = recorder.read(tempByteArray, 0, 2048);
                        System.out.println(out);
                        DatagramPacket sendPacket = new DatagramPacket(tempByteArray, out, inetAddress, 4444);
                        clientSocket.send(sendPacket);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                System.out.println("Came out");
                /*
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
                                        System.out.println("Recorded successfully !!!");
                                        File file = new File(outputFileSending);
                                        System.out.println("Took file");
                                        new sendUDPTask().execute(file);
                                        System.out.println("Sent a packet...");
                                        //Toast.makeText(getApplicationContext(), "Sent successfully", Toast.LENGTH_SHORT).show();
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

                */
            }
        }).start();
    }

    //public int

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

    private class sendUDPTask extends AsyncTask<File, Void, Void>{

        @Override
        protected Void doInBackground(File... params) {
            try{
                File file = params[0];
                //System.out.println((int) file.length());
                byte[] fileSendBuffer = new byte[(int) file.length()];
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(fileSendBuffer);
                fileInputStream.close();
                DatagramPacket sendPacket = new DatagramPacket(fileSendBuffer, fileSendBuffer.length, inetAddress, 4444);
                clientSocket.send(sendPacket);
                tempToBreak = true;
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

    public void clickedStop(View view){
        stopClicked = true;
    }

}




