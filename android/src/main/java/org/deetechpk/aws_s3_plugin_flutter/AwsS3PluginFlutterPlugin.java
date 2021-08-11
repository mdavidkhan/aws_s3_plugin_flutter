package org.deetechpk.aws_s3_plugin_flutter;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.CognitoCredentialsProvider;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import com.amazonaws.HttpMethod;

import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;


import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.shim.ShimPluginRegistry;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;


/** AwsS3PluginFlutterPlugin */
public class AwsS3PluginFlutterPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private static final String TAG = "awsS3Plugin";
  private static final String CHANNEL = "org.deetechpk/aws_s3_plugin_flutter";
  private static final String STREAM = "uploading_status";
  static final String METHOD_CALL_UPLOAD = "uploadToS3";
  static final String METHOD_CALL_PRESIGNED = "createPreSignedURL";
  private String AWSAccess;
  private String AWSSecret;
  private String filePath;
  private String awsFolder;
  private String fileNameWithExt;
  private MethodChannel.Result parentResult;
  private ClientConfiguration clientConfiguration;
  private TransferUtility transferUtility1;
  private String bucketName;
  private Context mContext;
  private EventChannel eventChannel;
  private MethodChannel methodChannel;
  private EventChannel.EventSink events;
  public AwsS3PluginFlutterPlugin() {
    filePath = "";
    awsFolder = "";
    fileNameWithExt = "";
    clientConfiguration = new ClientConfiguration();
  }

  public static void registerWith(PluginRegistry.Registrar registrar) {
    AwsS3PluginFlutterPlugin s3Plugin = new AwsS3PluginFlutterPlugin();
    s3Plugin.whenAttachedToEngine(registrar.context(), registrar.messenger());
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    whenAttachedToEngine(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger());

  }

  private void whenAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
    this.mContext = applicationContext;
    methodChannel = new MethodChannel(messenger, CHANNEL);
    eventChannel = new EventChannel(messenger, STREAM);
    eventChannel.setStreamHandler(this);
    methodChannel.setMethodCallHandler(this);

    Log.d(TAG, "whenAttachedToEngine");
  }


  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    mContext = null;
    methodChannel.setMethodCallHandler(null);
    methodChannel = null;
    eventChannel.setStreamHandler(null);
    eventChannel = null;

    Log.d(TAG, "onDetachedFromEngine");
  }

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result rawResult) {

    result = new MethodResultWrapper(rawResult);

    filePath = call.argument("filePath");
    Log.d(TAG, "onMethodCall : file path ==> : " + filePath);
    awsFolder = call.argument("awsFolder");
    fileNameWithExt = call.argument("fileNameWithExt");
    bucketName = call.argument("bucketName");
    AWSAccess = call.argument("AWSAccess");
    AWSSecret = call.argument("AWSSecret");
    switch (call.method) {
      case METHOD_CALL_UPLOAD:
        try {

          clientConfiguration.setConnectionTimeout(250000);
          clientConfiguration.setSocketTimeout(250000);
          AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials( AWSAccess,AWSSecret));
          transferUtility1 = TransferUtility.builder().context(mContext).awsConfiguration(AWSMobileClient.getInstance().getConfiguration()).s3Client(s3Client).build();
          sendImage();
        } catch (Exception e) {
          Log.e(TAG, "onMethodCall: exception: " + e.getMessage());
        }
        break;
      case METHOD_CALL_PRESIGNED:
        getPreSinedURL(call,result);
        break;
      default:
        result.notImplemented();
    }
  }
  private static class MethodResultWrapper implements MethodChannel.Result {
    private MethodChannel.Result methodResult;
    private Handler handler;

    MethodResultWrapper(MethodChannel.Result result) {
      methodResult = result;
      handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void success(final Object result) {
      handler.post(
              new Runnable() {
                @Override
                public void run() {
                  methodResult.success(result);
                }
              });
    }

    @Override
    public void error(
            final String errorCode, final String errorMessage, final Object errorDetails) {
      handler.post(
              new Runnable() {
                @Override
                public void run() {
                  methodResult.error(errorCode, errorMessage, errorDetails);
                }
              });
    }

    @Override
    public void notImplemented() {
      handler.post(
              new Runnable() {
                @Override
                public void run() {
                  methodResult.notImplemented();
                }
              });
    }
  }

  private void getPreSinedURL(@NonNull MethodCall call, @NonNull Result result){
    String reg = call.argument("region");
    assert reg != null;
    String objectKey = fileNameWithExt;
    if (awsFolder != null && !awsFolder.equals("")){
      objectKey = awsFolder + "/" + fileNameWithExt;
    }
    try {
      BasicAWSCredentials awsCreds = new BasicAWSCredentials( AWSAccess,AWSSecret);
      ClientConfiguration clientConfiguration = new ClientConfiguration();
      clientConfiguration.setSignerOverride("AWSS3V4SignerType");

      AmazonS3Client s3Client = new AmazonS3Client(
              awsCreds, clientConfiguration);
      String regionName = reg.replaceFirst("Regions.", "");
      Regions region = Regions.valueOf(regionName);
      s3Client.setRegion(com.amazonaws.regions.Region.getRegion(region));

      // For setting time to expire the link
      long ONE_MINUTE_IN_MILLIS=60000;//millisecs
      Calendar date = Calendar.getInstance();
      long t= date.getTimeInMillis();

      Date afterAddingFiveMins=new Date(t + (480 * ONE_MINUTE_IN_MILLIS)); // 8 hours

      GeneratePresignedUrlRequest generatePresignedUrlRequest =
              new GeneratePresignedUrlRequest(bucketName, objectKey)
                      .withMethod(HttpMethod.GET)
                      .withExpiration(afterAddingFiveMins);

      URL objectURL = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

      System.out.println("Pre-Signed URL: " + objectURL.toString());
      parentResult.success(objectURL.toString());
    } catch (Exception e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      parentResult.success(null);
      e.getMessage();
    }
  }
  private void sendImage() {
    String awsPath = fileNameWithExt;
    if (awsFolder != null && !awsFolder.equals("")) {
      awsPath = awsFolder + "/" + fileNameWithExt;
    }
    Log.d(TAG,"fileinfo:" + awsPath);
    TransferObserver transferObserver1 = transferUtility1
            .upload(bucketName, awsPath, new File(filePath), CannedAccessControlList.Private);

    transferObserver1.setTransferListener(new Transfer());
  }

//  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    this.events = events;
  }

//  @Override
  public void onCancel(Object arguments) {
    invalidateEventSink();
  }

  private void invalidateEventSink() {
    if (events != null) {
      events.endOfStream();
      events = null;
    }
  }

  class Transfer implements TransferListener {

    private static final String TAG = "Transfer";

//    @Override
    public void onStateChanged(int id, TransferState state) {
      switch (state) {
        case COMPLETED:
          Log.d(TAG, "onStateChanged: \"COMPLETED, " + fileNameWithExt);
          parentResult.success(fileNameWithExt);
          break;
        case WAITING:
          Log.d(TAG, "onStateChanged: \"WAITING, " + fileNameWithExt);
          break;
        case FAILED:
          invalidateEventSink();
          Log.d(TAG, "onStateChanged: \"FAILED, " + fileNameWithExt);
          parentResult.success(null);
          break;
        default:
          Log.d(TAG, "onStateChanged: \"SOMETHING ELSE, " + fileNameWithExt);
          break;
      }
    }

//    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

      float percentDoNef = ((float) bytesCurrent / (float) bytesTotal) * 100;
      int percentDone = (int) percentDoNef;
      Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percentDone + "%");

      if (events != null) {
        events.success(percentDone);
      }
    }

//    @Override
    public void onError(int id, Exception ex) {
      Log.e(TAG, "onError: " + ex);
      invalidateEventSink();
    }
  }
}
