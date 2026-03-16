import 'package:flutter/material.dart';

class TestimonyFilterSheet extends StatefulWidget {
  final String? currentSortBy;
  final bool? currentLinkedToPrayer;
  final bool? currentHasPraise;
  final String? currentCategory;
  final List<String> categories;

  const TestimonyFilterSheet({
    Key? key,
    this.currentSortBy,
    this.currentLinkedToPrayer,
    this.currentHasPraise,
    this.currentCategory,
    this.categories = const [],
  }) : super(key: key);

  @override
  State<TestimonyFilterSheet> createState() => _TestimonyFilterSheetState();
}

class _TestimonyFilterSheetState extends State<TestimonyFilterSheet> {
  late String? selectedSort;
  late bool? selectedLinkedToPrayer;
  late bool? selectedHasPraise;
  late String? selectedCategory;

  @override
  void initState() {
    super.initState();
    selectedSort = widget.currentSortBy;
    selectedLinkedToPrayer = widget.currentLinkedToPrayer;
    selectedHasPraise = widget.currentHasPraise;
    selectedCategory = widget.currentCategory;
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(24),
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Filter Testimonies',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                TextButton(
                  onPressed: () {
                    setState(() {
                      selectedSort = null;
                      selectedLinkedToPrayer = null;
                      selectedHasPraise = null;
                      selectedCategory = null;
                    });
                  },
                  child: const Text('Clear All'),
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Sort By
            Text(
              'Sort By',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: [
                ChoiceChip(
                  label: const Text('Newest First'),
                  selected: selectedSort == 'newest',
                  onSelected: (selected) {
                    setState(() {
                      selectedSort = selected ? 'newest' : null;
                    });
                  },
                ),
                ChoiceChip(
                  label: const Text('Oldest First'),
                  selected: selectedSort == 'oldest',
                  onSelected: (selected) {
                    setState(() {
                      selectedSort = selected ? 'oldest' : null;
                    });
                  },
                ),
                ChoiceChip(
                  label: const Text('Most Praised'),
                  selected: selectedSort == 'most_praised',
                  onSelected: (selected) {
                    setState(() {
                      selectedSort = selected ? 'most_praised' : null;
                    });
                  },
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Type (Linked to Prayer or Standalone)
            Text(
              'Type',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: [
                ChoiceChip(
                  label: const Text('All'),
                  selected: selectedLinkedToPrayer == null,
                  onSelected: (selected) {
                    setState(() {
                      selectedLinkedToPrayer = null;
                    });
                  },
                ),
                ChoiceChip(
                  label: const Text('Linked to Prayer'),
                  selected: selectedLinkedToPrayer == true,
                  onSelected: (selected) {
                    setState(() {
                      selectedLinkedToPrayer = selected ? true : null;
                    });
                  },
                ),
                ChoiceChip(
                  label: const Text('Standalone'),
                  selected: selectedLinkedToPrayer == false,
                  onSelected: (selected) {
                    setState(() {
                      selectedLinkedToPrayer = selected ? false : null;
                    });
                  },
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Praise Status
            Text(
              'Praise Status',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: [
                ChoiceChip(
                  label: const Text('All'),
                  selected: selectedHasPraise == null,
                  onSelected: (selected) {
                    setState(() {
                      selectedHasPraise = null;
                    });
                  },
                ),
                ChoiceChip(
                  label: const Text('No Praise Yet'),
                  selected: selectedHasPraise == false,
                  onSelected: (selected) {
                    setState(() {
                      selectedHasPraise = selected ? false : null;
                    });
                  },
                ),
                ChoiceChip(
                  label: const Text('Has Praise'),
                  selected: selectedHasPraise == true,
                  onSelected: (selected) {
                    setState(() {
                      selectedHasPraise = selected ? true : null;
                    });
                  },
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Category
            if (widget.categories.isNotEmpty) ...[
              Text(
                'Category',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                children: [
                  ChoiceChip(
                    label: const Text('All Categories'),
                    selected: selectedCategory == null,
                    onSelected: (selected) {
                      setState(() {
                        selectedCategory = null;
                      });
                    },
                  ),
                  ...widget.categories.map(
                        (category) => ChoiceChip(
                      label: Text(category),
                      selected: selectedCategory == category,
                      onSelected: (selected) {
                        setState(() {
                          selectedCategory = selected ? category : null;
                        });
                      },
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),
            ],

            // Apply Button
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: () {
                  Navigator.pop(context, {
                    'sortBy': selectedSort,
                    'linkedToPrayer': selectedLinkedToPrayer,
                    'hasPraise': selectedHasPraise,
                    'category': selectedCategory,
                  });
                },
                child: const Text('Apply Filters'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}