import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:life_issues_flutter/presentation/pages/verse_details/verse_details_page.dart';
import '../../core/constants/image_config.dart';
import '../../domain/entities/issue.dart';
import '../blocs/auth/auth_bloc.dart';
import '../blocs/auth/auth_state.dart';
import '../blocs/subscription/subscription_bloc.dart';
import '../blocs/subscription/subscription_state.dart';
import '../blocs/verses/verses_bloc.dart';
import '../widgets/ad_banner_widget.dart';
import '../widgets/verse_card_horizontal.dart';

class IssueVersesPage extends StatefulWidget {
  final Issue issue;

  const IssueVersesPage({
    super.key,
    required this.issue,
  });

  @override
  State<IssueVersesPage> createState() => _IssueVersesPageState();
}

class _IssueVersesPageState extends State<IssueVersesPage> {
  final PageController _pageController = PageController(viewportFraction: 0.92);
  int _currentPage = 0;

  @override
  void initState() {
    super.initState();
    _pageController.addListener(() {
      final next = _pageController.page!.round();
      if (_currentPage != next) {
        setState(() => _currentPage = next);
      }
    });
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  bool _showAd(SubscriptionState subState, AuthState authState) {
    if (authState is! Authenticated) return true;
    if (subState is SubscriptionLoaded) return !subState.canPost;
    return true;
  }

  String? _getImageUrl() {
    final url = ImageConfig.getIssueImageUrl(widget.issue.image);
    return url.isNotEmpty ? url : null;
  }

  @override
  Widget build(BuildContext context) {
    final imageUrl = _getImageUrl();
    final cs = Theme.of(context).colorScheme;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    // surfaceContainerLow: one tonal step above raw surface — warm tinted
    // off-white in light mode, deep tinted charcoal in dark mode.
    final bgColor = cs.surfaceContainerLow;

    return AnnotatedRegion<SystemUiOverlayStyle>(
      value: isDark ? SystemUiOverlayStyle.light : SystemUiOverlayStyle.dark,
      child: Scaffold(
        backgroundColor: bgColor,
        body: BlocBuilder<SubscriptionBloc, SubscriptionState>(
          builder: (context, subState) {
            return BlocBuilder<AuthBloc, AuthState>(
              builder: (context, authState) {
                final showAd = _showAd(subState, authState);

                return Column(
                  children: [
                    Expanded(
                      child: CustomScrollView(
                        slivers: [
                          // ── Hero / App Bar ──────────────────────────────
                          SliverAppBar(
                            expandedHeight: 220,
                            pinned: true,
                            backgroundColor: bgColor,
                            surfaceTintColor: cs.primaryContainer,
                            foregroundColor:
                            isDark ? Colors.white : cs.onSurface,
                            flexibleSpace: FlexibleSpaceBar(
                              title: Text(
                                widget.issue.name,
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: Colors.white,
                                  shadows: [
                                    Shadow(
                                      offset: Offset(0, 1),
                                      blurRadius: 4,
                                      color: Colors.black54,
                                    ),
                                  ],
                                ),
                              ),
                              background: Hero(
                                tag: 'issue_${widget.issue.id}',
                                child: _buildHeroBackground(
                                    context, imageUrl, cs),
                              ),
                            ),
                          ),

                          // ── Description ─────────────────────────────────
                          SliverToBoxAdapter(
                            child: Padding(
                              padding: const EdgeInsets.all(16.0),
                              child: Text(
                                widget.issue.description,
                                style: Theme.of(context)
                                    .textTheme
                                    .bodyLarge
                                    ?.copyWith(
                                  color: cs.onSurfaceVariant,
                                  height: 1.5,
                                ),
                                textAlign: TextAlign.center,
                              ),
                            ),
                          ),

                          // ── Verses ──────────────────────────────────────
                          BlocBuilder<VersesBloc, VersesState>(
                            builder: (context, state) {
                              if (state is VersesLoading) {
                                return const SliverFillRemaining(
                                  child: Center(
                                      child: CircularProgressIndicator()),
                                );
                              } else if (state is VersesLoaded) {
                                if (state.verses.isEmpty) {
                                  return const SliverFillRemaining(
                                    child: Center(
                                      child: Text(
                                          'No verses found for this issue'),
                                    ),
                                  );
                                }

                                return SliverToBoxAdapter(
                                  child: Column(
                                    children: [
                                      SizedBox(
                                        height: 450,
                                        child: PageView.builder(
                                          controller: _pageController,
                                          itemCount: state.verses.length,
                                          itemBuilder: (context, index) {
                                            final verse = state.verses[index];
                                            final isActive =
                                                index == _currentPage;

                                            return AnimatedScale(
                                              scale: isActive ? 1.0 : 0.9,
                                              duration: const Duration(
                                                  milliseconds: 300),
                                              curve: Curves.easeOutCubic,
                                              child: AnimatedOpacity(
                                                opacity:
                                                isActive ? 1.0 : 0.5,
                                                duration: const Duration(
                                                    milliseconds: 300),
                                                child: GestureDetector(
                                                  onTap: () {
                                                    Navigator.push(
                                                      context,
                                                      MaterialPageRoute(
                                                        builder: (context) =>
                                                            VerseDetailsPage(
                                                              verse: verse,
                                                              isFavorite:
                                                              verse.isFavorite,
                                                            ),
                                                      ),
                                                    );
                                                  },
                                                  child: VerseCardHorizontal(
                                                      verse: verse),
                                                ),
                                              ),
                                            );
                                          },
                                        ),
                                      ),
                                      const SizedBox(height: 16),

                                      // Page indicator dots
                                      Row(
                                        mainAxisAlignment:
                                        MainAxisAlignment.center,
                                        children: List.generate(
                                          state.verses.length,
                                              (index) => AnimatedContainer(
                                            duration: const Duration(
                                                milliseconds: 300),
                                            margin: const EdgeInsets.symmetric(
                                                horizontal: 4),
                                            width: _currentPage == index
                                                ? 24
                                                : 8,
                                            height: 8,
                                            decoration: BoxDecoration(
                                              color: _currentPage == index
                                                  ? cs.primary
                                                  : cs.primary
                                                  .withOpacity(0.2),
                                              borderRadius:
                                              BorderRadius.circular(4),
                                            ),
                                          ),
                                        ),
                                      ),
                                      const SizedBox(height: 24),

                                      Padding(
                                        padding: const EdgeInsets.symmetric(
                                            horizontal: 16.0),
                                        child: Text(
                                          'Swipe to explore ${state.verses.length} verses',
                                          style: Theme.of(context)
                                              .textTheme
                                              .bodySmall
                                              ?.copyWith(
                                            color: cs.onSurfaceVariant,
                                          ),
                                        ),
                                      ),
                                      const SizedBox(height: 32),
                                    ],
                                  ),
                                );
                              } else if (state is VersesError) {
                                return SliverFillRemaining(
                                  child: Center(
                                    child: Column(
                                      mainAxisAlignment:
                                      MainAxisAlignment.center,
                                      children: [
                                        const Icon(Icons.error_outline,
                                            size: 64, color: Colors.red),
                                        const SizedBox(height: 16),
                                        Text(state.message),
                                        const SizedBox(height: 16),
                                        ElevatedButton(
                                          onPressed: () {
                                            context.read<VersesBloc>().add(
                                              LoadVersesForIssueEvent(
                                                  widget.issue.id),
                                            );
                                          },
                                          child: const Text('Retry'),
                                        ),
                                      ],
                                    ),
                                  ),
                                );
                              }
                              return const SliverFillRemaining(
                                  child: SizedBox.shrink());
                            },
                          ),
                        ],
                      ),
                    ),

                    // ── Conditional Ad Banner ───────────────────────────
                    if (showAd) const AdBannerWidget(),
                  ],
                );
              },
            );
          },
        ),
      ),
    );
  }

  Widget _buildHeroBackground(
      BuildContext context, String? imageUrl, ColorScheme cs) {
    if (imageUrl != null) {
      return Stack(
        fit: StackFit.expand,
        children: [
          CachedNetworkImage(
            imageUrl: imageUrl,
            fit: BoxFit.cover,
            placeholder: (_, __) => _gradientFallback(cs),
            errorWidget: (_, __, ___) => _gradientFallback(cs),
          ),
          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                stops: const [0.0, 0.45, 1.0],
                colors: [
                  Colors.black.withOpacity(0.15),
                  Colors.black.withOpacity(0.35),
                  Colors.black.withOpacity(0.65),
                ],
              ),
            ),
          ),
        ],
      );
    }

    return _gradientFallback(cs);
  }

  Widget _gradientFallback(ColorScheme cs) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [cs.primaryContainer, cs.secondaryContainer],
        ),
      ),
      child: Center(
        child: Icon(
          Icons.favorite,
          size: 80,
          color: cs.onPrimaryContainer.withOpacity(0.2),
        ),
      ),
    );
  }
}