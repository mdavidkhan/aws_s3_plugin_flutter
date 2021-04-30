# aws_s3_plugin_flutter

This plugin developed to make it easy to upload  file(s) to AWS S3  Private buckets without
writing Android, and IOS code separately using method channel.

DISCLAIMER: This is not an AWS officially released plugin but this plugin uses
AWS official Android native and IOS native AWS plugins (So nothing to be worried).


Contributors are highly welcome.

To use this package, you have to create a instance of `AwsS3PluginFlutter` with parameters like below code snippet:

```
  AwsS3PluginFlutter awsS3 = AwsS3PluginFlutter(
  awsFolderPath: "your aws folder path",
  file: "file is of type File",
  fileNameWithExt: "file name",
  region: "your region using enum Regions",
  AWSAccess: "your AWSAccessKey",
  AWSSecret: "Your AWSSecretKey",
  bucketName: "your bucket name to upload");
  
```

`AwsS3PluginFlutter` class, parameters:

```
  final File file;
  final String fileNameWithExt;
  final String awsFolderPath;
  final String bucketName;

  AwsS3PluginFlutter awsS3 = AwsS3PluginFlutter(
  awsFolderPath: awsFolderPath,
  file: file,
  fileNameWithExt: fileNameWithExt,
  region: Regions.EU_WEST_2,
  AWSAccess: Constants.AWSAccessKey,
  AWSSecret: Constants.AWSSecretKey,
  bucketName: bucketName);
```
`AwsS3PluginFlutter` generate presigned URL

```

final String fileNameWithExt;
final String awsFolderPath;
final String bucketName;


  AwsS3PluginFlutter awsS3 = AwsS3PluginFlutter(
    awsFolderPath: awsFolderPath,
    fileNameWithExt: fileNameWithExt,
    region: Regions.EU_WEST_2,
    bucketName: bucketName,
    AWSAccess: Constants.AWSAccessKey,
    AWSSecret: Constants.AWSSecretKey,
  );
  String presigned = await awsS3.getPreSignedURLOfFile;
  
```

All the available regions from official Amazon AWS S3 android are supported in this Flutter plugin:

```
/// Enumeration of region names
enum Regions {
  GovCloud,
  US_GOV_EAST_1,
  US_EAST_1,
  US_EAST_2,
  US_WEST_1,
  US_WEST_2, ///Default: The default region of AWS Android SDK
  EU_WEST_1,
  EU_WEST_2,
  EU_WEST_3,
  EU_CENTRAL_1,
  EU_NORTH_1,
  AP_EAST_1,
  AP_SOUTH_1,
  AP_SOUTHEAST_1,
  AP_SOUTHEAST_2,
  AP_NORTHEAST_1,
  AP_NORTHEAST_2,
  SA_EAST_1,
  CA_CENTRAL_1,
  CN_NORTH_1,
  CN_NORTHWEST_1,
  ME_SOUTH_1
}
```

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our 
[online documentation](https://flutter.dev/docs), which offers tutorials, 
samples, guidance on mobile development, and a full API reference.
