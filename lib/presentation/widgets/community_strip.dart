// lib/presentation/widgets/community_strip.dart

import 'package:flutter/material.dart';
import '../../core/di/injection_container.dart' as di;
import '../../data/datasources/home_remote_datasource.dart';
import '../../data/models/home_summary_model.dart';
import '../pages/prayers/prayer_feed_page.dart';
import '../pages/testimonies/testimony_feed_page.dart';

class CommunityStrip extends StatefulWidget {
  const CommunityStrip({Key? key}) : super(key: key);

  @override
  State<CommunityStrip> createState() => _CommunityStripState();
}

class _CommunityStripState extends State<CommunityStrip> {
  HomeSummaryModel? _summary;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadSummary();
  }

  Future<void> _loadSummary() async {
    try {
      final dataSource = di.sl<HomeRemoteDataSource>();
      final summary = await dataSource.getHomeSummary();
      if (mounted) {
        setState(() {
          _summary = summary;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(child: _buildPrayerCard(context)),
        const SizedBox(width: 12),
        Expanded(child: _buildTestimonyCard(context)),
      ],
    );
  }

  Widget _buildPrayerCard(BuildContext context) {
    final count = _summary?.unansweredPrayerCount ?? 0;

    return Card(
      elevation: 2,
      child: InkWell(
        onTap: () => Navigator.push(
          context,
          MaterialPageRoute(builder: (_) => const PrayerFeedPage()),
        ),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                children: [
                  Icon(Icons.favorite, color: Colors.red, size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Prayer Requests',
                      style: Theme.of(context).textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              if (_isLoading)
                const SizedBox(
                  height: 20,
                  width: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              else ...[
                Text(
                  '$count ${count == 1 ? 'prayer needs' : 'prayers need'}',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                Text(
                  'intercession',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTestimonyCard(BuildContext context) {
    final title = _summary?.latestTestimony?.title ?? 'Share your story';

    return Card(
      elevation: 2,
      child: InkWell(
        onTap: () => Navigator.push(
          context,
          MaterialPageRoute(builder: (_) => const TestimonyFeedPage()),
        ),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                children: [
                  Icon(Icons.auto_awesome, color: Colors.amber, size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Testimonies',
                      style: Theme.of(context).textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              if (_isLoading)
                const SizedBox(
                  height: 20,
                  width: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              else
                Text(
                  title,
                  style: Theme.of(context).textTheme.bodyMedium,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
            ],
          ),
        ),
      ),
    );
  }
}