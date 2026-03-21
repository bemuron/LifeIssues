// lib/presentation/pages/testimonies/testimony_submission_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../../data/datasources/issue_local_datasource.dart';
import '../../blocs/testimony/testimony_bloc.dart';
import '../../blocs/testimony/testimony_event.dart';
import '../../blocs/testimony/testimony_state.dart';
import '../../blocs/prayer/prayer_bloc.dart';
import '../../blocs/prayer/prayer_event.dart';
import '../../blocs/prayer/prayer_state.dart';
import '../../../domain/entities/prayer.dart';

class TestimonySubmissionPage extends StatelessWidget {
  final int? linkedPrayerId;

  const TestimonySubmissionPage({
    Key? key,
    this.linkedPrayerId,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(create: (_) => di.sl<TestimonyBloc>()),
        BlocProvider(
          create: (_) => di.sl<PrayerBloc>()..add(LoadMyPrayersEvent()),
        ),
      ],
      child: TestimonySubmissionView(linkedPrayerId: linkedPrayerId),
    );
  }
}

class TestimonySubmissionView extends StatefulWidget {
  final int? linkedPrayerId;

  const TestimonySubmissionView({
    Key? key,
    this.linkedPrayerId,
  }) : super(key: key);

  @override
  State<TestimonySubmissionView> createState() => _TestimonySubmissionViewState();
}

class _TestimonySubmissionViewState extends State<TestimonySubmissionView> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _bodyController = TextEditingController();
  String? _selectedCategory;
  bool _linkToPrayer = false;
  int? _selectedPrayerId;
  List<String> _categories = [];

  @override
  void initState() {
    super.initState();
    if (widget.linkedPrayerId != null) {
      _linkToPrayer = true;
      _selectedPrayerId = widget.linkedPrayerId;
    }
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    try {
      final issues = await di.sl<IssueLocalDataSource>().getAllIssues();
      if (mounted) {
        setState(() {
          _categories = issues.map((i) => i.name).toList();
        });
      }
    } catch (_) {}
  }

  @override
  void dispose() {
    _titleController.dispose();
    _bodyController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Share Your Testimony'),
      ),
      body: BlocConsumer<TestimonyBloc, TestimonyState>(
        listener: (context, state) {
          if (state is TestimonySubmitted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Your testimony has been submitted for review'),
                backgroundColor: Colors.green,
              ),
            );
            Navigator.pop(context);
          }

          if (state is TestimonyError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(state.message),
                backgroundColor: Colors.red,
              ),
            );
          }
        },
        builder: (context, state) {
          final isSubmitting = state is TestimonySubmitting;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Info card
                  Card(
                    color: Theme.of(context).colorScheme.primaryContainer,
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Row(
                        children: [
                          Icon(
                            Icons.lightbulb_outline,
                            color: Theme.of(context).colorScheme.onPrimaryContainer,
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Text(
                              'Share how God moved in your life. Be specific and encouraging to others going through similar situations.',
                              style: TextStyle(
                                color: Theme.of(context).colorScheme.onPrimaryContainer,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),

                  // Title
                  TextFormField(
                    controller: _titleController,
                    maxLength: 120,
                    decoration: const InputDecoration(
                      labelText: 'Title',
                      hintText: 'God healed my mother',
                    ),
                    validator: (value) {
                      if (value == null || value.trim().isEmpty) {
                        return 'Please enter a title';
                      }
                      if (value.trim().length < 5) {
                        return 'Title must be at least 5 characters';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),

                  // Body
                  TextFormField(
                    controller: _bodyController,
                    maxLines: 10,
                    maxLength: 2000,
                    decoration: const InputDecoration(
                      labelText: 'Your Testimony',
                      hintText: 'Share your story...',
                      alignLabelWithHint: true,
                    ),
                    validator: (value) {
                      if (value == null || value.trim().isEmpty) {
                        return 'Please enter your testimony';
                      }
                      if (value.trim().length < 20) {
                        return 'Testimony must be at least 20 characters';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),

                  // Category dropdown
                  DropdownButtonFormField<String>(
                    value: _selectedCategory,
                    decoration: const InputDecoration(
                      labelText: 'Category (Optional)',
                    ),
                    items: _categories
                        .map((cat) => DropdownMenuItem(
                      value: cat,
                      child: Text(cat),
                    ))
                        .toList(),
                    onChanged: (value) {
                      setState(() {
                        _selectedCategory = value;
                      });
                    },
                  ),
                  const SizedBox(height: 16),

                  // Link to prayer toggle
                  SwitchListTile(
                    title: const Text('Link to a Prayer Request'),
                    subtitle: const Text('If this testimony answers one of your prayers'),
                    value: _linkToPrayer,
                    onChanged: widget.linkedPrayerId != null
                        ? null // Lock if pre-selected
                        : (value) {
                      setState(() {
                        _linkToPrayer = value;
                        if (!value) {
                          _selectedPrayerId = null;
                        }
                      });
                    },
                  ),

                  // Prayer picker
                  if (_linkToPrayer) ...[
                    const SizedBox(height: 16),
                    BlocBuilder<PrayerBloc, PrayerState>(
                      builder: (context, prayerState) {
                        if (prayerState is MyPrayersLoaded) {
                          final approvedPrayers = prayerState.myPrayers
                              .where((p) => p.status == 'approved')
                              .toList();

                          if (approvedPrayers.isEmpty) {
                            return Card(
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: Text(
                                  'You don\'t have any approved prayers yet',
                                  style: Theme.of(context).textTheme.bodyMedium,
                                ),
                              ),
                            );
                          }

                          return DropdownButtonFormField<int>(
                            value: _selectedPrayerId,
                            decoration: const InputDecoration(
                              labelText: 'Select Prayer',
                            ),
                            items: approvedPrayers
                                .map((prayer) => DropdownMenuItem(
                              value: prayer.id,
                              child: Text(
                                _truncate(prayer.body, 60),
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ))
                                .toList(),
                            onChanged: widget.linkedPrayerId != null
                                ? null
                                : (value) {
                              setState(() {
                                _selectedPrayerId = value;
                              });
                            },
                          );
                        }

                        return const Center(child: CircularProgressIndicator());
                      },
                    ),
                  ],
                  const SizedBox(height: 32),

                  // Submit button
                  FilledButton(
                    onPressed: isSubmitting ? null : _submitTestimony,
                    child: isSubmitting
                        ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                        : const Text('Submit Testimony'),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  String _truncate(String text, int maxLength) {
    if (text.length <= maxLength) return text;
    return '${text.substring(0, maxLength)}...';
  }

  void _submitTestimony() {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    context.read<TestimonyBloc>().add(
      SubmitTestimonyEvent(
        title: _titleController.text,
        body: _bodyController.text,
        category: _selectedCategory,
        prayerId: _linkToPrayer ? _selectedPrayerId : null,
      ),
    );
  }
}