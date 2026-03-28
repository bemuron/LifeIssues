import 'dart:io';

class AdHelper {
  static String get bannerAdUnitId {
    if (Platform.isAndroid) {
      // Test ID during development
      //return 'ca-app-pub-3940256099942544/6300978111';
      // Replace with your actual ID for production:
       return 'ca-app-pub-3075330085087679/2977421942';
    } else if (Platform.isIOS) {
      // Test ID during development
      return 'ca-app-pub-3940256099942544/2934735716';
      // Replace with your actual ID for production:
      // return 'YOUR_IOS_AD_UNIT_ID';
    } else {
      throw UnsupportedError('Unsupported platform');
    }
  }
}