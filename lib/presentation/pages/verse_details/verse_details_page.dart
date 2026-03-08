import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:share_plus/share_plus.dart';
import '../../../core/constants/bible_versions.dart';
import '../../../domain/entities/verse.dart';
import '../../blocs/favorites/favorites_bloc.dart';
import '../../blocs/settings/settings_bloc.dart';

class VerseDetailsPage extends StatefulWidget {
  final Verse verse;
  final bool isFavorite;

  const VerseDetailsPage({
    super.key,
    required this.verse,
    required this.isFavorite,
  });

  @override
  State<VerseDetailsPage> createState() => _VerseDetailsPageState();
}

class _VerseDetailsPageState extends State<VerseDetailsPage> {
  late String _selectedVersion;
  bool _showComparison = false;
  late bool _isFavorite;

  @override
  void initState() {
    super.initState();
    _isFavorite = widget.isFavorite;
    final settingsState = context.read<SettingsBloc>().state;
    if (settingsState is SettingsLoaded) {
      _selectedVersion = settingsState.bibleVersion;
    } else {
      _selectedVersion = 'kjv';
    }
  }

  String _getVerseText() {
    switch (_selectedVersion) {
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

  void _shareVerse() {
    final text = '${widget.verse.reference}\n\n${_getVerseText()}\n\n- Life Issues App';
    Share.share(text);
  }

  void _copyVerse() {
    final text = '${widget.verse.reference}\n\n${_getVerseText()}';
    Clipboard.setData(ClipboardData(text: text));
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Verse copied to clipboard'),
        behavior: SnackBarBehavior.floating,
      ),
    );
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
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.verse.reference),
        actions: [
          IconButton(
            icon: Icon(
              _isFavorite ? Icons.favorite : Icons.favorite_outline,
              color: _isFavorite ? Colors.red : null,
            ),
            onPressed: _toggleFavorite,
            tooltip: _isFavorite ? 'Remove from favorites' : 'Add to favorites',
          ),
          IconButton(
            icon: const Icon(Icons.copy),
            onPressed: _copyVerse,
            tooltip: 'Copy verse',
          ),
          IconButton(
            icon: const Icon(Icons.share),
            onPressed: _shareVerse,
            tooltip: 'Share verse',
          ),
        ],
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Version Selector
            Container(
              padding: const EdgeInsets.all(16),
              color: Theme.of(context).colorScheme.surfaceContainerHighest,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: SegmentedButton<String>(
                          segments: BibleVersions.versions.entries
                              .map(
                                (e) => ButtonSegment(
                              value: e.key,
                              label: Text(e.key.toUpperCase()),
                            ),
                          )
                              .toList(),
                          selected: {_selectedVersion},
                          onSelectionChanged: (Set<String> newSelection) {
                            setState(() {
                              _selectedVersion = newSelection.first;
                            });
                          },
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          BibleVersions.getVersionName(_selectedVersion),
                          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: Theme.of(context).colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ),
                      TextButton.icon(
                        onPressed: () {
                          setState(() {
                            _showComparison = !_showComparison;
                          });
                        },
                        icon: Icon(
                          _showComparison ? Icons.compress : Icons.compare_arrows,
                          size: 18,
                        ),
                        label: Text(_showComparison ? 'Hide' : 'Compare All'),
                      ),
                    ],
                  ),
                ],
              ),
            ),

            // Main Verse Display
            if (!_showComparison) ...[
              Padding(
                padding: const EdgeInsets.all(24.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      widget.verse.reference,
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: Theme.of(context).colorScheme.primary,
                      ),
                    ),
                    const SizedBox(height: 16),
                    SelectableText(
                      _getVerseText(),
                      style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                        height: 1.6,
                        fontSize: 18,
                      ),
                    ),
                  ],
                ),
              ),
            ],

            // Comparison View
            if (_showComparison) ...[
              _buildComparisonSection('King James Version (KJV)', widget.verse.kjv),
              const Divider(height: 1),
              _buildComparisonSection('The Message (MSG)', widget.verse.msg!),
              const Divider(height: 1),
              _buildComparisonSection('Amplified Bible (AMP)', widget.verse.amp!),
            ],

            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }

  Widget _buildComparisonSection(String title, String text) {
    return Container(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
              color: Theme.of(context).colorScheme.primary,
            ),
          ),
          const SizedBox(height: 12),
          SelectableText(
            text,
            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
              height: 1.6,
            ),
          ),
        ],
      ),
    );
  }
}