import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../core/services/connectivity_service.dart';
import '../blocs/auth/auth_bloc.dart';
import '../blocs/auth/auth_event.dart';
import '../blocs/subscription/subscription_bloc.dart';
import '../blocs/subscription/subscription_event.dart';
import 'home/home_page.dart';
import 'all_issues_page.dart';
import 'prayers/prayer_feed_page.dart';
import 'testimonies/testimony_feed_page.dart';
import 'profile/profile_page.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

class MainNavigationPageUpdated extends StatefulWidget {
  const MainNavigationPageUpdated({Key? key}) : super(key: key);

  @override
  State<MainNavigationPageUpdated> createState() =>
      _MainNavigationPageUpdatedState();
}

class _MainNavigationPageUpdatedState
    extends State<MainNavigationPageUpdated> {
  int _selectedIndex = 0;
  late final List<Widget> _pages;

  @override
  void initState() {
    super.initState();
    _pages = [
      HomePage(onNavigateToTab: _onDestinationSelected),
      const AllIssuesPage(),
      const PrayerFeedPage(),
      const TestimonyFeedPage(),
      const ProfilePage(),
    ];

    context.read<AuthBloc>().add(CheckAuthStatusEvent());
    context.read<SubscriptionBloc>().add(LoadSubscriptionStatusEvent());

    // Run connectivity check in parallel with the initial network calls.
    // If offline: clear any stacked "unknown error" snackbars from individual
    // bloc listeners and replace them with one clear message.
    _checkConnectivityAndNotify();
  }

  Future<void> _checkConnectivityAndNotify() async {
    final connected = await ConnectivityService.isConnected();
    if (!connected && mounted) {
      ScaffoldMessenger.of(context)
        ..clearSnackBars()
        ..showSnackBar(
          SnackBar(
            content: const Row(
              children: [
                Icon(Icons.wifi_off, color: Colors.white, size: 18),
                SizedBox(width: 10),
                Expanded(
                  child: Text(
                    'No internet connection. Please check your connection.',
                  ),
                ),
              ],
            ),
            backgroundColor: Colors.orange.shade800,
            duration: const Duration(seconds: 5),
            behavior: SnackBarBehavior.floating,
          ),
        );
    }
  }

  void _onDestinationSelected(int index) {
    setState(() => _selectedIndex = index);
  }

  @override
  Widget build(BuildContext context) {
    // PopScope owns the same _selectedIndex that drives the tab bar.
    // No external state sync needed — one widget, one source of truth.
    // Navigator.push routes (detail pages, etc.) are popped naturally first;
    // onPopInvokedWithResult is only reached once we're back at the root.
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, _) {
        if (didPop) return;
        if (_selectedIndex != 0) {
          // Not on Home → go to Home first.
          setState(() => _selectedIndex = 0);
        } else {
          // Already on Home → exit the app.
          SystemNavigator.pop();
        }
      },
      child: Scaffold(
        body: IndexedStack(
          index: _selectedIndex,
          children: _pages,
        ),
        bottomNavigationBar: NavigationBar(
          selectedIndex: _selectedIndex,
          onDestinationSelected: _onDestinationSelected,
          destinations: const [
            NavigationDestination(
              icon: Icon(Icons.home_outlined),
              selectedIcon: Icon(Icons.home),
              label: 'Home',
            ),
            NavigationDestination(
              icon: Icon(Icons.menu_book_outlined),
              selectedIcon: Icon(Icons.menu_book),
              label: 'Issues',
            ),
            NavigationDestination(
              icon: FaIcon(FontAwesomeIcons.personPraying),
              selectedIcon: FaIcon(FontAwesomeIcons.personPraying),
              label: 'Prayers',
            ),
            NavigationDestination(
              icon: Icon(Icons.auto_awesome_outlined),
              selectedIcon: Icon(Icons.auto_awesome),
              label: 'Testimonies',
            ),
            NavigationDestination(
              icon: Icon(Icons.person_outline),
              selectedIcon: Icon(Icons.person),
              label: 'Profile',
            ),
          ],
        ),
      ),
    );
  }
}
