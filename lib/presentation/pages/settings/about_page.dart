// lib/presentation/pages/settings/about_page.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/constants/app_strings.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_state.dart';
import '../auth/login_page.dart';
import '../prayers/prayer_submission_page.dart';
import '../subscription/subscription_page.dart';
import '../testimonies/testimony_submission_page.dart';

class AboutPage extends StatelessWidget {
  const AboutPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('About'),
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Hero image placeholder
            _HeroImagePlaceholder(),

            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  const SizedBox(height: 28),

                  // App icon + name
                  ClipRRect(
                    borderRadius: BorderRadius.circular(20),
                    child: Image.asset(
                      'assets/icons/app_icon.png',
                      width: 88,
                      height: 88,
                      errorBuilder: (_, __, ___) => Container(
                        width: 88,
                        height: 88,
                        decoration: BoxDecoration(
                          color: AppColors.primary,
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: const Icon(Icons.menu_book, size: 52, color: Colors.white),
                      ),
                    ),
                  ),

                  const SizedBox(height: 16),

                  Text(
                    AppStrings.appName,
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),

                  const SizedBox(height: 4),

                  Text(
                    'Version ${AppStrings.appVersion}',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                  ),

                  const SizedBox(height: 16),

                  Text(
                    'Find scripture for life\'s challenges. Pray together. '
                    'Share what God has done.',
                    style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                          height: 1.55,
                        ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 32),

                  // Features
                  _buildFeatureCard(
                    context,
                    emoji: '📖',
                    title: 'Scripture by Life Issue',
                    description:
                        'Bible verses organised by topic — anxiety, grief, finances, healing, '
                        'joy, purpose, and more. No digging. Just the right verse at the right moment.',
                  ),
                  const SizedBox(height: 12),
                  _buildFeatureCard(
                    context,
                    emoji: '🙏',
                    title: 'Prayer Community',
                    description:
                        'Post your prayer request and let the community stand with you. '
                        'You are not waiting alone.',
                  ),
                  const SizedBox(height: 12),
                  _buildFeatureCard(
                    context,
                    emoji: '✨',
                    title: 'Testimonies',
                    description:
                        'Share what God has done — big miracle or quiet blessing. '
                        'Your story is someone else\'s strength.',
                  ),
                  const SizedBox(height: 12),
                  _buildFeatureCard(
                    context,
                    emoji: '❤️',
                    title: 'Pray for Others',
                    description:
                        'Browse community prayer requests and intercede for fellow believers. '
                        'One tap. Real impact.',
                  ),
                  const SizedBox(height: 12),
                  _buildFeatureCard(
                    context,
                    emoji: '🎉',
                    title: 'Celebrate',
                    description:
                        'Share a blessing, an answered prayer, or simply a good day rooted in gratitude. '
                        'Joy shared is joy multiplied.',
                  ),

                  const SizedBox(height: 32),

                  // CTA section
                  _CtaSection(),

                  const SizedBox(height: 32),
                  const Divider(),
                  const SizedBox(height: 20),

                  // Credits
                  Text(
                    'Bible Translations',
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    '• King James Version (KJV)\n'
                    '• The Message (MSG)\n'
                    '• Amplified Bible (AMP)',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          height: 1.7,
                        ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 32),

                  // Footer
                  Text(
                    '© 2019 – 2026 ${AppStrings.appName}\nAll rights reserved.',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: Theme.of(context).colorScheme.outline,
                          height: 1.6,
                        ),
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 40),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFeatureCard(
    BuildContext context, {
    required String emoji,
    required String title,
    required String description,
  }) {
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(
          color: Theme.of(context).colorScheme.outlineVariant,
          width: 1,
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(emoji, style: const TextStyle(fontSize: 28)),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    description,
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                          height: 1.5,
                        ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Placeholder for a hero/banner image to be added later (assets/images/).
class _HeroImagePlaceholder extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      height: 200,
      color: Theme.of(context).colorScheme.surfaceVariant,
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Image.asset(
              'assets/images/about_hero.jpg',
              height: 200,
              width: double.infinity,
              fit: BoxFit.cover,
            ),
            /*Icon(
              Icons.image_outlined,
              size: 48,
              color: Theme.of(context).colorScheme.onSurfaceVariant.withOpacity(0.45),
            ),
            const SizedBox(height: 8),
            Text(
              'assets/images/ — hero image coming soon',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Theme.of(context)
                        .colorScheme
                        .onSurfaceVariant
                        .withOpacity(0.45),
                  ),
            ),*/
          ],
        ),
      ),
    );
  }
}

/// CTA buttons: Post a Prayer / Share a Testimony.
/// Checks auth then subscription before navigating.
class _CtaSection extends StatelessWidget {
  const _CtaSection();

  void _handlePostPrayer(BuildContext context) {
    final authState = context.read<AuthBloc>().state;
    if (authState is! Authenticated) {
      _showSignInDialog(context);
      return;
    }
    final subState = context.read<SubscriptionBloc>().state;
    final canPost = subState is SubscriptionLoaded && subState.canPost;
    if (!canPost) {
      _showSubscribeDialog(context);
      return;
    }
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const PrayerSubmissionPage()),
    );
  }

  void _handleShareTestimony(BuildContext context) {
    final authState = context.read<AuthBloc>().state;
    if (authState is! Authenticated) {
      _showSignInDialog(context);
      return;
    }
    final subState = context.read<SubscriptionBloc>().state;
    final canPost = subState is SubscriptionLoaded && subState.canPost;
    if (!canPost) {
      _showSubscribeDialog(context);
      return;
    }
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const TestimonySubmissionPage()),
    );
  }

  void _showSignInDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Sign in required'),
        content: const Text(
          'You need to be signed in to post a prayer or share a testimony.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(ctx);
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const LoginPage()),
              );
            },
            child: const Text('Sign In'),
          ),
        ],
      ),
    );
  }

  void _showSubscribeDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        icon: const Icon(Icons.stars_rounded, size: 40),
        title: const Text('Premium required'),
        content: const Text(
          'Posting prayers and testimonies is a Premium feature. '
          'Upgrade to connect with the community.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Not now'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(ctx);
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const SubscriptionPage()),
              );
            },
            child: const Text('View Plans'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          'Join the Community',
          style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 8),
        Text(
          'We\'re just getting started. More features, more topics, and more ways '
          'to grow together are coming in future updates.',
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Theme.of(context).colorScheme.onSurfaceVariant,
                height: 1.5,
              ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 20),
        FilledButton.icon(
          onPressed: () => _handlePostPrayer(context),
          icon: const FaIcon(FontAwesomeIcons.personPraying),
          label: const Text('Post a Prayer Request'),
          style: FilledButton.styleFrom(
            padding: const EdgeInsets.symmetric(vertical: 14),
          ),
        ),
        const SizedBox(height: 12),
        OutlinedButton.icon(
          onPressed: () => _handleShareTestimony(context),
          icon: const Icon(Icons.auto_awesome),
          label: const Text('Share a Testimony'),
          style: OutlinedButton.styleFrom(
            padding: const EdgeInsets.symmetric(vertical: 14),
          ),
        ),
      ],
    );
  }
}
