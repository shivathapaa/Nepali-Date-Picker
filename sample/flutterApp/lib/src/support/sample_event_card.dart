// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

import 'sample_event_payload.dart';

/// A tapped event as a product would draw it, built from the JSON the event
/// carries in its payload.
///
/// The bridge hands the event back exactly as it was given, id and payload
/// untouched, and everything below the name here is this sample reading its own
/// record out of that string. The image links are printed rather than fetched:
/// loading remote images needs an image package, and the point here is the
/// payload, not the network.
class SampleEventCard extends StatelessWidget {
  const SampleEventCard({super.key, required this.event});

  final NepaliEvent event;

  @override
  Widget build(BuildContext context) {
    final payload = event.samplePayload;
    final accent = payload?.accent ?? Theme.of(context).colorScheme.primary;
    final labels = Theme.of(context).textTheme.labelSmall;

    return Card(
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            color: accent.withValues(alpha: 0.25),
            padding: const EdgeInsets.all(12),
            child: Row(
              children: [
                if (payload != null)
                  Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: Icon(payload.iconData, color: accent),
                  ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(event.name,
                          style: Theme.of(context).textTheme.titleMedium),
                      Text(
                        '${event.kind.name} · ${event.year}/${event.month}/${event.dayOfMonth}',
                        style: labels,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12),
            child: payload == null
                ? const Text('This event carries no payload.')
                : Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    spacing: 6,
                    children: [
                      Text(payload.description),
                      if (payload.venue != null)
                        _line('Where', payload.venue!, labels),
                      if (payload.organizer != null)
                        _line('Who', payload.organizer!, labels),
                      if (payload.bannerUrl != null)
                        _line('Banner', payload.bannerUrl!, labels),
                      if (payload.iconUrl != null)
                        _line('Icon', payload.iconUrl!, labels),
                      if (payload.link != null)
                        _line('Link', payload.link!, labels),
                      _line('Correlates to', event.id ?? 'no id', labels),
                      if (payload.tags.isNotEmpty)
                        Wrap(
                          spacing: 6,
                          children: [
                            for (final tag in payload.tags)
                              Chip(
                                label: Text(tag),
                                backgroundColor: accent.withValues(alpha: 0.2),
                                visualDensity: VisualDensity.compact,
                              ),
                          ],
                        ),
                    ],
                  ),
          ),
        ],
      ),
    );
  }

  Widget _line(String label, String value, TextStyle? style) => Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('$label ',
              style: style?.copyWith(fontWeight: FontWeight.w600)),
          Expanded(
            child: Text(value,
                style: style, maxLines: 1, overflow: TextOverflow.ellipsis),
          ),
        ],
      );
}
