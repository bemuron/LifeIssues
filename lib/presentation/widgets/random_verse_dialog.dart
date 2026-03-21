// lib/presentation/widgets/random_verse_dialog.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/verse.dart';
import '../blocs/favorites/favorites_bloc.dart';
import '../blocs/settings/settings_bloc.dart';
import '../pages/verse_details/verse_details_page.dart';
import 'package:share_plus/share_plus.dart';

class RandomVerseDialog extends StatefulWidget {
  final Verse verse;

  const RandomVerseDialog({
    super.key,
    required this.verse,
  });

  @override
  State<RandomVerseDialog> createState() => _RandomVerseDialogState();
}

class _RandomVerseDialogState extends State<RandomVerseDialog> {
  late bool _isFavorite;

  @override
  void initState() {
    super.initState();
    _isFavorite = widget.verse.isFavorite;
  }

  String _getVerseText() {
    final settingsState = context.read<SettingsBloc>().state;
    final version = settingsState is SettingsLoaded ? settingsState.bibleVersion : 'kjv';
    return widget.verse.getVersion(version);
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

  void _shareVerse() {
    final text = '${widget.verse.reference}\n\n${_getVerseText()}\n\n- Life Issues App';
    Share.share(text);
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
      ),
      child: Container(
        constraints: const BoxConstraints(maxHeight: 600),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Header
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    Theme.of(context).colorScheme.primary,
                    Theme.of(context).colorScheme.secondary,
                  ],
                ),
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(20),
                  topRight: Radius.circular(20),
                ),
              ),
              child: Row(
                children: [
                  const Icon(Icons.shuffle, color: Colors.white),
                  const SizedBox(width: 12),
                  const Expanded(
                    child: Text(
                      'Random Verse',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  IconButton(
                    icon: Icon(
                      _isFavorite ? Icons.favorite : Icons.favorite_outline,
                      color: _isFavorite ? Colors.red[300] : Colors.white,
                    ),
                    onPressed: _toggleFavorite,
                    tooltip: _isFavorite ? 'Remove from favorites' : 'Add to favorites',
                  ),
                  IconButton(
                    icon: const Icon(Icons.close, color: Colors.white),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ),

            // Content
            Flexible(
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Reference
                    Text(
                      widget.verse.reference,
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        color: Theme.of(context).colorScheme.primary,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Verse Text
                    Text(
                      _getVerseText(),
                      style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                        height: 1.6,
                      ),
                    ),

                    // Issue/Category chip
                    if (widget.verse.issueName != null) ...[
                      const SizedBox(height: 16),
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
                  ],
                ),
              ),
            ),

            // Actions
            Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  OutlinedButton.icon(
                    onPressed: _shareVerse,
                    icon: const Icon(Icons.share, size: 18),
                    label: const Text('Share'),
                  ),
                  FilledButton.icon(
                    onPressed: () {
                      Navigator.pop(context);
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => VerseDetailsPage(
                            verse: widget.verse.copyWith(isFavorite: _isFavorite),
                            isFavorite: _isFavorite,
                          ),
                        ),
                      );
                    },
                    icon: const Icon(Icons.info_outline, size: 18),
                    label: const Text('Details'),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}