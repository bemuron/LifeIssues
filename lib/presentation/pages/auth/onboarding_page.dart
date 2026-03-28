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

  final List<_OnboardingData> _pages = [
    _OnboardingData(
      title: 'Find Hope in Scripture',
      description:
          'Bible verses organised by topic so you always find the Word you need — anxiety, grief, finances, healing, joy, and more.',
      icon: Icons.menu_book,
      accentColor: const Color(0xFF6750A4),
      imagePath: 'assets/images/onboarding1.jpg',
    ),
    _OnboardingData(
      title: 'You Are Not Waiting Alone',
      description:
          'Post your prayer request and let the community stand with you. One tap. Real intercession.',
      icon: Icons.volunteer_activism,
      accentColor: const Color(0xFFB5438A),
      imagePath: 'assets/images/onboarding2.jpg',
    ),
    _OnboardingData(
      title: 'Share What God Has Done',
      description:
          'Your testimony could be someone else\'s breakthrough. Share what God has done — big miracle or quiet blessing.',
      icon: Icons.auto_awesome,
      accentColor: const Color(0xFF7965AF),
      imagePath: 'assets/images/onboarding3.jpg',
    ),
    _OnboardingData(
      title: 'Grow Together',
      description:
          'Daily verses, community prayers, and testimonies that build your faith every single day.',
      icon: Icons.groups,
      accentColor: const Color(0xFF4A90D9),
      imagePath: 'assets/images/onboarding4.jpg',
    ),
  ];

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  void _nextPage() {
    _pageController.nextPage(
      duration: const Duration(milliseconds: 350),
      curve: Curves.easeInOut,
    );
  }

  void _prevPage() {
    _pageController.previousPage(
      duration: const Duration(milliseconds: 350),
      curve: Curves.easeInOut,
    );
  }

  @override
  Widget build(BuildContext context) {
    final bottomPad = MediaQuery.of(context).padding.bottom;
    final topPad = MediaQuery.of(context).padding.top;

    return Scaffold(
      body: Stack(
        children: [
          // ── Full-screen page content ──────────────────────────────────────
          Column(
            children: [
              // Image area with no top inset — bleeds behind status bar
              Expanded(
                child: PageView.builder(
                  controller: _pageController,
                  onPageChanged: (i) => setState(() => _currentPage = i),
                  itemCount: _pages.length,
                  itemBuilder: (_, i) => _buildPage(_pages[i]),
                ),
              ),

              // ── Dots ───────────────────────────────────────────────────────
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: List.generate(_pages.length, _buildDot),
                ),
              ),

              const SizedBox(height: 28),

              // ── Buttons ────────────────────────────────────────────────────
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Row(
                  children: [
                    if (_currentPage > 0) ...[
                      Expanded(
                        child: OutlinedButton(
                          onPressed: _prevPage,
                          style: OutlinedButton.styleFrom(
                            padding: const EdgeInsets.symmetric(vertical: 14),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(14),
                            ),
                          ),
                          child: const Text('Back'),
                        ),
                      ),
                      const SizedBox(width: 12),
                    ],
                    Expanded(
                      child: FilledButton(
                        onPressed: _currentPage == _pages.length - 1
                            ? _finishOnboarding
                            : _nextPage,
                        style: FilledButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 14),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(14),
                          ),
                        ),
                        child: Text(
                          _currentPage == _pages.length - 1
                              ? 'Get Started'
                              : 'Next',
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 15,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              SizedBox(height: bottomPad + 24),
            ],
          ),

          // ── Skip button — floats over image ───────────────────────────────
          Positioned(
            top: topPad + 8,
            right: 16,
            child: TextButton(
              onPressed: _finishOnboarding,
              style: TextButton.styleFrom(
                backgroundColor: Colors.black26,
                foregroundColor: Colors.white,
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20),
                ),
                minimumSize: Size.zero,
                tapTargetSize: MaterialTapTargetSize.shrinkWrap,
              ),
              child: const Text(
                'Skip',
                style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPage(_OnboardingData data) {
    final bgColor = Theme.of(context).scaffoldBackgroundColor;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // ── Image / illustration area (full-bleed, top ~58 %) ──────────────
        Expanded(
          flex: 58,
          child: _ImageArea(data: data, bgColor: bgColor),
        ),

        // ── Title + description (bottom ~42 %) ─────────────────────────────
        Expanded(
          flex: 42,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(32, 20, 32, 0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  data.title,
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                        height: 1.2,
                      ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 14),
                Text(
                  data.description,
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                        height: 1.55,
                        color:
                            Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildDot(int index) {
    final isActive = index == _currentPage;
    return AnimatedContainer(
      duration: const Duration(milliseconds: 300),
      curve: Curves.easeOut,
      margin: const EdgeInsets.symmetric(horizontal: 4),
      width: isActive ? 28 : 8,
      height: 8,
      decoration: BoxDecoration(
        color: isActive
            ? Theme.of(context).colorScheme.primary
            : Theme.of(context).colorScheme.outlineVariant,
        borderRadius: BorderRadius.circular(4),
      ),
    );
  }

  Future<void> _finishOnboarding() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('has_seen_onboarding', true);
    if (!mounted) return;
    Navigator.of(context).pushReplacement(
      PageRouteBuilder(
        pageBuilder: (_, a, __) => const MainNavigationPageUpdated(),
        transitionsBuilder: (_, a, __, child) =>
            FadeTransition(opacity: a, child: child),
        transitionDuration: const Duration(milliseconds: 400),
      ),
    );
  }
}

// ── Image area widget ─────────────────────────────────────────────────────────

class _ImageArea extends StatelessWidget {
  final _OnboardingData data;
  final Color bgColor;

  const _ImageArea({required this.data, required this.bgColor});

  @override
  Widget build(BuildContext context) {
    return Stack(
      fit: StackFit.expand,
      children: [
        // Tinted background — always visible, acts as mat for the icon
        // fallback and bleeds through semi-transparent image edges.
        DecoratedBox(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                data.accentColor.withOpacity(0.22),
                data.accentColor.withOpacity(0.08),
              ],
            ),
          ),
        ),

        // Photo or icon fallback
        if (data.imagePath != null)
          Image.asset(
            data.imagePath!,
            fit: BoxFit.cover,
            alignment: Alignment.topCenter,
            errorBuilder: (_, __, ___) => _IconFallback(data: data),
          )
        else
          _IconFallback(data: data),

        // Subtle dark vignette on the very top so the Skip pill stays legible
        Positioned(
          top: 0,
          left: 0,
          right: 0,
          height: 120,
          child: DecoratedBox(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.black.withOpacity(0.28),
                  Colors.transparent,
                ],
              ),
            ),
          ),
        ),

        // Gradient dissolve — image fades into the page background at the bottom
        Positioned(
          left: 0,
          right: 0,
          bottom: 0,
          height: 96,
          child: DecoratedBox(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [Colors.transparent, bgColor],
              ),
            ),
          ),
        ),
      ],
    );
  }
}

// ── Icon fallback (shown when image asset is absent) ─────────────────────────

class _IconFallback extends StatelessWidget {
  final _OnboardingData data;
  const _IconFallback({required this.data});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        width: 160,
        height: 160,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: data.accentColor.withOpacity(0.14),
        ),
        child: Icon(
          data.icon,
          size: 88,
          color: data.accentColor.withOpacity(0.75),
        ),
      ),
    );
  }
}

// ── Data model ────────────────────────────────────────────────────────────────

class _OnboardingData {
  final String title;
  final String description;
  final String? imagePath;
  final IconData icon;
  final Color accentColor;

  const _OnboardingData({
    required this.title,
    required this.description,
    required this.icon,
    required this.accentColor,
    this.imagePath,
  });
}
