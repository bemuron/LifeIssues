import 'package:flutter/material.dart';

class AppColors {
  // Primary Colors
  static const Color primary = Color(0xFF6750A4);
  static const Color primaryLight = Color(0xFF6750A4);
  static const Color primaryDark = Color(0xFFD0BCFF);

  static const Color secondary = Color(0xFF625B71);
  static const Color secondaryLight = Color(0xFF625B71);
  static const Color secondaryDark = Color(0xFFCCC2DC);

  static const Color tertiary = Color(0xFF7D5260);
  static const Color tertiaryLight = Color(0xFF7D5260);
  static const Color tertiaryDark = Color(0xFFEFB8C8);

  // Background Colors
  static const Color background = Color(0xFFFFFBFE);
  static const Color backgroundLight = Color(0xFFFFFBFE);
  static const Color backgroundDark = Color(0xFF1C1B1F);

  static const Color surface = Color(0xFFFFFBFE);
  static const Color surfaceLight = Color(0xFFFFFBFE);
  static const Color surfaceDark = Color(0xFF1C1B1F);

  // Dark theme colors
  static const Color darkBackground = Color(0xFF1C1B1F);
  static const Color darkSurface = Color(0xFF2B2930);
  static const Color darkTextPrimary = Color(0xFFE6E1E5);
  static const Color darkTextSecondary = Color(0xFFCAC4D0);

  // Error Colors
  static const Color error = Color(0xFFB3261E);
  static const Color errorLight = Color(0xFFB3261E);
  static const Color errorDark = Color(0xFFF2B8B5);

  // Success Colors
  static const Color success = Color(0xFF4CAF50);
  static const Color successDark = Color(0xFF81C784);

  // Warning Colors
  static const Color warning = Color(0xFFFFA726);
  static const Color warningDark = Color(0xFFFFB74D);

  // Info Colors
  static const Color info = Color(0xFF29B6F6);
  static const Color infoDark = Color(0xFF4FC3F7);

  // Text Colors
  static const Color textPrimary = Color(0xFF1C1B1F);
  static const Color textPrimaryLight = Color(0xFF1C1B1F);
  static const Color textPrimaryDark = Color(0xFFE6E1E5);

  static const Color textSecondary = Color(0xFF49454F);
  static const Color textSecondaryLight = Color(0xFF49454F);
  static const Color textSecondaryDark = Color(0xFFCAC4D0);

  static const Color textTertiary = Color(0xFF79747E);

  // Gradient Colors
  static const List<Color> primaryGradientLight = [
    Color(0xFF6750A4),
    Color(0xFF625B71),
  ];

  static const List<Color> primaryGradientDark = [
    Color(0xFFD0BCFF),
    Color(0xFFCCC2DC),
  ];

  static const List<Color> secondaryGradientLight = [
    Color(0xFF625B71),
    Color(0xFF7D5260),
  ];

  static const List<Color> secondaryGradientDark = [
    Color(0xFFCCC2DC),
    Color(0xFFEFB8C8),
  ];

  // Verse Card Gradient
  static const List<Color> verseCardGradientLight = [
    Color(0xFFEADDFF),
    Color(0xFFE8DEF8),
  ];

  static const List<Color> verseCardGradientDark = [
    Color(0xFF4A4458),
    Color(0xFF49454F),
  ];

  // Issue Card Colors
  static const List<Color> issueCardColors = [
    Color(0xFFFFB4AB),
    Color(0xFFFFDAD6),
    Color(0xFFD0BCFF),
    Color(0xFFEADDFF),
    Color(0xFFB3E5FC),
    Color(0xFFCFE8FF),
    Color(0xFFC8E6C9),
    Color(0xFFE8F5E9),
    Color(0xFFFFF9C4),
    Color(0xFFFFF59D),
  ];

  // Icon Colors
  static const Color favoriteIconColor = Color(0xFFE53935);
  static const Color shareIconColor = Color(0xFF1976D2);
  static const Color copyIconColor = Color(0xFF388E3C);

  // Status Colors
  static const Color activeColor = Color(0xFF4CAF50);
  static const Color inactiveColor = Color(0xFF9E9E9E);
  static const Color pendingColor = Color(0xFFFFA726);

  // Overlay Colors
  static const Color overlayLight = Color(0x0A000000);
  static const Color overlayDark = Color(0x14FFFFFF);

  // Border Colors
  static const Color borderLight = Color(0xFFCAC4D0);
  static const Color borderDark = Color(0xFF938F99);

  // Helper Methods
  static Color getRandomIssueColor(int index) {
    return issueCardColors[index % issueCardColors.length];
  }

  static LinearGradient getPrimaryGradient(bool isDarkMode) {
    return LinearGradient(
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
      colors: isDarkMode ? primaryGradientDark : primaryGradientLight,
    );
  }

  static LinearGradient getSecondaryGradient(bool isDarkMode) {
    return LinearGradient(
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
      colors: isDarkMode ? secondaryGradientDark : secondaryGradientLight,
    );
  }

  static LinearGradient getVerseCardGradient(bool isDarkMode) {
    return LinearGradient(
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
      colors: isDarkMode ? verseCardGradientDark : verseCardGradientLight,
    );
  }
}