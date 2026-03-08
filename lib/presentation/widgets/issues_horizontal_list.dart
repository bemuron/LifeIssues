import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/issue.dart';
import '../../core/di/injection_container.dart' as di;
import '../blocs/verses/verses_bloc.dart';
import '../pages/issue_verses_page.dart';

class IssuesHorizontalList extends StatelessWidget {
  final List<Issue> issues;

  const IssuesHorizontalList({
    super.key,
    required this.issues,
  });

  @override
  Widget build(BuildContext context) {
    // Take only the first 10 issues for the horizontal list
    final displayIssues = issues.take(10).toList();

    return SizedBox(
      height: 140,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        itemCount: displayIssues.length,
        itemBuilder: (context, index) {
          final issue = displayIssues[index];
          return Hero(
            tag: 'issue_${issue.id}',
            child: Card(
              margin: const EdgeInsets.symmetric(horizontal: 4),
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: BorderSide(
                  color: Theme.of(context)
                      .colorScheme
                      .outlineVariant
                      .withOpacity(0.5),
                ),
              ),
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
                child: Container(
                  width: 120,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(16),
                    gradient: LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: [
                        Theme.of(context)
                            .colorScheme
                            .primaryContainer
                            .withOpacity(0.3),
                        Theme.of(context)
                            .colorScheme
                            .secondaryContainer
                            .withOpacity(0.3),
                      ],
                    ),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.favorite,
                        size: 32,
                        color: Theme.of(context)
                            .colorScheme
                            .primary
                            .withOpacity(0.7),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        issue.name,
                        style: Theme.of(context).textTheme.titleSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.center,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}