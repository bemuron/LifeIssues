import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:share_plus/share_plus.dart';
import '../../../core/constants/bible_versions.dart';
import '../../../domain/entities/verse.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/favorites/favorites_bloc.dart';
import '../../blocs/settings/settings_bloc.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_state.dart';
import '../../widgets/ad_banner_widget.dart';

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

  String _getVerseText() => widget.verse.getVersion(_selectedVersion);

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
      body: Column(  // ← WRAP in Column
        children: [
          Expanded(  // ← WRAP ScrollView in Expanded
            child: SingleChildScrollView(
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
                                segments: widget.verse.translations.keys
                                    .map(
                                      (k) => ButtonSegment(
                                    value: k,
                                    label: Text(k),
                                  ),
                                )
                                    .toList(),
                                selected: {widget.verse.translations.containsKey(_selectedVersion.toUpperCase()) ? _selectedVersion.toUpperCase() : widget.verse.translations.keys.first},
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

                  // Issue/Category Display
                  if (widget.verse.issueName != null)
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      color: Theme.of(context).colorScheme.secondaryContainer.withOpacity(0.3),
                      child: Row(
                        children: [
                          Icon(
                            Icons.category,
                            color: Theme.of(context).colorScheme.primary,
                            size: 20,
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Category',
                                  style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                                  ),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  widget.verse.issueName!,
                                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                    fontWeight: FontWeight.w600,
                                    color: Theme.of(context).colorScheme.onSurface,
                                  ),
                                ),
                              ],
                            ),
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
                    for (final entry in widget.verse.translations.entries) ...[
                      _buildComparisonSection(
                        '${BibleVersions.getVersionName(entry.key)} (${entry.key})',
                        entry.value,
                      ),
                      const Divider(height: 1),
                    ],
                  ],

                  const SizedBox(height: 16), // Space before ad
                ],
              ),
            ),
          ),
          BlocBuilder<AuthBloc, AuthState>(
            builder: (context, authState) {
              return BlocBuilder<SubscriptionBloc, SubscriptionState>(
                builder: (context, subState) {
                  final showAd = authState is! Authenticated ||
                      !(subState is SubscriptionLoaded && subState.canPost);
                  if (!showAd) return const SizedBox.shrink();
                  return const AdBannerWidget();
                },
              );
            },
          ),
        ],
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