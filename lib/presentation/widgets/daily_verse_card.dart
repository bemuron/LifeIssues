import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:share_plus/share_plus.dart';
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

  String _getVerseText(String version) {
    switch (version) {
      case 'kjv':
        return widget.verse.kjv;
      case 'msg':
        return widget.verse.msg!;
      case 'amp':
        return widget.verse.amp!;
      default:
        return widget.verse.kjv;
    }
  }

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
        '${widget.verse.reference}\n\n$verseText\n\n- Life Issues App';
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

        return Card(
          margin: const EdgeInsets.all(16),
          elevation: 2,
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
                    Theme.of(context)
                        .colorScheme
                        .secondaryContainer
                        .withOpacity(0.7),
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
                          'Daily Verse',
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