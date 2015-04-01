package com.example.sgondala.udp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
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

import java.net.MalformedURLException;
/*
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
*/

public class MainActivity extends ActionBarActivity {

    /*  Old ones, Should remove them
    Boolean isServer = false;
    DatagramSocket serverSocket = null;
    int sizeOfAudio = 10240;
    String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myRecording";
    MediaPlayer myMediaPlayer = new MediaPlayer();
    int fileNo = 0;
    int sendFileNo = 0;
    Queue<String> audioQueue = new LinkedList<String>();
    Boolean tempToBreak = false;

    int RecordingRate = 44100;
    int Channel = AudioFormat.CHANNEL_IN_MONO;
    int Format = AudioFormat.ENCODING_PCM_16BIT;

    int BufferSize = AudioRecord.getMinBufferSize(RecordingRate,Channel, Format);
    int BufferElementsToRecord = 1024;
    int BytesPerElement = 2;
    */

    //Client variables
    private static final int RECORDING_RATE = 44100;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder;
    private static int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            RECORDING_RATE, CHANNEL, FORMAT);
    Boolean stopClicked = false;
    DatagramSocket clientSocket = null;
    InetAddress inetAddress = null;

    //Server variables

    int streamType = 0; //Stream_VoiceCall, Should check other possibilitites
    int sampleRate = 44100; //Should see if
    int channel = AudioFormat.CHANNEL_IN_MONO;
    int format = AudioFormat.ENCODING_PCM_16BIT;
    int mode = AudioTrack.MODE_STREAM;
    int serverBufferSize = 4096;
    AudioTrack myAudioTrack;

    Boolean isServer = false;
    DatagramSocket serverSocket = null;
    static int bufferSize = 9728;
    Queue<byte[]> packetQueue = new LinkedList<byte[]>();
    Thread playerThread;

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

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDING_RATE, CHANNEL, FORMAT, BUFFER_SIZE * 10);
        System.out.println(recorder.getAudioFormat());
        System.out.println(recorder.getChannelConfiguration());
        System.out.println(recorder.getSampleRate());

        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[8000]; //
                DatagramPacket packet; // Reusable packet

                recorder.startRecording();
                while(!stopClicked){
                    try {
                        Thread.sleep(90);
                        int out = recorder.read(buffer, 0, buffer.length);
                        System.out.println("out is "+out);
                        packet = new DatagramPacket(buffer, out, inetAddress, 4444);
                        clientSocket.send(packet);
                    } catch (IOException e1) {
                        System.out.println("Error in sending here");
                        e1.printStackTrace();
                    }

                    catch (InterruptedException e1) {
                        System.out.println("Yoyo");
                    }

                }
                System.out.println("Came out");
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

        myAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 44100, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 16000,AudioTrack.MODE_STREAM);

        myAudioTrack.play();
        playerThread = new Thread(m_packetCollector);
        playerThread.start();
        new receiveUDPTask().execute();
    }


    Runnable m_packetCollector = new Runnable()
    {
        public void run()
        {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            byte[] noiseData = null;
            while(true)
            {
                if (!packetQueue.isEmpty()) {
                    noiseData = packetQueue.remove();
                    myAudioTrack.write(noiseData, 0, noiseData.length);
                    System.out.println("Wrote new");
                }
            }
        }
    };

    private class receiveUDPTask extends AsyncTask<Void, Void, Void>{

        byte[] buf = new byte[8000];
        DatagramPacket dp = new DatagramPacket(buf, 8000);

        @Override
        protected Void doInBackground(Void... params) {

            while(true){
                System.out.println("Listening for packets");
                try {
                    serverSocket.receive(dp);
                    System.out.println("Got packet!!!!!");
                    System.out.println(dp.getLength());
                    packetQueue.add(buf);
                    System.out.println("Added");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    /*
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
    */




