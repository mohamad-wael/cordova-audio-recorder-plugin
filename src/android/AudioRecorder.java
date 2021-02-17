package com .twiserandom .cordova .plugin .audiorecorder;



import android .content .BroadcastReceiver;
import android .content .Context;
import android .content .Intent;
import android .content .IntentFilter;
import android.content.SharedPreferences;
import android .content .pm .PackageManager;

import android .Manifest;

import android .support .v4 .content .LocalBroadcastManager;

import org .apache .cordova .CallbackContext;
import org .apache .cordova .CordovaInterface;
import org .apache .cordova .CordovaPlugin;
import org .apache .cordova .CordovaWebView;
import org .apache .cordova .PluginResult;

import org .json .JSONArray;
import org .json .JSONException;

import static android.content.Context.MODE_PRIVATE;

public class
            AudioRecorder extends CordovaPlugin {

    /*Fields  */
    private static final String fnlStr_debug_tag = "Audio Recorder Debug ";
    final String fnlStr_abrupt_term_recording = "abrupt_term_recorder";

    private String audioRecorder_Action;
    String [ ] audioRecorder_Permissions = {
            Manifest .permission.RECORD_AUDIO  };
    private int audioCapture_duration;

    private Context context_application ;
    private CallbackContext callbackContext; /*Fields  */

    /*Functions  */
    @Override
    public void
                initialize (CordovaInterface cordova, CordovaWebView webView ){
        super .initialize (cordova , webView );
        context_application = cordova .getActivity ( ) .getApplicationContext ( );
        register_Broadcast_Receiver ( ); }


    @Override
    public boolean
                execute (String action ,
                         JSONArray args ,
                         CallbackContext callbackContext )
                        throws JSONException {

        if(!action .equals ("audioCapture_Start" ) && !action .equals ("audioCapture_Stop" ) )
            return false;

        this .callbackContext = callbackContext ;
        this .audioRecorder_Action = action;
        this .audioCapture_duration = args .optInt(0 , -1 );

        if(!cordova .hasPermission (Manifest .permission .RECORD_AUDIO ) ){
            cordova .requestPermissions (this , 0 , audioRecorder_Permissions ); }
        else
            audioCapture_Call_Action( );
        return true; }

    public void
                onRequestPermissionResult (int requestCode ,
                                          String[ ] permissions ,
                                          int[ ] grantResults ) throws JSONException{

        for (int r : grantResults ){
            if (r == PackageManager .PERMISSION_DENIED ){
                callbackContext .sendPluginResult (new PluginResult (PluginResult .Status .ERROR, "Permission Denied" ) );
                return; } }
        audioCapture_Call_Action( ); }

    public void
                register_Broadcast_Receiver ( ){
        AudioRecorder_Receiver audioRecorder_receiver = new AudioRecorder_Receiver ( );
        IntentFilter intentFilter = new IntentFilter ("audio recording stopped" );
        LocalBroadcastManager .getInstance (context_application )
                .registerReceiver (audioRecorder_receiver , intentFilter ); }

    class
                AudioRecorder_Receiver extends BroadcastReceiver {
        @Override
        public void
                    onReceive (Context context, Intent intent ){
            String cause = intent .getStringExtra ("cause" );
            String msg = intent .getStringExtra ("msg" );
            String recovered_file_path   = get_Recovered_Recording_PAth ( );
            switch (cause  ){
                case "failure" :
                    cordova .getThreadPool ( ) .execute (new Runnable ( ){
                        public void run() {
                            callbackContext .error(msg ); }});
                    break;
                case "success" :
                    cordova .getThreadPool ( ) .execute (new Runnable ( ){
                        public void run( ){
                            callbackContext .success(msg ); }});
                    break; } }}

    public void
                audioCapture_Call_Action( ){
        Intent intent;
        intent = new Intent(context_application , AudioRecorder_Service .class);
        String recovered_file_path   = get_Recovered_Recording_PAth ( );
        switch(audioRecorder_Action ){
            case "audioCapture_Start":
                intent .putExtra ("do" , "Record Sound" );
                intent .putExtra ("audio capture duration" , audioCapture_duration );
                context_application .startService (intent );
                break;
            case "audioCapture_Stop":
                if (recovered_file_path != null ){
                    cordova .getThreadPool ( ) .execute (new Runnable ( ){
                        public void run( ){
                            callbackContext .success(recovered_file_path ); }}); }
                else{
                    intent .putExtra ("do" , "Record Stop" );
                    context_application .startService (intent ); }
                break; } }

    public String
                get_Recovered_Recording_PAth ( ){
        SharedPreferences sharedPreferences = context_application .getSharedPreferences (fnlStr_abrupt_term_recording , MODE_PRIVATE );
        String recovered_file_path   = sharedPreferences .getString (fnlStr_abrupt_term_recording  , null );
        SharedPreferences .Editor editor = sharedPreferences .edit ( );
        editor .clear ( ) ;
        editor .commit ( );
        return recovered_file_path; } }