#import<Cordova/CDVPlugin.h>
#import <AVFoundation/AVFoundation.h>

@interface AudioRecorder: CDVPlugin{

    BOOL audioSession_active;

    AVAudioRecorder *audioCapture_recorder; 
    NSDictionary *audioCapture_settings; 
    BOOL audioCapture_recording;

    NSString* callbackId; 

    NSURL *app_tmpDirectory_URL; }

- (void ) audioCapture_Start: (CDVInvokedUrlCommand* ) command;
- (void ) audioCapture_Stop: (CDVInvokedUrlCommand* ) command;
- (void ) audioSession_Activate;
@end


