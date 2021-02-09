#import "AudioRecorder.h"

@implementation AudioRecorder

- (void ) pluginInitialize{
    audioCapture_settings = @{AVFormatIDKey: [NSNumber numberWithInt:kAudioFormatMPEG4AAC ] ,
                            AVSampleRateKey: @44100.0 ,
                            AVNumberOfChannelsKey: @2 ,
                            AVEncoderBitRateKey: @320000 ,
                            AVEncoderAudioQualityKey : [NSNumber numberWithInt: AVAudioQualityMax ] }; 
    audioSession_active = NO;
    audioCapture_recording = NO;
    app_tmpDirectory_URL = [NSURL fileURLWithPath: NSTemporaryDirectory( ) isDirectory: YES ]; }


- (void ) audioSession_Activate{
    NSError *err = nil;
    CDVPluginResult* pluginResult = nil;
    audioSession_active = NO;
    AVAudioSession *audioCapture_audioSession  = [AVAudioSession sharedInstance ];

    [audioCapture_audioSession
            setCategory:AVAudioSessionCategoryPlayAndRecord
            mode:AVAudioSessionModeDefault
            options: AVAudioSessionCategoryOptionMixWithOthers | AVAudioSessionCategoryOptionAllowBluetooth | AVAudioSessionCategoryOptionDefaultToSpeaker
            error:&err ];
    if(err ){
        NSLog(@"audioCapture_audioSession setCategory : %@", err );
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Failed to set audio session category." ];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId ];
        return; }
            
    err = nil;
    [audioCapture_audioSession setActive: YES error: &err ];
    if(err ){
        NSLog(@"audioCapture_audioSession setActive : %@", err );
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Failed to set audio session active." ];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId ];
        return; }

    NSNotificationCenter *nsNotification_center = [NSNotificationCenter defaultCenter ];
    [nsNotification_center 
                addObserver: self 
                selector: @selector(sessionDidInterrupt: ) 
                name: AVAudioSessionInterruptionNotification 
                object: nil ];
    audioSession_active = YES;
    err = nil; }


- (void ) sessionDidInterrupt: (NSNotification * )notification{
    switch ([notification.userInfo[AVAudioSessionInterruptionTypeKey ] intValue ] ){
        case AVAudioSessionInterruptionTypeBegan:
            NSLog(@"AVAudioSession_interruption_typeBegan ." );
            if(audioCapture_recording )
                [audioCapture_recorder pause ];
            else 
                audioSession_active = NO;
            break;

        case AVAudioSessionInterruptionTypeEnded:
            NSLog(@"AVAudioSession_interruption_typeEnded ");
            if(audioCapture_recording ){
                [self audioSession_Activate ];
                if(audioSession_active ){
                   [audioCapture_recorder record ]; } }
            break; } }


- (void ) audioCapture_Start: (CDVInvokedUrlCommand* ) command{
    if(!audioSession_active ){
        [self audioSession_Activate ]; }
    if(audioSession_active ){
        callbackId = command.callbackId;
        NSNumber* audioCapture_duration = [command.arguments objectAtIndex:0];

        [self.commandDelegate runInBackground:^{
            NSError *err = nil;
            CDVPluginResult* pluginResult = nil;
            
            NSString *audioCapture_fileName = [NSString stringWithFormat: @"%@%s",  [[NSProcessInfo processInfo ] globallyUniqueString ] , ".m4a" ];
            NSURL *audioCapture_fileURL = [app_tmpDirectory_URL URLByAppendingPathComponent: audioCapture_fileName ];
            
            audioCapture_recorder = [[AVAudioRecorder alloc ] initWithURL: audioCapture_fileURL settings: audioCapture_settings error: &err ];
            if (err ){
                NSLog(@"audioCapture_recorder alloc : %@", err );
                pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR messageAsString: @"Failed to initialize an audio recorder ." ];
                [self.commandDelegate sendPluginResult: pluginResult callbackId: callbackId ];
                return; }
            err = nil;

            audioCapture_recorder.delegate = self;
            
            if(![audioCapture_recorder prepareToRecord ] ){
                NSLog(@"Failed to prepare for recording ." );
                pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR messageAsString: @"Failed to prepare for recording ." ];
                [self.commandDelegate sendPluginResult: pluginResult callbackId: callbackId ];
                return; }
            
            if (![audioCapture_recorder recordForDuration: (NSTimeInterval )[audioCapture_duration intValue ] ]){
                NSLog(@"Faild to record for duration ." );
                pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR messageAsString: @"Failed to record for duration ." ];
                [self.commandDelegate sendPluginResult: pluginResult callbackId: callbackId ];
                return; } 
            audioCapture_recording = YES; } ];}}


- (void ) audioCapture_Stop: (CDVInvokedUrlCommand* ) command{
    [audioCapture_recorder stop ]; }


- (void ) audioRecorderDidFinishRecording: (AVAudioRecorder * ) recorder successfully: (BOOL )flag{
    audioCapture_recording = NO;    
    CDVPluginResult* pluginResult = nil;

    if(!flag ){
        NSLog(@"Audio encoding error , finished recording ." );
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR messageAsString: @"Audio enconding error , finished recording ." ];
        [self.commandDelegate sendPluginResult: pluginResult callbackId: callbackId ]; }
    else{
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString: recorder.url.absoluteString ];
        [self.commandDelegate sendPluginResult: pluginResult callbackId: callbackId ]; }}
@end

