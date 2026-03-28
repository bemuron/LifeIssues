import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../../data/datasources/issue_local_datasource.dart';
import '../../../domain/entities/prayer.dart';
import '../../blocs/prayer/prayer_bloc.dart';
import '../../blocs/prayer/prayer_event.dart';
import '../../blocs/prayer/prayer_state.dart';

class EditPrayerPage extends StatelessWidget {
  final Prayer prayer;

  const EditPrayerPage({Key? key, required this.prayer}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => di.sl<PrayerBloc>(),
      child: _EditPrayerView(prayer: prayer),
    );
  }
}

class _EditPrayerView extends StatefulWidget {
  final Prayer prayer;
  const _EditPrayerView({required this.prayer});

  @override
  State<_EditPrayerView> createState() => _EditPrayerViewState();
}

class _EditPrayerViewState extends State<_EditPrayerView> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _bodyController;
  String? _selectedCategory;
  bool _isAnonymous = false;
  List<String> _categories = [];

  @override
  void initState() {
    super.initState();
    _bodyController = TextEditingController(text: widget.prayer.body);
    _selectedCategory = widget.prayer.category;
    _isAnonymous = widget.prayer.isAnonymous;
    _loadCategories();
  }

  @override
  void dispose() {
    _bodyController.dispose();
    super.dispose();
  }

  Future<void> _loadCategories() async {
    try {
      final issues = await di.sl<IssueLocalDataSource>().getAllIssues();
      if (mounted) {
        setState(() => _categories = issues.map((i) => i.name).toList());
      }
    } catch (_) {}
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    context.read<PrayerBloc>().add(EditPrayerEvent(
          prayerId: widget.prayer.id,
          body: _bodyController.text.trim(),
          category: _selectedCategory,
          isAnonymous: _isAnonymous,
        ));
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return BlocListener<PrayerBloc, PrayerState>(
      listener: (context, state) {
        if (state is PrayerEdited) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Prayer updated and resubmitted for review'),
            ),
          );
          Navigator.pop(context, true);
        }
        if (state is PrayerError) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.message),
              backgroundColor: cs.error,
            ),
          );
        }
      },
      child: Scaffold(
        appBar: AppBar(title: const Text('Edit Prayer')),
        body: BlocBuilder<PrayerBloc, PrayerState>(
          builder: (context, state) {
            final isLoading = state is PrayerEditing;

            return SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Edit context banner
                    if (widget.prayer.status == 'approved' &&
                        widget.prayer.prayCount == 0)
                      Container(
                        padding: const EdgeInsets.all(12),
                        margin: const EdgeInsets.only(bottom: 20),
                        decoration: BoxDecoration(
                          color: cs.secondaryContainer,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Row(
                          children: [
                            Icon(Icons.info_outline,
                                size: 18, color: cs.onSecondaryContainer),
                            const SizedBox(width: 10),
                            Expanded(
                              child: Text(
                                'Saving will resubmit this prayer for review before it appears publicly again.',
                                style: tt.bodySmall?.copyWith(
                                    color: cs.onSecondaryContainer),
                              ),
                            ),
                          ],
                        ),
                      ),

                    // Category dropdown
                    if (_categories.isNotEmpty) ...[
                      DropdownButtonFormField<String>(
                        value: _categories.contains(_selectedCategory)
                            ? _selectedCategory
                            : null,
                        decoration: const InputDecoration(
                          labelText: 'Category (optional)',
                          prefixIcon: Icon(Icons.category_outlined),
                        ),
                        items: [
                          const DropdownMenuItem(
                              value: null, child: Text('No category')),
                          ..._categories.map((c) =>
                              DropdownMenuItem(value: c, child: Text(c))),
                        ],
                        onChanged: isLoading
                            ? null
                            : (v) =>
                                setState(() => _selectedCategory = v),
                      ),
                      const SizedBox(height: 16),
                    ],

                    // Body
                    TextFormField(
                      controller: _bodyController,
                      maxLines: 6,
                      maxLength: 1000,
                      decoration: const InputDecoration(
                        labelText: 'Prayer request',
                        alignLabelWithHint: true,
                      ),
                      validator: (v) {
                        if (v == null || v.trim().isEmpty) {
                          return 'Please enter your prayer request';
                        }
                        if (v.trim().length < 10) {
                          return 'Prayer request is too short';
                        }
                        return null;
                      },
                    ),

                    const SizedBox(height: 8),

                    // Anonymous toggle
                    SwitchListTile(
                      contentPadding: EdgeInsets.zero,
                      title: const Text('Post anonymously'),
                      subtitle: const Text(
                          'Your name will not be shown with this request'),
                      value: _isAnonymous,
                      onChanged: isLoading
                          ? null
                          : (v) => setState(() => _isAnonymous = v),
                    ),

                    const SizedBox(height: 24),

                    FilledButton(
                      onPressed: isLoading ? null : _submit,
                      child: isLoading
                          ? const SizedBox(
                              height: 20,
                              width: 20,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Text('Save Changes'),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
