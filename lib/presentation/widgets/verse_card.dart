import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/verse.dart';
import '../blocs/settings/settings_bloc.dart';
import '../pages/verse_details/verse_details_page.dart';

class VerseCard extends StatelessWidget {
  final Verse verse;
  final VoidCallback onFavoriteToggle;

  const VerseCard({
    super.key,
    required this.verse,
    required this.onFavoriteToggle,
  });

  String _getVerseText(String version) => verse.getVersion(version);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<SettingsBloc, SettingsState>(
      builder: (context, settingsState) {
        final version = settingsState is SettingsLoaded
            ? settingsState.bibleVersion
            : 'kjv';
        final verseText = _getVerseText(version);

        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: InkWell(
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => VerseDetailsPage(
                    verse: verse,
                    isFavorite: true,
                  ),
                ),
              );
            },
            borderRadius: BorderRadius.circular(12),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header with reference and favorite button
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          verse.reference,
                          style: Theme.of(context)
                              .textTheme
                              .titleMedium
                              ?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: Theme.of(context).colorScheme.primary,
                          ),
                        ),
                      ),
                      IconButton(
                        onPressed: onFavoriteToggle,
                        icon: const Icon(
                          Icons.favorite,
                          color: Colors.red,
                        ),
                        tooltip: 'Remove from favorites',
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),

                  // Verse text
                  Text(
                    verseText,
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      height: 1.5,
                    ),
                    maxLines: 4,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 8),

                  // Read more indicator
                  Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      Text(
                        'Tap to view full verse',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: Theme.of(context)
                              .colorScheme
                              .onSurfaceVariant,
                        ),
                      ),
                      const SizedBox(width: 4),
                      Icon(
                        Icons.arrow_forward,
                        size: 16,
                        color:
                        Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}