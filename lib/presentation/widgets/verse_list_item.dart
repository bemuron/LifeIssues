import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/verse.dart';
import '../blocs/settings/settings_bloc.dart';
import '../pages/verse_details/verse_details_page.dart';

class VerseListItem extends StatelessWidget {
  final Verse verse;
  final VoidCallback? onFavoriteToggle;
  final VoidCallback? onTap;
  final bool showActions;

  const VerseListItem({
    super.key,
    required this.verse,
    this.onFavoriteToggle,
    this.onTap,
    this.showActions = true,
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
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: InkWell(
            onTap: onTap ?? () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => VerseDetailsPage(
                    verse: verse,
                    isFavorite: verse.isFavorite,
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
                  // Header Row
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
                      if (showActions && onFavoriteToggle != null)
                        IconButton(
                          onPressed: onFavoriteToggle,
                          icon: Icon(
                            verse.isFavorite
                                ? Icons.favorite
                                : Icons.favorite_outline,
                            color: verse.isFavorite ? Colors.red : null,
                          ),
                          tooltip: verse.isFavorite
                              ? 'Remove from favorites'
                              : 'Add to favorites',
                        ),
                    ],
                  ),
                  const SizedBox(height: 12),

                  // Verse Text
                  Text(
                    verseText,
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      height: 1.5,
                    ),
                    maxLines: 4,
                    overflow: TextOverflow.ellipsis,
                  ),

                  if (verse.issueName != null)
                    Chip(
                      label: Text(verse.issueName!, style: TextStyle(fontSize: 11)),
                      visualDensity: VisualDensity.compact,
                      padding: EdgeInsets.symmetric(horizontal: 8),
                    ),
                  const SizedBox(height: 12),

                  // Footer
                  Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      Text(
                        'Tap for full verse',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: Theme.of(context)
                              .colorScheme
                              .onSurfaceVariant,
                        ),
                      ),
                      const SizedBox(width: 4),
                      Icon(
                        Icons.arrow_forward_ios,
                        size: 14,
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