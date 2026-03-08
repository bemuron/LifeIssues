// lib/presentation/pages/settings/about_page.dart
import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/constants/app_strings.dart';

class AboutPage extends StatelessWidget {
  const AboutPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('About'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const SizedBox(height: 32),

            // App Icon/Logo
            Container(
              width: 100,
              height: 100,
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(20),
              ),
              child: const Icon(
                Icons.menu_book,
                size: 60,
                color: Colors.white,
              ),
            ),

            const SizedBox(height: 24),

            // App Name
            Text(
              AppStrings.appName,
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 8),

            // Version
            Text(
              'Version 1.0.0',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: AppColors.textSecondary,
              ),
            ),

            const SizedBox(height: 32),

            // Description
            Text(
              'Life Issues helps you find Biblical guidance for life\'s challenges. '
                  'Discover relevant verses for whatever situation you\'re facing.',
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                height: 1.5,
              ),
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 32),

            // Features
            _buildFeatureCard(
              context,
              icon: Icons.today,
              title: 'Daily Verses',
              description: 'Start each day with a new inspirational verse',
            ),

            const SizedBox(height: 16),

            _buildFeatureCard(
              context,
              icon: Icons.category,
              title: 'Browse by Issue',
              description: 'Find verses related to specific life situations',
            ),

            const SizedBox(height: 16),

            _buildFeatureCard(
              context,
              icon: Icons.menu_book,
              title: 'Multiple Versions',
              description: 'Read verses in KJV, MSG, and AMP translations',
            ),

            const SizedBox(height: 16),

            _buildFeatureCard(
              context,
              icon: Icons.favorite,
              title: 'Favorites',
              description: 'Save your favorite verses for quick access',
            ),

            const SizedBox(height: 32),

            // Credits
            const Divider(),
            const SizedBox(height: 16),

            Text(
              'Credits',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 16),

            Text(
              'Bible Translations:\n'
                  '• King James Version (KJV)\n'
                  '• The Message (MSG)\n'
                  '• Amplified Bible (AMP)',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                height: 1.6,
              ),
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 32),

            // Copyright
            Text(
              '© 2024 Life Issues\n'
                  'All rights reserved',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: AppColors.textTertiary,
              ),
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }

  Widget _buildFeatureCard(
      BuildContext context, {
        required IconData icon,
        required String title,
        required String description,
      }) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.primary.withOpacity(0.1),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: AppColors.primary),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    description,
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: AppColors.textSecondary,
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