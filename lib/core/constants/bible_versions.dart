class BibleVersions {
  static const Map<String, String> versions = {
    'kjv': 'King James Version',
    'msg': 'The Message',
    'amp': 'Amplified Bible',
  };

  static String getVersionName(String code) {
    return versions[code] ?? 'King James Version';
  }
}