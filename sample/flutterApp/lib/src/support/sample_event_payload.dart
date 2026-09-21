// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:nepali_date_picker_kmp/nepali_date_picker_kmp.dart';

/// Everything this sample knows about an event beyond the day it falls on,
/// carried in `NepaliEvent.payload` as JSON.
///
/// The library never looks inside it: a payload is an opaque string that
/// crosses the bridge untouched and comes back on a tap, which is what lets an
/// app put its own record, colours, icons, image links and all, on an event.
@immutable
class SampleEventPayload {
  const SampleEventPayload({
    required this.description,
    required this.icon,
    required this.accentArgb,
    this.bannerUrl,
    this.iconUrl,
    this.tags = const <String>[],
    this.venue,
    this.organizer,
    this.link,
  });

  factory SampleEventPayload.fromJson(Map<String, Object?> json) =>
      SampleEventPayload(
        description: json['description'] as String? ?? '',
        icon: json['icon'] as String? ?? 'info',
        accentArgb: (json['accentArgb'] as num?)?.toInt() ?? 0xFF6750A4,
        bannerUrl: json['bannerUrl'] as String?,
        iconUrl: json['iconUrl'] as String?,
        tags: (json['tags'] as List<Object?>? ?? const <Object?>[])
            .map((tag) => tag.toString())
            .toList(growable: false),
        venue: json['venue'] as String?,
        organizer: json['organizer'] as String?,
        link: json['link'] as String?,
      );

  final String description;
  final String icon;
  final int accentArgb;
  final String? bannerUrl;
  final String? iconUrl;
  final List<String> tags;
  final String? venue;
  final String? organizer;
  final String? link;

  String encode() => jsonEncode(<String, Object?>{
        'description': description,
        'icon': icon,
        'accentArgb': accentArgb,
        if (bannerUrl != null) 'bannerUrl': bannerUrl,
        if (iconUrl != null) 'iconUrl': iconUrl,
        if (tags.isNotEmpty) 'tags': tags,
        if (venue != null) 'venue': venue,
        if (organizer != null) 'organizer': organizer,
        if (link != null) 'link': link,
      });

  Color get accent => Color(accentArgb);

  /// The icon the payload names, falling back to a neutral one for anything
  /// this sample does not recognize.
  IconData get iconData => switch (icon) {
        'star' => Icons.star,
        'place' => Icons.place,
        'person' => Icons.person,
        'reminder' => Icons.notifications,
        'festival' => Icons.celebration,
        'shopping' => Icons.shopping_bag,
        'share' => Icons.share,
        'date' => Icons.event,
        _ => Icons.info_outline,
      };
}

extension SampleEventPayloadReader on NepaliEvent {
  /// The record this event carries, or null when it carries none or carries
  /// something this sample does not understand. The app owns both ends of the
  /// string, so a decode failure means its own data changed shape.
  SampleEventPayload? get samplePayload {
    final raw = payload;
    if (raw == null) return null;
    try {
      return SampleEventPayload.fromJson(
          jsonDecode(raw) as Map<String, Object?>);
    } on FormatException {
      return null;
    }
  }
}
