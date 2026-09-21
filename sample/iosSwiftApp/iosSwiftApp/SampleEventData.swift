//
//  SampleEventData.swift
//  Nepali Date Picker
//
//  The event data the showcase marks its calendars with. The library ships none by design, so this
//  is the part a real app brings: a few named days, the app's own entries, and one festival that
//  runs longer than a day.
//

import SwiftUI
import nepali_date_picker

struct SampleEventData {
    let today: SimpleDate

    private let converter = NepaliDateConverter.shared

    /// Named days near today, so a picker always opens on a month with something on it.
    var events: [NepaliCalendarEvent] {
        [
            event(offset: 2, "Company day (demo)", .observance),
            event(offset: 3, "Local jatra (demo)", .regional),
            event(offset: 9, "Offices closed (demo)", .governmentpublic)
        ]
    }

    /// What the app itself keeps: meetings and errands, none of which close anything. Four land on
    /// one day, which is what shows the cell's three-dot cap.
    var ownEvents: [NepaliCalendarEvent] {
        [
            event(offset: 1, "Standup", .observance),
            event(offset: 4, "Sprint review", .observance),
            event(offset: 4, "Aama's birthday", .observance),
            event(offset: 6, "Dentist", .observance),
            event(offset: 6, "Bank errand", .observance),
            event(offset: 6, "Friend's wedding", .observance),
            event(offset: 6, "Design review", .observance)
        ]
    }

    /// A ten-day festival written once and expanded into the entries the calendar reads. An event
    /// covers one day, so a span is a list rather than a range, and each day keeps the shared id.
    var festivalSpan: [NepaliCalendarEvent] {
        NepaliCalendarEvent(
            date: offset(3),
            name: "Dashain (demo)",
            kind: .religious,
            closesOffices: true,
            id: "dashain-demo",
            payload: nil
        ).spanningDays(days: 10)
    }

    /// A week of leave, stated by the day it ends on. An observance names its days without closing
    /// them, so the office still counts them as worked.
    var leaveSpan: [NepaliCalendarEvent] {
        NepaliCalendarEvent(
            date: offset(2),
            name: "Annual leave (demo)",
            kind: .observance,
            closesOffices: false,
            id: "leave-42",
            payload: nil
        ).spanningThrough(end: offset(6))
    }

    /// The colours the app paints its own dots in, as `0xAARRGGBB`.
    static let dotColors: [Int32] = [
        argb(0xFF1E88E5),
        argb(0xFFF4511E),
        argb(0xFF43A047),
        argb(0xFF8E24AA)
    ]

    /// The Bikram Sambat date [days] days from today, rolling over the month and year ends.
    func offset(_ days: Int32) -> SimpleDate {
        converter.getNepaliCalendarAfterAdditionOrSubtraction(
            year: today.year,
            month: today.month,
            dayOfMonth: today.dayOfMonth,
            daysToAdjust: days
        ).simple
    }

    private func event(offset days: Int32, _ name: String, _ kind: NepaliEventKind) -> NepaliCalendarEvent {
        NepaliCalendarEvent(
            date: offset(days),
            name: name,
            kind: kind,
            closesOffices: kind.closesOfficesByDefault,
            id: nil,
            payload: nil
        )
    }

    /// A month's worth of events carrying the app's own record in the payload: a colour, an icon,
    /// a description, tags and image links. The library never reads the string; it hands it back
    /// when the entry is tapped, which is what makes a payload worth filling.
    var richEvents: [NepaliCalendarEvent] {
        [
            rich(offset: 1, "Sprint planning", .observance, id: "sprint-planning",
                 payload: SampleEventPayload(
                    description: "Scope for the fortnight, then estimates.",
                    icon: "date",
                    accentArgb: 0xFF1E88E5,
                    tags: ["work", "recurring"],
                    venue: "Meeting room 3B")),
            rich(offset: 3, "Aama's birthday", .observance, id: "birthday-aama",
                 payload: SampleEventPayload(
                    description: "Seventy this year. Cake at home in the evening.",
                    icon: "festival",
                    accentArgb: 0xFFEC407A,
                    iconUrl: "https://upload.wikimedia.org/wikipedia/commons/4/45/Birthday_cake.jpg",
                    tags: ["personal", "family"])),
            rich(offset: 5, "Dentist", .observance, id: "dentist",
                 payload: SampleEventPayload(
                    description: "Six-month check, the one that keeps being postponed.",
                    icon: "reminder",
                    accentArgb: 0xFF00897B,
                    tags: ["health"],
                    venue: "Lalitpur Dental")),
            rich(offset: 8, "Kirtipur trek", .observance, id: "trek",
                 payload: SampleEventPayload(
                    description: "Leaves at five in the morning, back by noon.",
                    icon: "place",
                    accentArgb: 0xFF43A047,
                    bannerUrl: "https://upload.wikimedia.org/wikipedia/commons/5/5e/Kirtipur_Nepal.jpg",
                    tags: ["outdoors"],
                    venue: "Kirtipur")),
            rich(offset: 11, "Quarterly review", .observance, id: "quarterly-review",
                 payload: SampleEventPayload(
                    description: "Numbers first, then the roadmap.",
                    icon: "star",
                    accentArgb: 0xFF5E35B1,
                    tags: ["work"],
                    organizer: "Leadership")),
            rich(offset: 11, "Dashain shopping", .observance, id: "dashain-shopping",
                 payload: SampleEventPayload(
                    description: "Clothes for the children, then Asan for spices.",
                    icon: "shopping",
                    accentArgb: 0xFFEF6C00,
                    tags: ["errand"],
                    venue: "Asan")),
            rich(offset: 14, "Blood donation camp", .regional, id: "blood-donation",
                 payload: SampleEventPayload(
                    description: "Ward-level camp, walk-ins welcome until four.",
                    icon: "festival",
                    accentArgb: 0xFFC62828,
                    tags: ["community", "health"],
                    organizer: "Nepal Red Cross",
                    link: "https://nrcs.org"))
        ]
    }

    private func rich(
        offset days: Int32,
        _ name: String,
        _ kind: NepaliEventKind,
        id: String,
        payload: SampleEventPayload
    ) -> NepaliCalendarEvent {
        NepaliCalendarEvent(
            date: offset(days),
            name: name,
            kind: kind,
            closesOffices: kind.closesOfficesByDefault,
            id: id,
            payload: payload.encoded
        )
    }
}

/// Everything this sample knows about an event beyond the day it falls on, carried in the event's
/// payload as JSON. The library treats it as an opaque string, so an app can put whatever its own
/// screens need in there.
struct SampleEventPayload: Codable {
    var description: String
    var icon: String
    var accentArgb: Int64
    var bannerUrl: String?
    var iconUrl: String?
    var tags: [String] = []
    var venue: String?
    var organizer: String?
    var link: String?

    /// The payload as the string an event carries.
    var encoded: String {
        guard let data = try? JSONEncoder().encode(self) else { return "{}" }
        return String(decoding: data, as: UTF8.self)
    }

    /// The record an event carries, or nil when it carries none or carries something this sample
    /// does not understand.
    static func decode(from event: NepaliCalendarEvent) -> SampleEventPayload? {
        guard let payload = event.payload, let data = payload.data(using: .utf8) else { return nil }
        return try? JSONDecoder().decode(SampleEventPayload.self, from: data)
    }

    /// The SF Symbol the payload names, falling back to a neutral one.
    var symbolName: String {
        switch icon {
        case "star": return "star.fill"
        case "place": return "mappin.and.ellipse"
        case "person": return "person.fill"
        case "reminder": return "bell.fill"
        case "festival": return "party.popper.fill"
        case "shopping": return "bag.fill"
        case "share": return "square.and.arrow.up"
        case "date": return "calendar"
        default: return "info.circle"
        }
    }
}
