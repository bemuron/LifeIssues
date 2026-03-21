import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import 'package:share_plus/share_plus.dart';
import '../../core/theme/app_theme.dart';
import '../../domain/entities/verse.dart';
import '../blocs/favorites/favorites_bloc.dart';
import '../blocs/settings/settings_bloc.dart';
import '../pages/verse_details/verse_details_page.dart';

class DailyVerseCard extends StatefulWidget {
  final Verse verse;

  const DailyVerseCard({
    super.key,
    required this.verse,
  });

  @override
  State<DailyVerseCard> createState() => _DailyVerseCardState();
}

class _DailyVerseCardState extends State<DailyVerseCard> {
  late bool _isFavorite;

  @override
  void initState() {
    super.initState();
    _isFavorite = widget.verse.isFavorite;
  }

  String _getVerseText(String version) => widget.verse.getVersion(version);

  void _toggleFavorite() {
    setState(() {
      _isFavorite = !_isFavorite;
    });
    context.read<FavoritesBloc>().add(
      ToggleFavoriteEvent(
        verseId: widget.verse.id,
        isFavorite: _isFavorite,
      ),
    );
  }

  void _shareVerse(String verseText) {
    final text =
        '${widget.verse.reference}\n\n$verseText\n\n- Yachal App';
    Share.share(text);
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<SettingsBloc, SettingsState>(
      builder: (context, settingsState) {
        final version = settingsState is SettingsLoaded
            ? settingsState.bibleVersion
            : 'kjv';
        final verseText = _getVerseText(version);

        // Today's date formatted the same way it would appear in a Bible app,
        // e.g. "Sunday, March 15, 2026"
        final today = DateFormat('EEEE, MMMM d, y').format(DateTime.now());

        return Card(
          margin: const EdgeInsets.all(16),
          elevation: 4,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          child: InkWell(
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => VerseDetailsPage(
                    verse: widget.verse,
                    isFavorite: _isFavorite,
                  ),
                ),
              );
            },
            borderRadius: BorderRadius.circular(12),
            child: Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(12),
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    Theme.of(context).colorScheme.primaryContainer,
                    Theme.of(context).colorScheme.secondaryContainer.withOpacity(0.7),
                  ],
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 12,
                          vertical: 6,
                        ),
                        decoration: BoxDecoration(
                          color: Theme.of(context).colorScheme.primary,
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Text(
                          'Verse of The Day',
                          style:
                          Theme.of(context).textTheme.labelMedium?.copyWith(
                            color: Theme.of(context)
                                .colorScheme
                                .onPrimary,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      const Spacer(),
                      IconButton(
                        onPressed: _toggleFavorite,
                        icon: Icon(
                          _isFavorite
                              ? Icons.favorite
                              : Icons.favorite_outline,
                          color: _isFavorite ? Colors.red : null,
                        ),
                        tooltip: _isFavorite
                            ? 'Remove from favorites'
                            : 'Add to favorites',
                      ),
                      IconButton(
                        onPressed: () => _shareVerse(verseText),
                        icon: const Icon(Icons.share),
                        tooltip: 'Share verse',
                      ),
                    ],
                  ),

                  // ── Date ──────────────────────────────────────────────
                  const SizedBox(height: 6),
                  Row(
                    children: [
                      Icon(
                        Icons.calendar_today,
                        size: 13,
                        color: Theme.of(context)
                            .colorScheme
                            .onPrimaryContainer
                            .withOpacity(0.65),
                      ),
                      const SizedBox(width: 5),
                      Text(
                        today,
                        style: Theme.of(context)
                            .textTheme
                            .labelSmall
                            ?.copyWith(
                          color: Theme.of(context)
                              .colorScheme
                              .onPrimaryContainer
                              .withOpacity(0.65),
                          letterSpacing: 0.2,
                        ),
                      ),
                    ],
                  ),
                  // ── End date ──────────────────────────────────────────

                  const SizedBox(height: 16),

                  // Reference
                  Text(
                    widget.verse.reference,
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                  ),
                  const SizedBox(height: 12),

                  // Verse Text
                  Text(
                    verseText,
                    style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      height: 1.6,
                      fontSize: 16,
                    ),
                    maxLines: 6,
                    overflow: TextOverflow.ellipsis,
                  ),

                  // Issue/Category Chip
                  if (widget.verse.issueName != null) ...[
                    const SizedBox(height: 12),
                    Wrap(
                      spacing: 8,
                      children: [
                        Chip(
                          avatar: Icon(
                            Icons.category,
                            size: 16,
                            color: Theme.of(context).colorScheme.onSecondaryContainer,
                          ),
                          label: Text(widget.verse.issueName!),
                          backgroundColor: Theme.of(context).colorScheme.secondaryContainer,
                          labelStyle: TextStyle(
                            fontSize: 12,
                            color: Theme.of(context).colorScheme.onSecondaryContainer,
                          ),
                        ),
                      ],
                    ),
                  ],

                  const SizedBox(height: 16),

                  // Footer
                  Row(
                    children: [
                      Icon(
                        Icons.info_outline,
                        size: 16,
                        color:
                        Theme.of(context).colorScheme.onPrimaryContainer,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'Tap to view full verse and compare versions',
                          style:
                          Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: Theme.of(context)
                                .colorScheme
                                .onPrimaryContainer,
                          ),
                        ),
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