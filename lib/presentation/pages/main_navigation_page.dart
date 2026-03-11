// lib/presentation/pages/main_navigation_page_updated.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../blocs/auth/auth_bloc.dart';
import '../blocs/auth/auth_event.dart';
import '../blocs/subscription/subscription_bloc.dart';
import '../blocs/subscription/subscription_event.dart';
import 'home/home_page.dart';
import 'all_issues_page.dart';
import 'prayers/prayer_feed_page.dart';
import 'testimonies/testimony_feed_page.dart';
import 'profile/profile_page.dart';

class MainNavigationPageUpdated extends StatefulWidget {
  final int initialIndex;
  final Function(int)? onTabSelected;

  const MainNavigationPageUpdated({
    Key? key,
    this.initialIndex = 0,
    this.onTabSelected,
  }) : super(key: key);

  @override
  State<MainNavigationPageUpdated> createState() => _MainNavigationPageUpdatedState();
}

class _MainNavigationPageUpdatedState extends State<MainNavigationPageUpdated> {
  late int _selectedIndex;

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
    _selectedIndex = widget.initialIndex;

    // Check auth status and subscription on app launch
    context.read<AuthBloc>().add(CheckAuthStatusEvent());
    context.read<SubscriptionBloc>().add(LoadSubscriptionStatusEvent());
  }

  @override
  void didUpdateWidget(MainNavigationPageUpdated oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Update selected index when it changes from parent
    if (widget.initialIndex != oldWidget.initialIndex) {
      setState(() {
        _selectedIndex = widget.initialIndex;
      });
    }
  }

  void _onDestinationSelected(int index) {
    setState(() {
      _selectedIndex = index;
    });

    // Notify parent if callback provided
    widget.onTabSelected?.call(index);
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