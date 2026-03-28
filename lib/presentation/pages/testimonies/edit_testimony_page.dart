import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../../data/datasources/issue_local_datasource.dart';
import '../../../domain/entities/testimony.dart';
import '../../blocs/testimony/testimony_bloc.dart';
import '../../blocs/testimony/testimony_event.dart';
import '../../blocs/testimony/testimony_state.dart';

class EditTestimonyPage extends StatelessWidget {
  final Testimony testimony;

  const EditTestimonyPage({Key? key, required this.testimony}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => di.sl<TestimonyBloc>(),
      child: _EditTestimonyView(testimony: testimony),
    );
  }
}

class _EditTestimonyView extends StatefulWidget {
  final Testimony testimony;
  const _EditTestimonyView({required this.testimony});

  @override
  State<_EditTestimonyView> createState() => _EditTestimonyViewState();
}

class _EditTestimonyViewState extends State<_EditTestimonyView> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _titleController;
  late final TextEditingController _bodyController;
  String? _selectedCategory;
  List<String> _categories = [];

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.testimony.title);
    _bodyController = TextEditingController(text: widget.testimony.body);
    _selectedCategory = widget.testimony.category;
    _loadCategories();
  }

  @override
  void dispose() {
    _titleController.dispose();
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
    context.read<TestimonyBloc>().add(EditTestimonyEvent(
          testimonyId: widget.testimony.id,
          title: _titleController.text.trim(),
          body: _bodyController.text.trim(),
          category: _selectedCategory,
        ));
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return BlocListener<TestimonyBloc, TestimonyState>(
      listener: (context, state) {
        if (state is TestimonyEdited) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Testimony updated and resubmitted for review'),
            ),
          );
          Navigator.pop(context, true);
        }
        if (state is TestimonyError) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.message),
              backgroundColor: cs.error,
            ),
          );
        }
      },
      child: Scaffold(
        appBar: AppBar(title: const Text('Edit Testimony')),
        body: BlocBuilder<TestimonyBloc, TestimonyState>(
          builder: (context, state) {
            final isLoading = state is TestimonyEditing;

            return SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Edit context banner
                    if (widget.testimony.status == 'approved')
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
                                'Saving will resubmit this testimony for review before it appears publicly again.',
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

                    // Title
                    TextFormField(
                      controller: _titleController,
                      maxLength: 200,
                      decoration: const InputDecoration(
                        labelText: 'Title',
                        prefixIcon: Icon(Icons.title),
                      ),
                      validator: (v) {
                        if (v == null || v.trim().isEmpty) {
                          return 'Please enter a title';
                        }
                        return null;
                      },
                    ),

                    const SizedBox(height: 16),

                    // Body
                    TextFormField(
                      controller: _bodyController,
                      maxLines: 8,
                      maxLength: 2000,
                      decoration: const InputDecoration(
                        labelText: 'Your testimony',
                        alignLabelWithHint: true,
                      ),
                      validator: (v) {
                        if (v == null || v.trim().isEmpty) {
                          return 'Please share your testimony';
                        }
                        if (v.trim().length < 20) {
                          return 'Testimony is too short';
                        }
                        return null;
                      },
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
