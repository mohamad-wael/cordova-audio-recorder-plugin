# Cordova audio recorder plugin

A Cordova plugin to record audio .

```
cordova plugin add https://github.com/mohamad-wael/cordova-audio-recorder-plugin.git
```

## Plugin Concepts

The plugin cordova audio recorder  , can be used to record audio , on android and iOS . This plugin , provides an **api , which is accessible** , using the global object cordova.plugins.audioRecorder .

The api , **consists of two functions** , one for performing the recording , and one for stopping it .

For recording , the `cordova.plugins.audioRecorder.audioCapture_Start` function , can be used , it has the following signature :

```
cordova.plugins.audioRecorder.audioCapture_Start(audioCapture_Ended , audioCapture_Failed , audioCapture_duration )
/*
audioCapture_duration : 
    Number of seconds to perform audio
    recording . If non is provided , 
    the default is 60 seconds . 
audioCapture_Ended : 
    call back function , called when the
    amount of time allocated for recording , 
    audioCapture_duration , has elapsed . 
    It receives as argument , a String 
    containing the path of the recorded 
    m4a audio file .
    If cordova.plugins.audioRecorder.audioCapture_Stop 
    is  called , while performing recording , 
    then the registered methods ,
    using this function , are not called  , 
    instead the registered functions , using 
    cordova.plugins.audioRecorder.audioCapture_Stop
    are called .    
audioCapture_Failed : 
    If any error happens during the 
    act of performing the recording , 
    this method is called . It is passed.   
    a String , detailing the error .*/
```

To **stop the recording**, it is either stopped automatically , after the set amount of time has elapsed , or it can be stopped at any time , using the cordova.plugins.audioRecorder.audioCapture_Stop method ,which has the following signature :
```
cordova.plugins.audioRecorder.audioCapture_Stop(audioCapture_Ended , audioCapture_Failed )
/*
audioCapture_Ended : 
   call back function , called when successfully , 
   the audioCapture_Stop function , stopped the
   recording of the audio file . 
   audioCapture_Ended receives as argument , a 
   String containing the path to the recorded file . 
audioCapture_Failed : 
   Called when performing the recording has 
   failed . 
   It receives as an argument , a String , detailing 
   the error .*/
```

The **recorded audio file** is an m4a audio file , it is saved in the cache directory , of an application . The cache directory , might be emptied by the system , on low storage . Hence , for permanent storage , the recorded files , must be copied elsewhere .

**For iOS** , in the config.xml file , found in the root of the application , the following must be added , in between the widget element :
```
<edit-config file="*-Info.plist" mode="merge" target="NSMicrophoneUsageDescription">
    <string>Describe why microphone permission is needed</string>
</edit-config>
```
**For iOS** , also , and to allow recording to continue in the background , you should add UIBackgroundModes , in the config.xml file :
```
<platform name="ios">
...
    <config-file parent="UIBackgroundModes" platform="ios" target="*-Info.plist">
        <array>
            <string>audio</string>
        </array>
    </config-file>
...
</platform>
```

For **android** , the plugin can record , even when the application is destroyed , or is running in the background , nothing is to be configured . When the application is recreated after being destroyed , just call the audioCapture_Stop method , on the deviceready event , to get the recording that has happened , while the application was destroyed .

## Demo app

A tutorial , about creating a demo application , can be found [here](https://twiserandom.com/cordova/cordova-audio-recorder-plugin-tutorial/#Plugin_demo) .

