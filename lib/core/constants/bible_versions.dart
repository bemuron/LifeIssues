class BibleVersions {
  static const Map<String, String> versions = {
    'KJV': 'King James Version',
    'MSG': 'The Message',
    'AMP': 'Amplified Bible',
    'NIV': 'New International Version',
    'ESV': 'English Standard Version',
    'NLT': 'New Living Translation',
    'NKJV': 'New King James Version',
  };

  /// Returns the full name for a version code (case-insensitive).
  /// Falls back to the code itself if unknown.
  static String getVersionName(String code) {
    return versions[code.toUpperCase()] ?? code.toUpperCase();
  }
}
