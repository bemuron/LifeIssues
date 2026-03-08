import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/verse.dart';
import '../blocs/favorites/favorites_bloc.dart';
import '../blocs/settings/settings_bloc.dart';

class VerseCardHorizontal extends StatefulWidget {
  final Verse verse;

  const VerseCardHorizontal({
    super.key,
    required this.verse,
  });

  @override
  State<VerseCardHorizontal> createState() => _VerseCardHorizontalState();
}

class _VerseCardHorizontalState extends State<VerseCardHorizontal> {
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

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<SettingsBloc, SettingsState>(
      builder: (context, settingsState) {
        final version = settingsState is SettingsLoaded
            ? settingsState.bibleVersion
            : 'kjv';

        return Container(
          margin: const EdgeInsets.symmetric(horizontal: 8),
          child: Card(
            elevation: 4,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(20),
            ),
            child: Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(20),
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    Theme.of(context).colorScheme.primaryContainer,
                    Theme.of(context).colorScheme.secondaryContainer,
                  ],
                ),
              ),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Reference
                  Text(
                    widget.verse.reference,
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 24),

                  // Verse Text
                  Expanded(
                    child: SingleChildScrollView(
                      child: Text(
                        _getVerseText(version),
                        style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                          height: 1.6,
                          fontSize: 16,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),

                  // Actions
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      IconButton.filledTonal(
                        onPressed: _toggleFavorite,
                        icon: Icon(
                          _isFavorite ? Icons.favorite : Icons.favorite_outline,
                          color: _isFavorite ? Colors.red : null,
                        ),
                        tooltip: _isFavorite
                            ? 'Remove from favorites'
                            : 'Add to favorites',
                      ),
                      const SizedBox(width: 16),
                      Text(
                        'Tap card for more',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: Theme.of(context)
                              .colorScheme
                              .onPrimaryContainer
                              .withOpacity(0.7),
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