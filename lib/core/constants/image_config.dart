// lib/core/constants/image_config.dart

class ImageConfig {
  // Base URLs for different image types
  // TODO: Replace these with your actual hosting URLs
  static const String issueImagesBaseUrl = 'https://www.emtechint.com/life_issues/issues/';
  static const String verseImagesBaseUrl = 'https://www.emtechint.com/life_issues/images/verses/';

  // Alternative: You can use different hosting services
  // For example, Firebase Storage, AWS S3, Cloudinary, etc.
  // static const String issueImagesBaseUrl = 'https://firebasestorage.googleapis.com/v0/b/your-bucket/o/issues%2F';
  // static const String verseImagesBaseUrl = 'https://firebasestorage.googleapis.com/v0/b/your-bucket/o/verses%2F';

  /// Builds full image URL from image filename
  /// If the filename already contains http/https, returns it as-is
  /// Otherwise, appends to the appropriate base URL
  static String getIssueImageUrl(String? imageName) {
    if (imageName == null || imageName.isEmpty) {
      return '';
    }

    if (imageName.startsWith('http://') || imageName.startsWith('https://')) {
      return imageName;
    }

    return '$issueImagesBaseUrl$imageName';
  }

  /// Builds full verse image URL from image filename
  static String getVerseImageUrl(String? imageName) {
    if (imageName == null || imageName.isEmpty) {
      return '';
    }

    if (imageName.startsWith('http://') || imageName.startsWith('https://')) {
      return imageName;
    }

    return '$verseImagesBaseUrl$imageName';
  }

  /// Checks if an image URL is valid
  static bool isValidImageUrl(String? url) {
    if (url == null || url.isEmpty) return false;
    return url.startsWith('http://') || url.startsWith('https://');
  }
}