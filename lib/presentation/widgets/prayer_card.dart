// lib/presentation/widgets/prayer_card.dart

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:intl/intl.dart';
import '../../domain/entities/prayer.dart';
import '../pages/prayers/prayer_detail_page.dart';

class PrayerCard extends StatelessWidget {
  final Prayer prayer;
  final VoidCallback? onTapPraying;
  /// Override the default card tap (navigate to detail). If provided, this
  /// is called instead of pushing PrayerDetailPage.
  final VoidCallback? onTap;

  const PrayerCard({
    Key? key,
    required this.prayer,
    this.onTapPraying,
    this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
      ),
      child: InkWell(
        onTap: onTap ?? () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => PrayerDetailPage(prayerId: prayer.id),
            ),
          );
        },
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header row
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildAvatar(context),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          prayer.isAnonymous
                              ? 'Anonymous'
                              : (prayer.posterName ?? 'Unknown'),
                          style: theme.textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          _getRelativeTime(prayer.createdAt),
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (prayer.answered) _buildAnsweredBadge(context),
                ],
              ),

              const SizedBox(height: 12),

              // Category chip
              if (prayer.category != null) ...[
                _buildCategoryChip(context, prayer.category!),
                const SizedBox(height: 8),
              ],

              // Prayer body
              Text(
                prayer.body,
                style: theme.textTheme.bodyMedium?.copyWith(height: 1.5),
                maxLines: 3,
                overflow: TextOverflow.ellipsis,
              ),

              const SizedBox(height: 12),

              // Footer
              Row(
                children: [
                  FaIcon(
                    FontAwesomeIcons.handsPraying,
                    size: 16,
                    color: prayer.hasPrayed
                        ? colorScheme.primary
                        : colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(width: 4),
                  Text(
                    '${prayer.prayCount} ${prayer.prayCount == 1 ? 'prayer' : 'prayers'}',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const Spacer(),
                  _buildPrayButton(context),
                ],
              ),

              // Linked testimony
              if (prayer.linkedTestimony != null) ...[
                const SizedBox(height: 10),
                Divider(height: 1, color: colorScheme.outlineVariant),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Icon(Icons.auto_awesome, size: 14, color: colorScheme.tertiary),
                    const SizedBox(width: 6),
                    Expanded(
                      child: Text(
                        'Answered: ${prayer.linkedTestimony!.title}',
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: colorScheme.tertiary,
                          fontWeight: FontWeight.w600,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    Icon(Icons.arrow_forward_ios, size: 11, color: colorScheme.tertiary),
                  ],
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAvatar(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final hasImage = prayer.profileImageUrl != null && !prayer.isAnonymous;

    return CircleAvatar(
      radius: 22,
      backgroundColor: colorScheme.primaryContainer,
      backgroundImage: hasImage ? NetworkImage(prayer.profileImageUrl!) : null,
      child: hasImage
          ? null
          : Icon(
              prayer.isAnonymous ? Icons.person_off_outlined : Icons.person_outlined,
              size: 22,
              color: colorScheme.onPrimaryContainer,
            ),
    );
  }

  Widget _buildAnsweredBadge(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.green.withOpacity(0.12),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.green.shade400, width: 1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.check_circle_outline, size: 12, color: Colors.green.shade700),
          const SizedBox(width: 4),
          Text(
            'Answered',
            style: TextStyle(
              color: Colors.green.shade700,
              fontSize: 11,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCategoryChip(BuildContext context, String category) {
    final colorScheme = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: colorScheme.secondaryContainer,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        category,
        style: TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.w500,
          color: colorScheme.onSecondaryContainer,
        ),
      ),
    );
  }

  Widget _buildPrayButton(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    if (prayer.hasPrayed) {
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
        decoration: BoxDecoration(
          color: colorScheme.primaryContainer,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.check, size: 14, color: colorScheme.onPrimaryContainer),
            const SizedBox(width: 4),
            Text(
              'Praying',
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w600,
                color: colorScheme.onPrimaryContainer,
              ),
            ),
          ],
        ),
      );
    }

    return FilledButton.tonal(
      onPressed: onTapPraying,
      style: FilledButton.styleFrom(
        visualDensity: VisualDensity.compact,
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          FaIcon(
            FontAwesomeIcons.handsPraying,
            size: 14,
            color: colorScheme.onSecondaryContainer,
          ),
          SizedBox(width: 4),
          Text('Pray',
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: colorScheme.onSecondaryContainer,),
          ),
        ],
      ),
    );
  }

  String _getRelativeTime(DateTime dateTime) {
    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inDays > 7) {
      return DateFormat('MMM d').format(dateTime);
    } else if (difference.inDays > 0) {
      return '${difference.inDays}d ago';
    } else if (difference.inHours > 0) {
      return '${difference.inHours}h ago';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes}m ago';
    } else {
      return 'Just now';
    }
  }
}
