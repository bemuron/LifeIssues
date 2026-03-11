// lib/presentation/pages/main_navigation_page_updated.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../core/di/injection_container.dart' as di;
import '../blocs/auth/auth_bloc.dart';
import '../blocs/auth/auth_event.dart';
import '../blocs/subscription/subscription_bloc.dart';
import '../blocs/subscription/subscription_event.dart';
import 'home/home_page.dart';
import '../pages/all_issues_page.dart';
import 'prayers/prayer_feed_page.dart';
import 'testimonies/testimony_feed_page.dart';
import 'profile/profile_page.dart';

class MainNavigationPageUpdated extends StatefulWidget {
  const MainNavigationPageUpdated({Key? key}) : super(key: key);

  @override
  State<MainNavigationPageUpdated> createState() => _MainNavigationPageUpdatedState();
}

class _MainNavigationPageUpdatedState extends State<MainNavigationPageUpdated> {
  int _selectedIndex = 0;

  final List<Widget> _pages = const [
    HomePage(),
    AllIssuesPage(),
    PrayerFeedPage(),
    TestimonyFeedPage(),
    ProfilePage(),
  ];

  @override
  void initState() {
    super.initState();
    // Check auth status and subscription on app launch
    context.read<AuthBloc>().add(CheckAuthStatusEvent());
    context.read<SubscriptionBloc>().add(LoadSubscriptionStatusEvent());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _selectedIndex,
        children: _pages,
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        onDestinationSelected: (index) {
          setState(() {
            _selectedIndex = index;
          });
        },
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
            icon: Icon(Icons.favorite_outline),
            selectedIcon: Icon(Icons.favorite),
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
    );
  }
}