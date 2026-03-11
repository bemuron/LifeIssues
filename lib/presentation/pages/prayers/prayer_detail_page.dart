// lib/presentation/pages/prayers/prayer_detail_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../../domain/entities/prayer.dart';
import '../../blocs/prayer/prayer_bloc.dart';
import '../../blocs/prayer/prayer_event.dart';
import '../../blocs/prayer/prayer_state.dart';

class PrayerDetailPage extends StatelessWidget {
  final int prayerId;

  const PrayerDetailPage({
    Key? key,
    required this.prayerId,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => di.sl<PrayerBloc>()..add(LoadPrayerByIdEvent(prayerId)),
      child: const PrayerDetailView(),
    );
  }
}

class PrayerDetailView extends StatelessWidget {
  const PrayerDetailView({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Prayer Request'),
      ),
      body: BlocBuilder<PrayerBloc, PrayerState>(
        builder: (context, state) {
          if (state is PrayerDetailLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is PrayerDetailLoaded) {
            return _buildPrayerDetail(context, state.prayer);
          }

          if (state is PrayerError) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline, size: 64, color: Colors.red),
                  const SizedBox(height: 16),
                  Text(state.message),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Go Back'),
                  ),
                ],
              ),
            );
          }

          return const SizedBox();
        },
      ),
    );
  }

  Widget _buildPrayerDetail(BuildContext context, Prayer prayer) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Poster info
          Row(
            children: [
              CircleAvatar(
                child: Icon(
                  prayer.isAnonymous ? Icons.person_off : Icons.person,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      prayer.posterName ?? 'Anonymous',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    Text(
                      DateFormat('MMM d, yyyy').format(prayer.createdAt),
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                ),
              ),
              if (prayer.answered)
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.green,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.check_circle, size: 16, color: Colors.white),
                      SizedBox(width: 4),
                      Text(
                        'Answered',
                        style: TextStyle(color: Colors.white),
                      ),
                    ],
                  ),
                ),
            ],
          ),
          const SizedBox(height: 16),

          // Category
          if (prayer.category != null)
            Chip(
              label: Text(prayer.category!),
              avatar: const Icon(Icons.category, size: 16),
            ),
          const SizedBox(height: 16),

          // Prayer body
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Text(
                prayer.body,
                style: Theme.of(context).textTheme.bodyLarge,
              ),
            ),
          ),
          const SizedBox(height: 24),

          // Prayer count
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.favorite, color: Colors.red),
              const SizedBox(width: 8),
              Text(
                '${prayer.prayCount} ${prayer.prayCount == 1 ? 'person is' : 'people are'} praying',
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Praying button
          FilledButton.icon(
            onPressed: prayer.hasPrayed
                ? null
                : () {
              context
                  .read<PrayerBloc>()
                  .add(TogglePrayingEvent(prayer.id));
            },
            icon: Icon(prayer.hasPrayed ? Icons.check : Icons.favorite),
            label: Text(prayer.hasPrayed ? 'You are praying' : 'I\'m Praying'),
          ),

          // Linked testimony
          if (prayer.linkedTestimony != null) ...[
            const SizedBox(height: 24),
            const Divider(),
            const SizedBox(height: 16),
            Text(
              'Testimony',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Card(
              child: ListTile(
                leading: const Icon(Icons.auto_awesome),
                title: Text(prayer.linkedTestimony!.title),
                subtitle: Text(
                  '${prayer.linkedTestimony!.praiseCount} people praised God',
                ),
                trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                onTap: () {
                  // TODO: Navigate to testimony detail
                },
              ),
            ),
          ],
        ],
      ),
    );
  }
}