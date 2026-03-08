import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:share_plus/share_plus.dart';
import 'package:device_info_plus/device_info_plus.dart';
import 'dart:io';
import '../../../core/constants/app_strings.dart';
import '../../../core/constants/bible_versions.dart';
import '../../blocs/settings/settings_bloc.dart';
import '../widget_config_page.dart';

class SettingsPage extends StatelessWidget {
  const SettingsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      body: BlocBuilder<SettingsBloc, SettingsState>(
        builder: (context, state) {
          if (state is SettingsLoaded) {
            return ListView(
              children: [
                const SizedBox(height: 16),

                // General Section
                _buildSectionHeader(context, 'General'),
                _buildBibleVersionTile(context, state),
                _buildThemeTile(context, state),
                _buildShareAppTile(context),

                const SizedBox(height: 24),

                // Widget Section
                _buildSectionHeader(context, 'Home Screen Widget'),
                _buildWidgetConfigTile(context),

                const SizedBox(height: 24),

                // Notifications Section
                _buildSectionHeader(context, 'Notifications'),
                _buildDailyVerseNotificationTile(context, state),
                if (state.notificationsEnabled)
                  _buildNotificationTimeTile(context, state),

                const SizedBox(height: 24),

                // Support Section
                _buildSectionHeader(context, 'Support'),
                _buildSendFeedbackTile(context),
                _buildPrivacyPolicyTile(context),
                _buildAboutTile(context),

                const SizedBox(height: 24),

                // App Info
                _buildAppInfo(context),

                const SizedBox(height: 24),
              ],
            );
          }

          return const Center(child: CircularProgressIndicator());
        },
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
      child: Text(
        title.toUpperCase(),
        style: Theme.of(context).textTheme.labelMedium?.copyWith(
          color: Theme.of(context).colorScheme.primary,
          fontWeight: FontWeight.bold,
          letterSpacing: 1.2,
        ),
      ),
    );
  }

  Widget _buildBibleVersionTile(BuildContext context, SettingsLoaded state) {
    return ListTile(
      leading: const Icon(Icons.menu_book),
      title: const Text('Default Bible Version'),
      subtitle: Text(BibleVersions.getVersionName(state.bibleVersion)),
      trailing: const Icon(Icons.chevron_right),
      onTap: () => _showBibleVersionDialog(context, state),
    );
  }

  Widget _buildThemeTile(BuildContext context, SettingsLoaded state) {
    final String themeText = state.isDarkMode ? 'Dark' : 'Light';

    return ListTile(
      leading: Icon(state.isDarkMode ? Icons.dark_mode : Icons.light_mode),
      title: const Text('App Theme'),
      subtitle: Text(themeText),
      trailing: Switch(
        value: state.isDarkMode,
        onChanged: (value) {
          context.read<SettingsBloc>().add(ToggleThemeEvent());
        },
      ),
    );
  }

  Widget _buildShareAppTile(BuildContext context) {
    return ListTile(
      leading: const Icon(Icons.share),
      title: const Text('Share Life Issues'),
      subtitle: const Text('Share the app with friends'),
      onTap: () {
        Share.share(
          'Check out Life Issues - Bible verses for life situations\n\n'
              'https://play.google.com/store/apps/details?id=com.lifeissues.lifeissues',
        );
      },
    );
  }

  Widget _buildDailyVerseNotificationTile(
      BuildContext context,
      SettingsLoaded state,
      ) {
    return SwitchListTile(
      secondary: const Icon(Icons.notifications),
      title: const Text('Daily Verse Notification'),
      subtitle: const Text('Get a daily Bible verse notification'),
      value: state.notificationsEnabled,
      onChanged: (value) {
        context.read<SettingsBloc>().add(ToggleNotificationsEvent());
      },
    );
  }

  Widget _buildNotificationTimeTile(
      BuildContext context,
      SettingsLoaded state,
      ) {
    final time = state.notificationTime;
    final timeString = '${time.hour.toString().padLeft(2, '0')}:${time.minute.toString().padLeft(2, '0')}';

    return ListTile(
      leading: const SizedBox(width: 40), // Indent to align with switch tiles
      title: const Text('Notification Time'),
      subtitle: Text(timeString),
      trailing: const Icon(Icons.access_time),
      onTap: () => _showTimePicker(context, state),
    );
  }

  Widget _buildSendFeedbackTile(BuildContext context) {
    return ListTile(
      leading: const Icon(Icons.feedback),
      title: const Text('Send Feedback'),
      subtitle: const Text('Help us improve the app'),
      onTap: () => _sendFeedback(context),
    );
  }

  Widget _buildPrivacyPolicyTile(BuildContext context) {
    return ListTile(
      leading: const Icon(Icons.privacy_tip),
      title: const Text('Privacy Policy'),
      subtitle: const Text('View our privacy policy'),
      trailing: const Icon(Icons.open_in_new),
      onTap: () => _openPrivacyPolicy(),
    );
  }

  Widget _buildAboutTile(BuildContext context) {
    return ListTile(
      leading: const Icon(Icons.info),
      title: const Text('About Life Issues'),
      subtitle: const Text('App information and credits'),
      trailing: const Icon(Icons.chevron_right),
      onTap: () {
        showAboutDialog(
          context: context,
          applicationName: AppStrings.appName,
          applicationVersion: '1.0.0',
          applicationIcon: const Icon(Icons.menu_book, size: 48),
          children: [
            const Text(
              'Life Issues provides Bible verses for various life situations, '
                  'helping you find guidance and comfort in God\'s Word.',
            ),
          ],
        );
      },
    );
  }

  Widget _buildAppInfo(BuildContext context) {
    return Center(
      child: Column(
        children: [
          Text(
            AppStrings.appName,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: Theme.of(context).colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Version 1.0.0',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: Theme.of(context).colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildWidgetConfigTile(BuildContext context) {
    return ListTile(
      leading: const Icon(Icons.widgets),
      title: const Text('Configure Widget'),
      subtitle: const Text('Add daily verse to home screen'),
      trailing: const Icon(Icons.chevron_right),
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => WidgetConfigPage(),
          ),
        );
        //Navigator.pushNamed(context, '/widget-config');
      },
    );
  }

  void _showBibleVersionDialog(BuildContext context, SettingsLoaded state) {
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Select Bible Version'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: BibleVersions.versions.entries.map((entry) {
            return RadioListTile<String>(
              title: Text(entry.value),
              subtitle: Text(entry.key.toUpperCase()),
              value: entry.key,
              groupValue: state.bibleVersion,
              onChanged: (String? value) {
                if (value != null) {
                  context.read<SettingsBloc>().add(
                    UpdateBibleVersionEvent(value),
                  );
                  Navigator.pop(dialogContext);
                }
              },
            );
          }).toList(),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
        ],
      ),
    );
  }

  Future<void> _showTimePicker(
      BuildContext context,
      SettingsLoaded state,
      ) async {
    final TimeOfDay? picked = await showTimePicker(
      context: context,
      initialTime: state.notificationTime,
    );

    if (picked != null && context.mounted) {
      context.read<SettingsBloc>().add(
        UpdateNotificationTimeEvent(picked),
      );
    }
  }

  Future<void> _sendFeedback(BuildContext context) async {
    // Get device information
    final deviceInfo = DeviceInfoPlugin();
    String deviceDetails = '';

    try {
      if (Platform.isAndroid) {
        final androidInfo = await deviceInfo.androidInfo;
        deviceDetails = '''
Device: ${androidInfo.model}
Android Version: ${androidInfo.version.release}
SDK: ${androidInfo.version.sdkInt}
''';
      } else if (Platform.isIOS) {
        final iosInfo = await deviceInfo.iosInfo;
        deviceDetails = '''
Device: ${iosInfo.model}
iOS Version: ${iosInfo.systemVersion}
''';
      }
    } catch (e) {
      deviceDetails = 'Unable to get device information';
    }

    final emailUri = Uri(
      scheme: 'mailto',
      path: 'support@lifeissues.app',
      query: Uri.encodeQueryComponent(
        'subject=Life Issues Feedback&'
            'body=\n\n\n'
            '--- Device Information ---\n'
            '$deviceDetails'
            'App Version: 1.0.0',
      ).replaceAll('+', '%20'),
    );

    if (await canLaunchUrl(emailUri)) {
      await launchUrl(emailUri);
    } else {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Could not open email app'),
          ),
        );
      }
    }
  }

  Future<void> _openPrivacyPolicy() async {
    final Uri url = Uri.parse('https://lifeissues.app/privacy-policy');
    if (await canLaunchUrl(url)) {
      await launchUrl(url, mode: LaunchMode.externalApplication);
    }
  }
}