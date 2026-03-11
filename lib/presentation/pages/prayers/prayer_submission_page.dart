// lib/presentation/pages/prayers/prayer_submission_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../blocs/prayer/prayer_bloc.dart';
import '../../blocs/prayer/prayer_event.dart';
import '../../blocs/prayer/prayer_state.dart';

class PrayerSubmissionPage extends StatelessWidget {
  const PrayerSubmissionPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => di.sl<PrayerBloc>(),
      child: const PrayerSubmissionView(),
    );
  }
}

class PrayerSubmissionView extends StatefulWidget {
  const PrayerSubmissionView({Key? key}) : super(key: key);

  @override
  State<PrayerSubmissionView> createState() => _PrayerSubmissionViewState();
}

class _PrayerSubmissionViewState extends State<PrayerSubmissionView> {
  final _formKey = GlobalKey<FormState>();
  final _bodyController = TextEditingController();
  String? _selectedCategory;
  bool _isAnonymous = false;

  @override
  void dispose() {
    _bodyController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Submit Prayer Request'),
      ),
      body: BlocConsumer<PrayerBloc, PrayerState>(
        listener: (context, state) {
          if (state is PrayerSubmitted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Your prayer has been submitted for review'),
                backgroundColor: Colors.green,
              ),
            );
            Navigator.pop(context);
          }

          if (state is PrayerError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(state.message),
                backgroundColor: Colors.red,
              ),
            );
          }
        },
        builder: (context, state) {
          final isSubmitting = state is PrayerSubmitting;

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
                            Icons.info_outline,
                            color: Theme.of(context).colorScheme.onPrimaryContainer,
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Text(
                              'All prayers are reviewed before publishing to ensure a safe community',
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

                  // Prayer body
                  TextFormField(
                    controller: _bodyController,
                    maxLines: 8,
                    maxLength: 500,
                    decoration: const InputDecoration(
                      labelText: 'Prayer Request',
                      hintText: 'Share what you need prayer for...',
                      alignLabelWithHint: true,
                    ),
                    validator: (value) {
                      if (value == null || value.trim().isEmpty) {
                        return 'Please enter your prayer request';
                      }
                      if (value.trim().length < 10) {
                        return 'Prayer must be at least 10 characters';
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
                    items: _getCategories()
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

                  // Anonymous toggle
                  SwitchListTile(
                    title: const Text('Post Anonymously'),
                    subtitle: const Text('Hide your name from this prayer'),
                    value: _isAnonymous,
                    onChanged: (value) {
                      setState(() {
                        _isAnonymous = value;
                      });
                    },
                  ),
                  const SizedBox(height: 32),

                  // Submit button
                  FilledButton(
                    onPressed: isSubmitting ? null : _submitPrayer,
                    child: isSubmitting
                        ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                        : const Text('Submit Prayer'),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  List<String> _getCategories() {
    // TODO: Fetch from database or constants
    return [
      'Family',
      'Health',
      'Work',
      'Relationships',
      'Financial',
      'Spiritual Growth',
      'Guidance',
      'Other',
    ];
  }

  void _submitPrayer() {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    context.read<PrayerBloc>().add(
      SubmitPrayerEvent(
        body: _bodyController.text,
        category: _selectedCategory,
        isAnonymous: _isAnonymous,
      ),
    );
  }
}