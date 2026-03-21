import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../domain/entities/issue.dart';
import '../../core/constants/app_colors.dart';
import '../../core/constants/image_config.dart';
import '../../core/di/injection_container.dart' as di;
import '../blocs/verses/verses_bloc.dart';
import '../pages/issue_verses_page.dart';

class IssueCard extends StatelessWidget {
  final Issue issue;
  final int index;
  final bool isGridView;
  final String heroTagPrefix;

  const IssueCard({
    super.key,
    required this.issue,
    required this.index,
    this.isGridView = false,
    this.heroTagPrefix = '',
  });

  IconData _getIconForIssue(String issueName) {
    final name = issueName.toLowerCase();
    if (name.contains('love')) return Icons.favorite;
    if (name.contains('anger')) return Icons.whatshot;
    if (name.contains('anxiety') || name.contains('worry')) return Icons.psychology;
    if (name.contains('faith')) return Icons.church;
    if (name.contains('peace')) return Icons.self_improvement;
    if (name.contains('family')) return Icons.family_restroom;
    if (name.contains('forgiveness')) return Icons.handshake;
    if (name.contains('courage') || name.contains('strength')) return Icons.fitness_center;
    if (name.contains('wisdom')) return Icons.school;
    if (name.contains('gratitude')) return Icons.volunteer_activism;
    if (name.contains('prayer')) return Icons.church_outlined;
    if (name.contains('hope')) return Icons.wb_sunny;
    if (name.contains('joy') || name.contains('happiness')) return Icons.sentiment_very_satisfied;
    if (name.contains('patience')) return Icons.hourglass_empty;
    if (name.contains('healing') || name.contains('health')) return Icons.healing;
    if (name.contains('marriage')) return Icons.diamond;
    if (name.contains('money') || name.contains('finance')) return Icons.attach_money;
    if (name.contains('work') || name.contains('job')) return Icons.work;
    return Icons.menu_book;
  }

  Color _getCardColor() {
    return AppColors.getRandomIssueColor(index);
  }

  String? _getImageUrl() {
    final url = ImageConfig.getIssueImageUrl(issue.image);
    return url.isNotEmpty ? url : null;
  }

  @override
  Widget build(BuildContext context) {
    final cardColor = _getCardColor();
    final icon = _getIconForIssue(issue.name);
    final imageUrl = _getImageUrl();

    final heroTag = heroTagPrefix.isEmpty 
        ? 'issue_${issue.id}' 
        : '${heroTagPrefix}_issue_${issue.id}';
    
    return Hero(
      tag: heroTag,
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => BlocProvider(
                  create: (_) => di.sl<VersesBloc>()
                    ..add(LoadVersesForIssueEvent(issue.id)),
                  child: IssueVersesPage(
                    issue: issue,
                    heroTag: heroTag,
                  ),
                ),
              ),
            );
          },
          borderRadius: BorderRadius.circular(16),
          child: isGridView
              ? _buildGridCard(context, cardColor, icon, imageUrl)
              : _buildListCard(context, cardColor, icon, imageUrl),
        ),
      ),
    );
  }

  // ── Grid card ──────────────────────────────────────────────────────────────
  // Natural height so MasonryGridView can stagger properly.
  // Name pinned to the bottom behind a dark gradient scrim — always white text.
  Widget _buildGridCard(
      BuildContext context,
      Color cardColor,
      IconData icon,
      String? imageUrl,
      ) {
    return Card(
      elevation: 4,
      clipBehavior: Clip.antiAlias,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(
          color: Theme.of(context).colorScheme.outlineVariant.withOpacity(0.3),
        ),
      ),
      child: Stack(
        children: [
          _buildBackground(imageUrl, cardColor),

          // Bottom gradient scrim — name always readable in white
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.bottomCenter,
                  end: Alignment.topCenter,
                  stops: const [0.0, 0.75, 1.0],
                  colors: [
                    Colors.black.withOpacity(0.78),
                    Colors.black.withOpacity(0.35),
                    Colors.transparent,
                  ],
                ),
              ),
              padding: const EdgeInsets.fromLTRB(10, 28, 10, 12),
              child: Text(
                issue.name,
                style: Theme.of(context).textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w800,
                  fontSize: 13,
                  color: Colors.white,
                  letterSpacing: 0.2,
                  shadows: const [
                    Shadow(
                      color: Colors.black54,
                      blurRadius: 6,
                      offset: Offset(0, 1),
                    ),
                  ],
                ),
                textAlign: TextAlign.center,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ),

          // Icon centred in the body when there is no image
          if (imageUrl == null)
            Positioned(
              top: 0,
              left: 0,
              right: 0,
              bottom: 48,
              child: Center(
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: cardColor.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(
                    icon,
                    size: 28,
                    color: cardColor.withOpacity(0.9),
                  ),
                ),
              ),
            ),

          // Invisible spacer so MasonryGridView measures natural height
          const Padding(
            padding: EdgeInsets.fromLTRB(10, 56, 10, 48),
            child: SizedBox.shrink(),
          ),
        ],
      ),
    );
  }

  // ── List card ──────────────────────────────────────────────────────────────
  // Used by the home page horizontal strip and the AllIssues list view.
  // Always shows white text over a bottom gradient — works on both the
  // colour-gradient background (no image) and over a photo.
  Widget _buildListCard(
      BuildContext context,
      Color cardColor,
      IconData icon,
      String? imageUrl,
      ) {
    return AspectRatio(
      aspectRatio: 2.5,
      child: Card(
        elevation: 2,
        clipBehavior: Clip.antiAlias,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(
            color: Theme.of(context).colorScheme.outlineVariant.withOpacity(0.3),
          ),
        ),
        child: Stack(
          fit: StackFit.expand,
          children: [
            // Background: network image or colour gradient
            _buildBackground(imageUrl, cardColor),

            // Uniform dark scrim so white text is always legible, whether
            // the card has a photo or just the colour gradient background.
            Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  stops: const [0.0, 0.5, 1.0],
                  colors: [
                    Colors.black.withOpacity(0.10),
                    Colors.black.withOpacity(0.30),
                    Colors.black.withOpacity(0.55),
                  ],
                ),
              ),
            ),

            // Content: icon (no-image only) + name + optional description
            Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (imageUrl == null)
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.15),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Icon(
                        icon,
                        size: 24,
                        color: Colors.white.withOpacity(0.95),
                      ),
                    ),

                  if (imageUrl == null) const SizedBox(height: 8),

                  Flexible(
                    child: Text(
                      issue.name,
                      style: Theme.of(context).textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.w800,
                        fontSize: 13,
                        color: Colors.white,
                        letterSpacing: 0.1,
                        shadows: const [
                          Shadow(
                            color: Colors.black54,
                            blurRadius: 4,
                            offset: Offset(0, 1),
                          ),
                        ],
                      ),
                      textAlign: TextAlign.center,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),

                  if (issue.description.isNotEmpty) ...[
                    const SizedBox(height: 4),
                    Flexible(
                      child: Text(
                        issue.description,
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          fontSize: 11,
                          color: Colors.white.withOpacity(0.80),
                          shadows: const [
                            Shadow(
                              color: Colors.black45,
                              blurRadius: 3,
                            ),
                          ],
                        ),
                        textAlign: TextAlign.center,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBackground(String? imageUrl, Color cardColor) {
    if (imageUrl != null) {
      return CachedNetworkImage(
        imageUrl: imageUrl,
        fit: BoxFit.cover,
        width: double.infinity,
        height: double.infinity,
        placeholder: (context, url) => _gradientBox(cardColor),
        errorWidget: (context, url, error) => _gradientBox(cardColor),
      );
    }
    return _gradientBox(cardColor);
  }

  Widget _gradientBox(Color cardColor) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            cardColor.withOpacity(0.85),
            cardColor.withOpacity(0.55),
          ],
        ),
      ),
    );
  }
}