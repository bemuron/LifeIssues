import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AppTheme {
  // Color Seeds — single source of truth for the app palette
  static const Color primarySeed = Color(0xFF6750A4);
  static const Color secondarySeed = Color(0xFF625B71);
  static const Color _tertiarySeed = Color(0xFF7D5260);

  // Semantic status colors used across the app
  static const Color success = Color(0xFF4CAF50);
  static const Color warning = Color(0xFFFFA726);
  static const Color info = Color(0xFF29B6F6);

  // Issue card palette (referenced by IssueCard widget)
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

  static Color issueCardColor(int index) =>
      issueCardColors[index % issueCardColors.length];

  // Verse card gradients
  static LinearGradient verseCardGradient(bool isDark) => LinearGradient(
        begin: Alignment.topLeft,
        end: Alignment.bottomRight,
        colors: isDark
            ? const [Color(0xFF4A4458), Color(0xFF49454F)]
            : const [Color(0xFFEADDFF), Color(0xFFE8DEF8)],
      );

  // ── Shared text theme using Google Fonts (RobotoSlab) ─────────────────────
  static TextTheme _buildTextTheme(ColorScheme cs) {
    return GoogleFonts.robotoSlabTextTheme(
      TextTheme(
        displayLarge: TextStyle(
            fontSize: 57,
            fontWeight: FontWeight.w400,
            letterSpacing: -0.25,
            color: cs.onSurface),
        displayMedium: TextStyle(
            fontSize: 45, fontWeight: FontWeight.w400, color: cs.onSurface),
        displaySmall: TextStyle(
            fontSize: 36, fontWeight: FontWeight.w400, color: cs.onSurface),
        headlineLarge: TextStyle(
            fontSize: 32, fontWeight: FontWeight.w600, color: cs.onSurface),
        headlineMedium: TextStyle(
            fontSize: 28, fontWeight: FontWeight.w600, color: cs.onSurface),
        headlineSmall: TextStyle(
            fontSize: 24, fontWeight: FontWeight.w600, color: cs.onSurface),
        titleLarge: TextStyle(
            fontSize: 22, fontWeight: FontWeight.w600, color: cs.onSurface),
        titleMedium: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.15,
            color: cs.onSurface),
        titleSmall: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
            letterSpacing: 0.1,
            color: cs.onSurface),
        bodyLarge: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w400,
            letterSpacing: 0.5,
            color: cs.onSurface),
        bodyMedium: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w400,
            letterSpacing: 0.25,
            color: cs.onSurface),
        bodySmall: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w400,
            letterSpacing: 0.4,
            color: cs.onSurfaceVariant),
        labelLarge: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
            letterSpacing: 0.1,
            color: cs.onSurface),
        labelMedium: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w500,
            letterSpacing: 0.5,
            color: cs.onSurfaceVariant),
        labelSmall: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w500,
            letterSpacing: 0.5,
            color: cs.onSurfaceVariant),
      ),
    );
  }

  // ── Shared component themes ────────────────────────────────────────────────
  static ThemeData _build(ColorScheme colorScheme) {
    final isDark = colorScheme.brightness == Brightness.dark;
    return ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      brightness: colorScheme.brightness,
      textTheme: _buildTextTheme(colorScheme),
      // Slightly darker scaffold so elevated cards visually pop above it.
      // Light: warm near-white; Dark: deep muted purple-black (like M3 baseline).
      scaffoldBackgroundColor:
          isDark ? const Color(0xFF0F0E13) : const Color(0xFFF4F1F8),

      // AppBar
      appBarTheme: AppBarTheme(
        centerTitle: false,
        elevation: 0,
        backgroundColor: colorScheme.surface,
        foregroundColor: colorScheme.onSurface,
        surfaceTintColor: colorScheme.surfaceTint,
        titleTextStyle: GoogleFonts.robotoSlab(
          fontSize: 20,
          fontWeight: FontWeight.w600,
          color: colorScheme.onSurface,
        ),
      ),

      // Card — elevation 2 gives the tonal-surface lift in Material 3 that
      // makes cards visually float above the scaffold in both light and dark.
      // Explicit color ensures cards stand out from the tinted scaffold in both themes.
      cardTheme: CardThemeData(
        elevation: 2,
        color: isDark ? const Color(0xFF2A2833) : Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        clipBehavior: Clip.antiAlias,
        shadowColor: isDark ? Colors.black54 : Colors.black26,
      ),

      // Input
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: colorScheme.surfaceContainerHighest,
        hintStyle: TextStyle(color: colorScheme.onSurfaceVariant),
        labelStyle: TextStyle(color: colorScheme.onSurfaceVariant),
        border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide.none),
        enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide.none),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: colorScheme.primary, width: 2),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: colorScheme.error, width: 1),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: colorScheme.error, width: 2),
        ),
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      ),

      // Buttons
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          elevation: 1,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          foregroundColor: colorScheme.onSurface,
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          foregroundColor: colorScheme.onPrimary,
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          side: BorderSide(color: colorScheme.outline),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          foregroundColor: colorScheme.primary,
        ),
      ),
      iconButtonTheme: IconButtonThemeData(
        style: IconButton.styleFrom(
          padding: const EdgeInsets.all(12),
          foregroundColor: colorScheme.onSurfaceVariant,
        ),
      ),

      // List tiles
      listTileTheme: ListTileThemeData(
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        shape:
            RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        titleTextStyle: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w500,
            color: colorScheme.onSurface),
        subtitleTextStyle:
            TextStyle(fontSize: 14, color: colorScheme.onSurfaceVariant),
        iconColor: colorScheme.onSurfaceVariant,
      ),

      // Bottom navigation (Material 3 NavigationBar)
      navigationBarTheme: NavigationBarThemeData(
        elevation: 3,
        height: 80,
        labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
        backgroundColor: colorScheme.surface,
        indicatorColor: colorScheme.secondaryContainer,
        labelTextStyle: WidgetStateProperty.resolveWith((states) {
          final selected = states.contains(WidgetState.selected);
          return TextStyle(
            fontSize: 12,
            fontWeight: selected ? FontWeight.w600 : FontWeight.w400,
            color: selected
                ? colorScheme.onSecondaryContainer
                : colorScheme.onSurfaceVariant,
          );
        }),
      ),

      // Dialog
      dialogTheme: DialogThemeData(
        elevation: 3,
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(28)),
        titleTextStyle: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w600,
            color: colorScheme.onSurface),
        contentTextStyle:
            TextStyle(fontSize: 14, color: colorScheme.onSurfaceVariant),
      ),

      // FAB
      floatingActionButtonTheme: FloatingActionButtonThemeData(
        elevation: 3,
        shape:
            RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        foregroundColor: colorScheme.onPrimaryContainer,
        backgroundColor: colorScheme.primaryContainer,
      ),

      // Divider
      dividerTheme: DividerThemeData(
        color: colorScheme.outlineVariant,
        thickness: 1,
        space: 1,
      ),
    );
  }

  // ── Public theme getters ───────────────────────────────────────────────────
  static ThemeData get lightTheme => _build(
        ColorScheme.fromSeed(
          seedColor: primarySeed,
          secondary: secondarySeed,
          tertiary: _tertiarySeed,
          brightness: Brightness.light,
        ),
      );

  static ThemeData get darkTheme => _build(
        ColorScheme.fromSeed(
          seedColor: primarySeed,
          secondary: secondarySeed,
          tertiary: _tertiarySeed,
          brightness: Brightness.dark,
        ),
      );
}
