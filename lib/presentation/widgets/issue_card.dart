import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/issue.dart';
import '../../core/constants/app_colors.dart';
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

  @override
  Widget build(BuildContext context) {
    final cardColor = _getCardColor();
    final icon = _getIconForIssue(issue.name);

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
          child: Card(
            elevation: isGridView ? 1 : 2,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
              side: BorderSide(
                color: Theme.of(context)
                    .colorScheme
                    .outlineVariant
                    .withOpacity(0.3),
              ),
            ),
            child: Container(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    cardColor.withOpacity(0.3),
                    cardColor.withOpacity(0.1),
                  ],
                ),
              ),
              child: Padding(
                padding: EdgeInsets.all(isGridView ? 16 : 20),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    // Icon
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: cardColor.withOpacity(0.2),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Icon(
                        icon,
                        size: isGridView ? 32 : 40,
                        color: cardColor.withOpacity(0.9),
                      ),
                    ),
                    SizedBox(height: isGridView ? 12 : 16),

                    // Issue Name
                    Text(
                      issue.name,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                      textAlign: TextAlign.center,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),

                    if (!isGridView && issue.description.isNotEmpty) ...[
                      const SizedBox(height: 8),
                      Text(
                        issue.description,
                        style:
                        Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: Theme.of(context)
                              .colorScheme
                              .onSurfaceVariant,
                        ),
                        textAlign: TextAlign.center,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}