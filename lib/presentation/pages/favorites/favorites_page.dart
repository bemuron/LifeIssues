// lib/presentation/pages/favorites/favorites_page.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/constants/app_strings.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/favorites/favorites_bloc.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_state.dart';
import '../../widgets/ad_banner_widget.dart';
import '../../widgets/verse_list_item.dart';
import '../verse_details/verse_details_page.dart';

class FavoritesPage extends StatelessWidget {
  const FavoritesPage({Key? key}) : super(key: key);

  bool _showAd(SubscriptionState subState, AuthState authState) {
    if (authState is! Authenticated) return true;
    if (subState is SubscriptionLoaded) return !subState.canPost;
    return true;
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<SubscriptionBloc, SubscriptionState>(
      builder: (context, subState) {
        return BlocBuilder<AuthBloc, AuthState>(
          builder: (context, authState) {
            final showAd = _showAd(subState, authState);

            return Scaffold(
              appBar: AppBar(
                title: const Text(AppStrings.navFavorites),
              ),
              body: Column(
                children: [
                  Expanded(
                    child: BlocBuilder<FavoritesBloc, FavoritesState>(
                      builder: (context, state) {
                        if (state is FavoritesLoading) {
                          return const Center(
                              child: CircularProgressIndicator());
                        } else if (state is FavoritesLoaded) {
                          if (state.favorites.isEmpty) {
                            return _buildEmptyState(context);
                          }

                          return RefreshIndicator(
                            onRefresh: () async {
                              context
                                  .read<FavoritesBloc>()
                                  .add(LoadFavoritesEvent());
                            },
                            child: ListView.separated(
                              padding: const EdgeInsets.all(16),
                              itemCount: state.favorites.length,
                              separatorBuilder: (context, index) =>
                              const SizedBox(height: 12),
                              itemBuilder: (context, index) {
                                final verse = state.favorites[index];
                                return Hero(
                                  tag: 'verse_${verse.id}',
                                  child: Material(
                                    color: Colors.transparent,
                                    child: VerseListItem(
                                      verse: verse,
                                      onTap: () {
                                        Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                            builder: (context) =>
                                                VerseDetailsPage(
                                                  verse: verse,
                                                  isFavorite:
                                                  state.favorites.isEmpty,
                                                ),
                                          ),
                                        );
                                      },
                                      onFavoriteToggle: () {
                                        context.read<FavoritesBloc>().add(
                                          ToggleFavoriteEvent(
                                            verseId: verse.id,
                                            isFavorite: true,
                                          ),
                                        );
                                      },
                                    ),
                                  ),
                                );
                              },
                            ),
                          );
                        } else if (state is FavoritesError) {
                          return _buildErrorState(context, state.message);
                        }

                        return const SizedBox.shrink();
                      },
                    ),
                  ),

                  // Ad banner — only shown when user is not subscribed
                  if (showAd) const AdBannerWidget(),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(32),
              decoration: BoxDecoration(
                color: AppColors.primary.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.favorite_outline,
                size: 80,
                color: AppColors.primary.withOpacity(0.5),
              ),
            ),
            const SizedBox(height: 24),
            Text(
              AppStrings.noFavorites,
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              'Start saving verses you love by tapping the heart icon',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: AppColors.textSecondary,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32),
            ElevatedButton.icon(
              onPressed: () {
                Navigator.pop(context);
              },
              icon: const Icon(Icons.explore),
              label: const Text('Browse Verses'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(
                  horizontal: 32,
                  vertical: 16,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildErrorState(BuildContext context, String message) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.error_outline,
              size: 80,
              color: AppColors.error,
            ),
            const SizedBox(height: 24),
            Text(
              'Oops!',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              message,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: AppColors.textSecondary,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32),
            ElevatedButton.icon(
              onPressed: () {
                context.read<FavoritesBloc>().add(LoadFavoritesEvent());
              },
              icon: const Icon(Icons.refresh),
              label: const Text(AppStrings.retry),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(
                  horizontal: 32,
                  vertical: 16,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}