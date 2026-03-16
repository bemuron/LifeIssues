import 'package:flutter/material.dart';

class PrayerFilterSheet extends StatefulWidget {
  final String? currentSortBy;
  final bool? currentHasPrayers;
  final String? currentCategory;
  final List<String> categories;

  const PrayerFilterSheet({
    Key? key,
    this.currentSortBy,
    this.currentHasPrayers,
    this.currentCategory,
    this.categories = const [],
  }) : super(key: key);

  @override
  State<PrayerFilterSheet> createState() => _PrayerFilterSheetState();
}

class _PrayerFilterSheetState extends State<PrayerFilterSheet> {
  late String? selectedSort;
  late bool? selectedHasPrayers;
  late String? selectedCategory;

  @override
  void initState() {
    super.initState();
    selectedSort = widget.currentSortBy;
    selectedHasPrayers = widget.currentHasPrayers;
    selectedCategory = widget.currentCategory;
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Filter Prayers',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              TextButton(
                onPressed: () {
                  setState(() {
                    selectedSort = null;
                    selectedHasPrayers = null;
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
                label: const Text('Needs Prayer'),
                selected: selectedSort == 'needs_prayer',
                onSelected: (selected) {
                  setState(() {
                    selectedSort = selected ? 'needs_prayer' : null;
                  });
                },
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Prayer Status
          Text(
            'Prayer Status',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              ChoiceChip(
                label: const Text('All Prayers'),
                selected: selectedHasPrayers == null,
                onSelected: (selected) {
                  setState(() {
                    selectedHasPrayers = null;
                  });
                },
              ),
              ChoiceChip(
                label: const Text('No Prayers Yet'),
                selected: selectedHasPrayers == false,
                onSelected: (selected) {
                  setState(() {
                    selectedHasPrayers = selected ? false : null;
                  });
                },
              ),
              ChoiceChip(
                label: const Text('Has Prayers'),
                selected: selectedHasPrayers == true,
                onSelected: (selected) {
                  setState(() {
                    selectedHasPrayers = selected ? true : null;
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
                  'hasPrayers': selectedHasPrayers,
                  'category': selectedCategory,
                });
              },
              child: const Text('Apply Filters'),
            ),
          ),
        ],
      ),
    );
  }
}