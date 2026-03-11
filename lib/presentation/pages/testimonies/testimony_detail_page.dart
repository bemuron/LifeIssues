// lib/presentation/pages/testimonies/testimony_detail_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../../domain/entities/testimony.dart';
import '../../blocs/testimony/testimony_bloc.dart';
import '../../blocs/testimony/testimony_event.dart';
import '../../blocs/testimony/testimony_state.dart';

class TestimonyDetailPage extends StatelessWidget {
  final int testimonyId;

  const TestimonyDetailPage({
    Key? key,
    required this.testimonyId,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => di.sl<TestimonyBloc>()
        ..add(LoadTestimonyByIdEvent(testimonyId)),
      child: const TestimonyDetailView(),
    );
  }
}

class TestimonyDetailView extends StatelessWidget {
  const TestimonyDetailView({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Testimony'),
      ),
      body: BlocBuilder<TestimonyBloc, TestimonyState>(
        builder: (context, state) {
          if (state is TestimonyDetailLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is TestimonyDetailLoaded) {
            return _buildTestimonyDetail(context, state.testimony);
          }

          if (state is TestimonyError) {
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

  Widget _buildTestimonyDetail(BuildContext context, Testimony testimony) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Poster info
          Row(
            children: [
              CircleAvatar(
                child: const Icon(Icons.person),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      testimony.posterName,
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    Text(
                      DateFormat('MMM d, yyyy').format(testimony.createdAt),
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 6,
                ),
                decoration: BoxDecoration(
                  color: Colors.green.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.green),
                ),
                child: const Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.verified, size: 16, color: Colors.green),
                    SizedBox(width: 4),
                    Text(
                      'Verified',
                      style: TextStyle(
                        color: Colors.green,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Category
          if (testimony.category != null)
            Chip(
              label: Text(testimony.category!),
              avatar: const Icon(Icons.category, size: 16),
            ),
          const SizedBox(height: 16),

          // Title
          Text(
            testimony.title,
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),

          // Body
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Text(
                testimony.body,
                style: Theme.of(context).textTheme.bodyLarge,
              ),
            ),
          ),
          const SizedBox(height: 24),

          // Praise count
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.auto_awesome, color: Colors.amber),
              const SizedBox(width: 8),
              Text(
                '${testimony.praiseCount} ${testimony.praiseCount == 1 ? 'person praised' : 'people praised'} God',
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Praise button
          FilledButton.icon(
            onPressed: testimony.hasPraised
                ? null
                : () {
              context
                  .read<TestimonyBloc>()
                  .add(TogglePraiseEvent(testimony.id));
            },
            icon: Icon(testimony.hasPraised ? Icons.check : Icons.auto_awesome),
            label: Text(testimony.hasPraised ? 'You praised God' : 'Praise God'),
          ),

          // Linked prayer
          if (testimony.linkedPrayer != null) ...[
            const SizedBox(height: 24),
            const Divider(),
            const SizedBox(height: 16),
            Text(
              'This testimony answers a prayer',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.favorite, size: 16, color: Colors.red),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            testimony.linkedPrayer!.isAnonymous
                                ? 'Anonymous prayer'
                                : 'Prayer request',
                            style: Theme.of(context).textTheme.labelLarge,
                          ),
                        ),
                        Text(
                          '${testimony.linkedPrayer!.prayCount} prayers',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      testimony.linkedPrayer!.bodyExcerpt,
                      style: Theme.of(context).textTheme.bodyMedium,
                    ),
                    const SizedBox(height: 8),
                    Align(
                      alignment: Alignment.centerRight,
                      child: TextButton(
                        onPressed: () {
                          // TODO: Navigate to prayer detail
                        },
                        child: const Text('View Prayer'),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}