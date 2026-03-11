import 'dart:ui';

import 'package:flutter/material.dart';

class CommunityCard extends StatelessWidget {
  final String icon;
  final String label;
  final bool isLoading;
  final VoidCallback onTap;
  final int maxLines;

  const CommunityCard({
    required this.icon,
    required this.label,
    required this.isLoading,
    required this.onTap,
    this.maxLines = 1,
  });

  @override
  Widget build(BuildContext context) {
    // HS-05: "slightly lighter dark surface card" — use surfaceContainerHigh
    // which sits one level above the page surface in Material 3 dark theme.
    final cs = Theme.of(context).colorScheme;
    final cardColor = cs.surfaceContainerHigh;

    return Material(
      color: cardColor,
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(icon, style: const TextStyle(fontSize: 24)),
              const SizedBox(height: 8),
              if (isLoading)
                Container(
                  height: 14,
                  width: 80,
                  decoration: BoxDecoration(
                    color: cs.onSurface.withOpacity(0.15),
                    borderRadius: BorderRadius.circular(4),
                  ),
                )
              else
                Text(
                  label,
                  style: TextStyle(
                    color: cs.onSurface,
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    height: 1.4,
                  ),
                  maxLines: maxLines,
                  overflow: TextOverflow.ellipsis,
                ),
            ],
          ),
        ),
      ),
    );
  }
}