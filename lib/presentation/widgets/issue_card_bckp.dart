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

  const IssueCard({
    super.key,
    required this.issue,
    required this.index,
    this.isGridView = false,
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
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Hero(
      tag: 'issue_${issue.id}',
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
                  child: IssueVersesPage(issue: issue),
                ),
              ),
            );
          },
          borderRadius: BorderRadius.circular(16),
          child: AspectRatio(
            aspectRatio: isGridView ? 1.0 : 2.5,
            child: Card(
              elevation: isGridView ? 1 : 2,
              clipBehavior: Clip.antiAlias,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: BorderSide(
                  color: Theme.of(context)
                      .colorScheme
                      .outlineVariant
                      .withOpacity(0.3),
                ),
              ),
              child: Stack(
                fit: StackFit.expand,
                children: [
                  // Background Image (if available)
                  if (imageUrl != null)
                    CachedNetworkImage(
                      imageUrl: imageUrl,
                      fit: BoxFit.cover,
                      placeholder: (context, url) => Container(
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topLeft,
                            end: Alignment.bottomRight,
                            colors: [
                              cardColor.withOpacity(0.3),
                              cardColor.withOpacity(0.1),
                            ],
                          ),
                        ),
                      ),
                      errorWidget: (context, url, error) => Container(
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topLeft,
                            end: Alignment.bottomRight,
                            colors: [
                              cardColor.withOpacity(0.3),
                              cardColor.withOpacity(0.1),
                            ],
                          ),
                        ),
                      ),
                    )
                  else
                  // Gradient background (no image)
                    Container(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                          colors: [
                            cardColor.withOpacity(0.3),
                            cardColor.withOpacity(0.1),
                          ],
                        ),
                      ),
                    ),

                  // Overlay for better text readability
                  if (imageUrl != null)
                    Container(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: [
                            isDark
                                ? Colors.black.withOpacity(0.5)
                                : Colors.black.withOpacity(0.7), //Colors.white.withOpacity(0.85),
                            isDark
                                ? Colors.black.withOpacity(0.5)
                                : Colors.black.withOpacity(0.5), //Colors.white.withOpacity(0.7),
                          ],
                        ),
                      ),
                    ),

                  // Content
                  Padding(
                    padding: EdgeInsets.all(isGridView ? 12 : 16),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        // Icon (only if no image)
                        if (imageUrl == null)
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: cardColor.withOpacity(0.2),
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Icon(
                              icon,
                              size: isGridView ? 28 : 36,
                              color: cardColor.withOpacity(0.9),
                            ),
                          ),

                        if (imageUrl == null)
                          SizedBox(height: isGridView ? 8 : 12),

                        // Issue Name
                        Flexible(
                          child: Text(
                            issue.name,
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                              fontSize: isGridView ? 13 : 16,
                              color: imageUrl != null
                                  ? (isDark ? Colors.white : Colors.black87)
                                  : null,
                              shadows: imageUrl != null
                                  ? [
                                Shadow(
                                  color: isDark ? Colors.black54 : Colors.white70,
                                  blurRadius: 4,
                                ),
                              ]
                                  : null,
                            ),
                            textAlign: TextAlign.center,
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),

                        if (!isGridView && issue.description.isNotEmpty) ...[
                          const SizedBox(height: 6),
                          Flexible(
                            child: Text(
                              issue.description,
                              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                fontSize: 12,
                                color: imageUrl != null
                                    ? (isDark
                                    ? Colors.white.withOpacity(0.8)
                                    : Colors.black.withOpacity(0.7))
                                    : Theme.of(context).colorScheme.onSurfaceVariant,
                                shadows: imageUrl != null
                                    ? [
                                  Shadow(
                                    color: isDark ? Colors.black45 : Colors.white60,
                                    blurRadius: 3,
                                  ),
                                ]
                                    : null,
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
          ),
        ),
      ),
    );
  }
}