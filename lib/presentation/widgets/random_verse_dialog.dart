// lib/presentation/widgets/random_verse_dialog.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../blocs/random_verse/random_verse_bloc.dart';
import '../blocs/favorites/favorites_bloc.dart';
import '../../core/constants/app_colors.dart';
import 'package:share_plus/share_plus.dart';

class RandomVerseDialog extends StatelessWidget {
  const RandomVerseDialog({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<RandomVerseBloc, RandomVerseState>(
      builder: (context, state) {
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
                    color: AppColors.primary,
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
                    child: _buildContent(context, state),
                  ),
                ),

                // Actions
                if (state is RandomVerseLoaded)
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        IconButton(
                          icon: Icon(
                            state.verse.isFavorite
                                ? Icons.favorite
                                : Icons.favorite_outline,
                            color: AppColors.error,
                          ),
                          onPressed: () {
                            if (state.verse.isFavorite) {
                              context.read<FavoritesBloc>().add(
                                ToggleFavoriteEvent(verseId: state.verse.id, isFavorite: false),
                              );
                            } else {
                              context.read<FavoritesBloc>().add(
                                ToggleFavoriteEvent(verseId: state.verse.id, isFavorite: true),
                              );
                            }
                            context.read<RandomVerseBloc>().add(LoadRandomVerse());
                          },
                        ),
                        IconButton(
                          icon: const Icon(Icons.share, color: AppColors.primary),
                          onPressed: () {
                            Share.share(
                              '${state.verse.reference}\n\n${state.verse.text}\n\n- Life Issues App',
                            );
                          },
                        ),
                        ElevatedButton.icon(
                          onPressed: () {
                            context.read<RandomVerseBloc>().add(LoadRandomVerse());
                          },
                          icon: const Icon(Icons.shuffle),
                          label: const Text('Next'),
                        ),
                      ],
                    ),
                  ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildContent(BuildContext context, RandomVerseState state) {
    if (state is RandomVerseLoading) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(40),
          child: CircularProgressIndicator(),
        ),
      );
    } else if (state is RandomVerseLoaded) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Reference
          Text(
            state.verse.reference,
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              color: AppColors.primary,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 16),

          // Verse Text
          Text(
            state.verse.text,
            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
              height: 1.6,
            ),
          ),
        ],
      );
    } else if (state is RandomVerseError) {
      return Column(
        children: [
          const Icon(Icons.error_outline, color: AppColors.error, size: 48),
          const SizedBox(height: 16),
          Text(
            state.message,
            style: const TextStyle(color: AppColors.error),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () {
              context.read<RandomVerseBloc>().add(LoadRandomVerse());
            },
            child: const Text('Retry'),
          ),
        ],
      );
    }

    return const SizedBox.shrink();
  }
}