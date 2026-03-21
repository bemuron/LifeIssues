// lib/presentation/pages/auth/onboarding_page.dart

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../main_navigation_page.dart';

class OnboardingPage extends StatefulWidget {
  const OnboardingPage({Key? key}) : super(key: key);

  @override
  State<OnboardingPage> createState() => _OnboardingPageState();
}

class _OnboardingPageState extends State<OnboardingPage> {
  final PageController _pageController = PageController();
  int _currentPage = 0;

  // To use an image instead of an icon on any slide, set [imagePath] to the
  // asset path (e.g. 'assets/images/onboarding1.png') and leave [icon] null.
  // To use an icon, set [icon] and leave [imagePath] null.
  final List<OnboardingData> _pages = [
    OnboardingData(
      title: 'Find Hope in Scripture',
      description:
          'Discover Bible verses perfectly matched to your life situations and challenges',
      icon: Icons.menu_book,
      color: Colors.blue,
      // imagePath: 'assets/images/onboarding1.png',  // ← uncomment to use image
    ),
    OnboardingData(
      title: 'Share Prayer Requests',
      description:
          'Join a caring community where believers pray for one another',
      icon: Icons.favorite,
      color: Colors.red,
      // imagePath: 'assets/images/onboarding2.png',
    ),
    OnboardingData(
      title: 'Share Your Testimony',
      description:
          'Inspire others by sharing how God has moved in your life',
      icon: Icons.auto_awesome,
      color: Colors.amber,
      // imagePath: 'assets/images/onboarding3.png',
    ),
    OnboardingData(
      title: 'Build Your Faith',
      description:
          'Daily verses, powerful testimonies, and a supportive community await',
      icon: Icons.church,
      color: Colors.purple,
      // imagePath: 'assets/images/onboarding4.png',
    ),
  ];

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            // Skip button
            Align(
              alignment: Alignment.topRight,
              child: TextButton(
                onPressed: _finishOnboarding,
                child: const Text('Skip'),
              ),
            ),

            // Pages
            Expanded(
              child: PageView.builder(
                controller: _pageController,
                onPageChanged: (index) {
                  setState(() {
                    _currentPage = index;
                  });
                },
                itemCount: _pages.length,
                itemBuilder: (context, index) {
                  return _buildPage(_pages[index]);
                },
              ),
            ),

            // Page indicator dots
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(
                _pages.length,
                (index) => _buildDot(index),
              ),
            ),
            const SizedBox(height: 32),

            // Navigation buttons
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: Row(
                children: [
                  if (_currentPage > 0)
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () {
                          _pageController.previousPage(
                            duration: const Duration(milliseconds: 300),
                            curve: Curves.easeInOut,
                          );
                        },
                        child: const Text('Back'),
                      ),
                    ),
                  if (_currentPage > 0) const SizedBox(width: 16),
                  Expanded(
                    child: FilledButton(
                      onPressed: () {
                        if (_currentPage == _pages.length - 1) {
                          _finishOnboarding();
                        } else {
                          _pageController.nextPage(
                            duration: const Duration(milliseconds: 300),
                            curve: Curves.easeInOut,
                          );
                        }
                      },
                      child: Text(
                        _currentPage == _pages.length - 1
                            ? 'Get Started'
                            : 'Next',
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }

  Widget _buildPage(OnboardingData data) {
    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _buildVisual(data),
          const SizedBox(height: 48),

          // Title
          Text(
            data.title,
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),

          // Description
          Text(
            data.description,
            style: Theme.of(context).textTheme.bodyLarge,
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  /// Shows an image asset when [data.imagePath] is set, otherwise falls back
  /// to the icon in a coloured circle.
  Widget _buildVisual(OnboardingData data) {
    if (data.imagePath != null) {
      return SizedBox(
        height: 220,
        child: Image.asset(
          data.imagePath!,
          fit: BoxFit.contain,
          errorBuilder: (context, _, __) => _buildIconFallback(data),
        ),
      );
    }
    return _buildIconFallback(data);
  }

  Widget _buildIconFallback(OnboardingData data) {
    return Container(
      width: 150,
      height: 150,
      decoration: BoxDecoration(
        color: data.color.withOpacity(0.1),
        shape: BoxShape.circle,
      ),
      child: Icon(
        data.icon ?? Icons.star,
        size: 80,
        color: data.color,
      ),
    );
  }

  Widget _buildDot(int index) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 300),
      margin: const EdgeInsets.symmetric(horizontal: 4),
      width: _currentPage == index ? 24 : 8,
      height: 8,
      decoration: BoxDecoration(
        color: _currentPage == index
            ? Theme.of(context).colorScheme.primary
            : Theme.of(context).colorScheme.surfaceVariant,
        borderRadius: BorderRadius.circular(4),
      ),
    );
  }

  Future<void> _finishOnboarding() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('has_seen_onboarding', true);

    if (!mounted) return;

    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const MainNavigationPageUpdated()),
    );
  }
}

/// Data for a single onboarding slide.
///
/// Provide either [imagePath] (an asset path such as
/// `'assets/images/onboarding1.png'`) or [icon] — not both.
/// If [imagePath] is set the image is displayed; otherwise the icon is used.
class OnboardingData {
  final String title;
  final String description;

  /// Asset path to an image, e.g. `'assets/images/onboarding1.png'`.
  /// Takes precedence over [icon] when set.
  final String? imagePath;

  /// Fallback icon shown when [imagePath] is null or fails to load.
  final IconData? icon;

  final Color color;

  OnboardingData({
    required this.title,
    required this.description,
    this.imagePath,
    this.icon,
    required this.color,
  });
}
