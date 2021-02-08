package com.twiserandom.cordova.plugin.audiorecorder;

import android.content.pm.PackageManager;
import android.Manifest;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.util.Log;


import java.util.UUID;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

public class AudioRecorder extends CordovaPlugin {

    /*Fields  */
    private static final String TAG = "AudioRecorderAPI";

    private String audioRecorder_Action;
    String [] audioRecorder_Permissions = {
        Manifest.permission.RECORD_AUDIO  }; 

    private MediaRecorder audioCapture_recorder;
    private String audioCapture_fileName;
    private int audioCapture_duration;
    private CountDownTimer audioCapture_countDownTimer;

    private CallbackContext callbackContext; /*Fields  */


    /*Functions  */
    @Override
    public boolean execute(String action,
                            JSONArray args,
                            CallbackContext callbackContext )
                            throws JSONException {

        if(!action.equals("audioCapture_Start" ) && !action.equals("audioCapture_Stop" ) )
            return false;

        this.callbackContext = callbackContext ;
        this.audioRecorder_Action = action;
        this.audioCapture_duration = args.optInt(0 , -1 );

        if(!cordova.hasPermission(Manifest.permission.RECORD_AUDIO )){
            cordova.requestPermissions(this, 0, audioRecorder_Permissions ); }
        else
            audiocapture_Call_Action( );
        return true; } 

    public void onRequestPermissionResult(int requestCode ,
                                    String[ ] permissions ,
                                    int[ ] grantResults ) throws JSONException{

        for(int r:grantResults ){
            if(r == PackageManager.PERMISSION_DENIED ){
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Permission Denied" ) );
                    return; } }
        audiocapture_Call_Action( ); }

    public void audiocapture_Call_Action( ){
        switch(audioRecorder_Action ){
            case "audioCapture_Start":
                audioCapture_Start( );
                break;
            case "audioCapture_Stop":
                audioCapture_Stop( );
                break; } }

    private void audioCapture_Start( ){
        try {
            audioCapture_fileName = cordova.getActivity().getApplicationContext().getCacheDir( ).getAbsoluteFile( )
                                    + "/"
                                    + UUID.randomUUID( ).toString( ) + ".m4a";
            audioCapture_recorder = new MediaRecorder( );
            audioCapture_recorder.setAudioSource(MediaRecorder.AudioSource.MIC ) ;
            audioCapture_recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4 );
            audioCapture_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC );
            audioCapture_recorder.setAudioSamplingRate(44100 );
            audioCapture_recorder.setAudioChannels(2 );
            audioCapture_recorder.setAudioEncodingBitRate(32000 );
            audioCapture_recorder.setOutputFile(audioCapture_fileName );
            audioCapture_recorder.prepare( );
            audioCapture_recorder.start( );

            audioCapture_countDownTimer = new CountDownTimer(audioCapture_duration * 1000 , 1000 ){
                public void onTick(long millisUntilFinished ) { }
                public void onFinish( ){
                    audioCapture_Stop( ); }};
            audioCapture_countDownTimer.start( ); }
        catch(Exception exception){
            Log.d(TAG, Log.getStackTraceString(exception ) );
            cordova.getThreadPool( ).execute(new Runnable( ){
                public void run() {
                    callbackContext.error(exception.getMessage( ) );}});}}


    private void audioCapture_Stop( ){
        audioCapture_countDownTimer.cancel( );
        try{
            audioCapture_recorder.stop( );
            audioCapture_recorder.release( );
            cordova.getThreadPool().execute(new Runnable( ){
                public void run( ){
                    callbackContext.success(audioCapture_fileName );}});}
        catch(Exception exception ){
            Log.d(TAG, Log.getStackTraceString(exception ) );
            cordova.getThreadPool( ).execute(new Runnable( ){
                public void run() {
                    callbackContext.error(exception.getMessage( ) );}});}}}

