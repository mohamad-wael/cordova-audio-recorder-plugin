#import<Cordova/CDVPlugin.h>
#import <AVFoundation/AVFoundation.h>

@interface AudioRecorder: CDVPlugin{
    AVAudioSession *audioCapture_audioSession;
    AVAudioRecorder *audioCapture_recorder; 
    NSString* callbackId; 
    NSDictionary *audioCapture_settings; 
    NSURL *app_tmpDirectory_URL; }

- (void )audioCapture_Start:(CDVInvokedUrlCommand* )command;
- (void )audioCapture_Stop:(CDVInvokedUrlCommand* )command;
@end


