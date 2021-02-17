package com.twiserandom.cordova.plugin.audiorecorder;

import android .app .Notification;
import android .app .NotificationChannel;
import android .app .NotificationManager;
import android .app .Service;

import android .content .Intent;
import android .content .SharedPreferences;

import android .graphics .Bitmap;
import android .graphics .Canvas;
import android .graphics .Color;
import android .graphics .Paint;

import android .media.MediaRecorder;

import android .os .CountDownTimer;
import android .os .IBinder;
import android .support .v4 .content .LocalBroadcastManager;

import android .util .Log;

import java .util .UUID;

public class
            AudioRecorder_Service extends Service{

    final String fnlStr_debug_tag = "Service Recorder Debug";

    LocalBroadcastManager localbroadCast_Manager;

    final String fnlStr_abrupt_term_recording = "abrupt_term_recorder";
    boolean stop_recording = false;
    int audioCapture_duration ;
    MediaRecorder audioCapture_recorder;
    CountDownTimer audioCapture_countDownTimer;
    String audioCapture_fileName ;
    Thread thr_recordSound;

    IBinder binder ;

    @Override
    public void
                onCreate() {
        localbroadCast_Manager = LocalBroadcastManager
                .getInstance (AudioRecorder_Service .this .getApplicationContext ( ) ); }

    @Override
    public int
                onStartCommand (Intent intent , int flags , int startId ){
        switch (intent .getStringExtra ("do" ) ){
            case "Record Stop" :
                if (thr_recordSound != null  ){
                    stop_recording = true; }
                else {
                    send_Action_Error ("Not recording anything" );
                    stopSelf ( ); }
                break;
            case "Record Sound" :
                if (thr_recordSound == null  ){
                    audioCapture_duration = intent .getIntExtra ("audio capture duration" , 60 );
                    thr_recordSound = new Thread (new Record_Sound ( ) );
                    thr_recordSound .run ( ); }
                else {
                    send_Action_Error ("Already Recording" );  }
                break; }
        return START_NOT_STICKY; }

        private void send_Action_Error (String msg ){
            Intent returnIntent = new Intent ("audio recording stopped" );
            returnIntent .putExtra ("cause"  , "failure" );
            returnIntent .putExtra ("msg"  , msg );
            localbroadCast_Manager .sendBroadcast (returnIntent ); }

    @Override
    public IBinder
                onBind (Intent intent ){
        return binder; }

    @Override
    public void
                onDestroy ( ){
        if (audioCapture_countDownTimer != null  ) {
            //low memory , unforeseen events
            audioCapture_countDownTimer .cancel ( );
            try{
                audioCapture_recorder .stop ( );
                audioCapture_recorder .release ( );
                save_Abrupt_Term_Recording ( ); }
            catch (Exception exception ){
                Log .d (fnlStr_debug_tag , exception .toString ( ) ); }}}


    public void save_Abrupt_Term_Recording ( ){
        SharedPreferences sharedPreferences = getSharedPreferences (fnlStr_abrupt_term_recording , MODE_PRIVATE );
        SharedPreferences .Editor editor = sharedPreferences .edit ( );
        editor .putString (fnlStr_abrupt_term_recording , audioCapture_fileName );
        editor .commit ( );  }



    /* Record sound class
     */
    class
                Record_Sound implements Runnable{
        Intent intent ;

        public
                    Record_Sound ( ){
            intent = new Intent ("audio recording stopped" ); }

        @Override
        public void
                    run ( ) {
            start_Recording ( ); }

        public void
                    start_Recording ( ) {
            try {
                //Start recording
                audioCapture_fileName = getCacheDir( ) .getAbsoluteFile( )
                        + "/"  + UUID .randomUUID( ) .toString( ) + ".m4a";
                audioCapture_recorder = new MediaRecorder ( );
                audioCapture_recorder .setAudioSource (MediaRecorder .AudioSource .MIC ) ;
                audioCapture_recorder .setOutputFormat (MediaRecorder .OutputFormat .MPEG_4 );
                audioCapture_recorder .setAudioEncoder (MediaRecorder .AudioEncoder .AAC );
                audioCapture_recorder .setAudioSamplingRate (44100 );
                audioCapture_recorder .setAudioChannels (2 );
                audioCapture_recorder .setAudioEncodingBitRate (32000 );
                audioCapture_recorder .setOutputFile (audioCapture_fileName );
                audioCapture_recorder .prepare ( );
                audioCapture_recorder .start ( );

                //Cancel recording after audioCapture_duration , or on interrupt
                //exceptions before audioCapture_countDownTimer , not nul
                audioCapture_countDownTimer = new CountDownTimer (audioCapture_duration * 1000 , 1000 ){
                    public void
                                onTick (long millisUntilFinished ){
                        if(stop_recording  && audioCapture_countDownTimer != null ){//android 4.4
                            audioCapture_countDownTimer .cancel ( ) ;
                            stop_Recording ( ); }}
                    public void
                                onFinish ( ){
                        stop_Recording ( ); }};
                audioCapture_countDownTimer .start( );
                startForeground (2 , sound_Recording_Notification ( ) ); }

            catch (Exception exception ){
                intent .putExtra ("cause"  , "failure" );
                intent .putExtra ("msg"  , exception .toString ( ) );
                localbroadCast_Manager .sendBroadcast (intent );
                stopSelf ( ); }}

        public void
                    stop_Recording ( ){
            try {
                audioCapture_countDownTimer = null ;
                audioCapture_recorder .stop ( );
                audioCapture_recorder .release ( );

                intent .putExtra ("cause"  , "success" );
                intent .putExtra ("msg"  , audioCapture_fileName ); }
            catch (Exception exception ){
                intent .putExtra ("cause"  , "failure" );
                intent .putExtra ("msg"  , exception .toString ( ) ); }
            finally {
                save_Abrupt_Term_Recording ( );
                stopForeground (true );
                localbroadCast_Manager .sendBroadcast (intent );
                stopSelf ( ); }}


        public Bitmap
                    create_Notification_Bitmap (){
            int notification_large_icon_width = (int ) getResources ( ) .getDimension (android .R .dimen .notification_large_icon_width );
            int notification_large_icon_height = (int ) getResources ( ) .getDimension (android .R .dimen .notification_large_icon_height );
            int diameter = notification_large_icon_width < notification_large_icon_height ? notification_large_icon_width : notification_large_icon_height;
            float radius = diameter / 2.0f;
            Bitmap bitmap = Bitmap
                    .createBitmap ( notification_large_icon_width , notification_large_icon_height , Bitmap .Config .ARGB_8888);
            bitmap .eraseColor(Color .WHITE );
            Canvas canvas = new Canvas (bitmap );
            Paint paint = new Paint ( );
            paint .setColor(Color .RED );
            canvas .drawCircle(notification_large_icon_width /2.0f , notification_large_icon_height /2.0f , radius , paint );
            return bitmap; }

        public Notification
                    sound_Recording_Notification ( ){
            // Create notification channel
            NotificationChannel notification_channel = null;
            String notification_channel_id = "cordova-audio-recorder-plugin";
            String notification_channel_name = "cordova audio recorder plugin";
            String notification_channel_description = "Cordova audio recorder plugin";

            String notification_content_title = "Recording audio";
            String notification_content_text = "Sound is being recorded";

            Bitmap notification_bitmap;

            if (android .os .Build .VERSION .SDK_INT >= android .os .Build .VERSION_CODES .O) {
                notification_channel = new NotificationChannel(
                        notification_channel_id , notification_channel_name ,
                        NotificationManager .IMPORTANCE_DEFAULT );
                notification_channel .setDescription (notification_channel_description );
                NotificationManager notificationManager = getSystemService (NotificationManager .class );
                notificationManager .createNotificationChannel (notification_channel );  }

            notification_bitmap = create_Notification_Bitmap ();

            // Create the notification
            Notification .Builder notification_builder ;

            if (android .os .Build .VERSION .SDK_INT >= android .os .Build .VERSION_CODES .O ){
                notification_builder = new Notification
                        .Builder (AudioRecorder_Service .this , notification_channel_id )
                        .setSmallIcon(android .R .drawable .ic_media_play)
                        .setLargeIcon (notification_bitmap )
                        .setContentTitle (notification_content_title )
                        .setContentText (notification_content_text ); }
            else {
                notification_builder = new Notification
                        .Builder (AudioRecorder_Service .this  )
                        .setSmallIcon(android .R .drawable .ic_media_play)
                        .setLargeIcon (notification_bitmap )
                        .setContentTitle (notification_content_title )
                        .setContentText (notification_content_text ); }
            return notification_builder .build ( ); }}}