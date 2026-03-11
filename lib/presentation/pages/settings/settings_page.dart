// lib/presentation/pages/settings/settings_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../subscription/subscription_page.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({Key? key}) : super(key: key);

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  bool _notificationsEnabled = true;
  bool _prayerNotifications = true;
  bool _testimonyNotifications = true;
  String _selectedBibleVersion = 'KJV';

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _notificationsEnabled = prefs.getBool('notifications_enabled') ?? true;
      _prayerNotifications = prefs.getBool('prayer_notifications') ?? true;
      _testimonyNotifications = prefs.getBool('testimony_notifications') ?? true;
      _selectedBibleVersion = prefs.getString('bible_version') ?? 'KJV';
    });
  }

  Future<void> _saveSettings() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('notifications_enabled', _notificationsEnabled);
    await prefs.setBool('prayer_notifications', _prayerNotifications);
    await prefs.setBool('testimony_notifications', _testimonyNotifications);
    await prefs.setString('bible_version', _selectedBibleVersion);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      body: ListView(
        children: [
          // General Section
          _buildSectionHeader('General'),
          ListTile(
            leading: const Icon(Icons.book),
            title: const Text('Default Bible Version'),
            subtitle: Text(_selectedBibleVersion),
            trailing: const Icon(Icons.chevron_right),
            onTap: _showBibleVersionPicker,
          ),
          const Divider(),

          // Notifications Section
          BlocBuilder<AuthBloc, AuthState>(
            builder: (context, authState) {
              if (authState is! Authenticated) {
                return const SizedBox.shrink();
              }

              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildSectionHeader('Notifications'),
                  SwitchListTile(
                    secondary: const Icon(Icons.notifications),
                    title: const Text('Enable Notifications'),
                    subtitle: const Text('Receive push notifications'),
                    value: _notificationsEnabled,
                    onChanged: (value) {
                      setState(() {
                        _notificationsEnabled = value;
                        if (!value) {
                          _prayerNotifications = false;
                          _testimonyNotifications = false;
                        }
                      });
                      _saveSettings();
                    },
                  ),
                  if (_notificationsEnabled) ...[
                    SwitchListTile(
                      secondary: const Icon(Icons.favorite),
                      title: const Text('Prayer Notifications'),
                      subtitle: const Text('When someone prays for you'),
                      value: _prayerNotifications,
                      onChanged: (value) {
                        setState(() {
                          _prayerNotifications = value;
                        });
                        _saveSettings();
                      },
                    ),
                    SwitchListTile(
                      secondary: const Icon(Icons.auto_awesome),
                      title: const Text('Testimony Notifications'),
                      subtitle: const Text('When someone praises your testimony'),
                      value: _testimonyNotifications,
                      onChanged: (value) {
                        setState(() {
                          _testimonyNotifications = value;
                        });
                        _saveSettings();
                      },
                    ),
                  ],
                  const Divider(),
                ],
              );
            },
          ),

          // Account Section
          BlocBuilder<AuthBloc, AuthState>(
            builder: (context, authState) {
              if (authState is! Authenticated) {
                return const SizedBox.shrink();
              }

              return Column(
                children: [
                  _buildSectionHeader('Account'),
                  ListTile(
                    leading: const Icon(Icons.stars),
                    title: const Text('Subscription'),
                    subtitle: const Text('Manage your subscription'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => const SubscriptionPage(),
                        ),
                      );
                    },
                  ),
                  const Divider(),
                ],
              );
            },
          ),

          // Support Section
          _buildSectionHeader('Support'),
          ListTile(
            leading: const Icon(Icons.share),
            title: const Text('Share Life Issues'),
            subtitle: const Text('Tell others about this app'),
            onTap: _shareApp,
          ),
          ListTile(
            leading: const Icon(Icons.feedback),
            title: const Text('Send Feedback'),
            subtitle: const Text('Help us improve the app'),
            onTap: _sendFeedback,
          ),
          ListTile(
            leading: const Icon(Icons.privacy_tip),
            title: const Text('Privacy Policy'),
            trailing: const Icon(Icons.open_in_new),
            onTap: _openPrivacyPolicy,
          ),
          ListTile(
            leading: const Icon(Icons.info),
            title: const Text('About Life Issues'),
            trailing: const Icon(Icons.chevron_right),
            onTap: _showAbout,
          ),
          const Divider(),

          // App Info
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                Text(
                  'Life Issues',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 4),
                Text(
                  'Version 2.0.0',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
                const SizedBox(height: 8),
                Text(
                  'Bible verses for life\'s challenges',
                  style: Theme.of(context).textTheme.bodySmall,
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 24, 16, 8),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleSmall?.copyWith(
          color: Theme.of(context).colorScheme.primary,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  void _showBibleVersionPicker() {
    showModalBottomSheet(
      context: context,
      builder: (context) => Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text(
              'Select Bible Version',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ),
          _buildVersionOption('KJV', 'King James Version'),
          _buildVersionOption('MSG', 'The Message'),
          _buildVersionOption('AMP', 'Amplified Bible'),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildVersionOption(String code, String name) {
    final isSelected = _selectedBibleVersion == code;
    return ListTile(
      title: Text(name),
      subtitle: Text(code),
      trailing: isSelected ? const Icon(Icons.check, color: Colors.green) : null,
      onTap: () {
        setState(() {
          _selectedBibleVersion = code;
        });
        _saveSettings();
        Navigator.pop(context);
      },
    );
  }

  void _shareApp() {
    // TODO: Implement share functionality
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Share functionality coming soon')),
    );
  }

  void _sendFeedback() {
    // TODO: Implement feedback functionality
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Feedback functionality coming soon')),
    );
  }

  void _openPrivacyPolicy() {
    // TODO: Implement privacy policy link
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Opening privacy policy...')),
    );
  }

  void _showAbout() {
    showAboutDialog(
      context: context,
      applicationName: 'Life Issues',
      applicationVersion: '2.0.0',
      applicationIcon: Container(
        width: 64,
        height: 64,
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.primary,
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Icon(
          Icons.menu_book,
          size: 32,
          color: Colors.white,
        ),
      ),
      children: [
        const Text(
          'Bible verses for life\'s challenges. Find hope, share prayers, and inspire others with your testimony.',
        ),
      ],
    );
  }
}