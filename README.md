# Meeting Scheduler — LLD

A low-level design implementation of a **Meeting Scheduler** that allows users to schedule, cancel, and reschedule meetings in meeting rooms while preventing room-level time conflicts.

The implementation is intentionally kept simple and interview-friendly, with clear separation of responsibilities between the scheduler, meeting rooms, calendars, meetings, and notification service.

---

## 1. Requirements

### Functional Requirements

The system should support:

* Schedule a meeting in a meeting room.
* Cancel an existing meeting.
* Reschedule an existing meeting.
* Prevent overlapping meetings in the same room.
* Allow meetings in different rooms at the same time.
* Validate that the requested meeting room exists.
* Validate that the meeting time is valid.
* Notify participants when a meeting is scheduled, cancelled, or rescheduled.

### Current Scope

The current implementation checks **meeting-room availability**.

### Future Scope

User/participant calendar availability can be added later.

For example:

```text
User Calendar
      |
      +---- Meeting 1
      |
      +---- Meeting 2
```

Before scheduling a meeting, the scheduler could then validate:

```text
Room available?
       +
Organizer available?
       +
Participants available?
```

This is intentionally kept outside the current scope to keep the core LLD focused.

---

# 2. Main Entities

```text
User
Meeting
MeetingRoom
Calendar
MeetingScheduler
NotificationService
SchedulerException
```

---

# 3. Class Responsibilities

## User

Represents a user in the system.

```java
public record User(
    int userId,
    String name
) {}
```

Responsibilities:

* Store user identity.
* Store user information.

A user's calendar is currently outside the scope.

---

## Meeting

Represents a scheduled meeting.

```text
Meeting
 ├── meetingId
 ├── title
 ├── description
 ├── meetingRoomId
 ├── startTime
 ├── endTime
 ├── organizer
 └── participants
```

`LocalDateTime` is used instead of an integer/HHMM representation so that date and time are represented safely.

Example:

```java
LocalDateTime startTime;
LocalDateTime endTime;
```

A meeting is identified by its `meetingId`.

---

## MeetingRoom

Represents a meeting room.

```text
MeetingRoom
 ├── meetingRoomId
 ├── name
 └── Calendar
```

The room owns a `Calendar`.

It delegates calendar-related operations such as:

* Add meeting
* Remove meeting
* Check whether a meeting exists
* Check whether a time slot is available

---

## Calendar

The calendar maintains meetings belonging to a resource, currently a meeting room.

```text
Calendar
    |
    +---- Meeting
    +---- Meeting
    +---- Meeting
```

Responsibilities:

* Add a meeting.
* Remove a meeting.
* Check whether a meeting exists.
* Check whether a time slot is available.

The important business rule is implemented here:

```java
scheduled.startTime().isBefore(meeting.endTime())
        && meeting.startTime().isBefore(scheduled.endTime())
```

This determines whether two meeting intervals overlap.

---

# 4. Time Interval Rule

Meetings are treated as half-open intervals:

```text
[startTime, endTime)
```

Therefore:

```text
Meeting A: 09:00 ───── 10:00
Meeting B:              10:00 ───── 11:00
```

These meetings **do not overlap**.

However:

```text
Meeting A: 09:00 ───── 10:00
Meeting B:        09:30 ───── 10:30
```

These meetings overlap and cannot both be scheduled in the same room.

### Overlap condition

Two meetings overlap when:

```java
existing.startTime().isBefore(new.endTime())
&& new.startTime().isBefore(existing.endTime())
```

Equivalent mathematical representation:

```text
existing.start < new.end
AND
new.start < existing.end
```

---

# 5. MeetingScheduler

`MeetingScheduler` is the main service/orchestrator.

It coordinates:

```text
Validation
    ↓
Room lookup
    ↓
Availability check
    ↓
Calendar modification
    ↓
Notification
```

### Schedule

```java
scheduleMeet(Meeting meeting)
```

Flow:

```text
Validate meeting
       ↓
Find meeting room
       ↓
Lock room
       ↓
Check room availability
       ↓
Add meeting
       ↓
Unlock room
       ↓
Notify participants
```

---

### Cancel

```java
cancelMeet(Meeting meeting)
```

Flow:

```text
Validate meeting
       ↓
Find meeting room
       ↓
Check meeting exists
       ↓
Lock room
       ↓
Remove meeting
       ↓
Unlock room
       ↓
Notify participants
```

---

### Reschedule

```java
reScheduleMeet(Meeting old, Meeting latest)
```

Flow:

```text
Validate old meeting
       ↓
Validate new meeting
       ↓
Find old room
       ↓
Verify old meeting exists
       ↓
Find new room
       ↓
Check new room availability
       ↓
Remove old meeting
       ↓
Add new meeting
       ↓
Notify participants
```

The important point is that the new slot is validated **before modifying the existing meeting**.

---

# 6. NotificationService

Notification logic is separated from scheduling logic.

```java
notificationService.notifyParticipants(
    meeting,
    "Meeting is Scheduled"
);
```

This prevents `MeetingScheduler` from being responsible for the actual notification mechanism.

Currently the implementation prints a message to the console.

This can later be extended to:

```text
Email
SMS
Push Notification
Slack
Teams
```

without changing the core scheduling logic.

---

# 7. Exception Handling

A custom:

```java
SchedulerException
```

is used for scheduling-related failures.

Examples:

```text
Invalid Meeting
Meeting room is invalid
Meeting room is not Available
Meeting is not present in the mentioned Meeting room
```

This keeps domain-specific failures separate from generic exceptions.

---

# 8. Concurrency

Meeting-room booking needs to protect the following operation:

```text
Check availability
       +
Add meeting
```

These two operations must behave atomically.

Otherwise, two concurrent requests could do:

```text
Thread A                  Thread B

check room
available
                          check room
                          available

add meeting
                          add meeting
```

resulting in two overlapping meetings.

The implementation therefore uses room-level synchronization so that availability checking and booking are performed safely.

The same principle applies to cancellation/rescheduling operations that modify a room's calendar.

---

# 9. Why Calendar Owns Availability Logic

The scheduler should not contain logic such as:

```java
for (Meeting meeting : roomMeetings) {
    // check overlap
}
```

Instead:

```text
MeetingScheduler
       |
       | asks
       ↓
MeetingRoom
       |
       ↓
Calendar
       |
       ↓
isSlotAvailable()
```

This follows the **Single Responsibility Principle**.

`MeetingScheduler` coordinates the operation.

`Calendar` knows how meetings occupy time slots.

---

# 10. Why MeetingRoom Owns a Calendar

A meeting room has its own schedule.

Therefore:

```text
MeetingRoom
      |
      └── Calendar
             |
             ├── Meeting A
             ├── Meeting B
             └── Meeting C
```

This makes the model natural:

> A room owns the calendar representing its bookings.

If another schedulable resource is introduced in the future, the same calendar concept can potentially be reused.

---

# 11. SOLID Principles

## Single Responsibility Principle

Responsibilities are separated:

```text
Meeting             → Meeting data
User                → User data
Calendar            → Time-slot management
MeetingRoom         → Room + calendar
MeetingScheduler    → Scheduling orchestration
NotificationService → Notifications
```

---

## Open/Closed Principle

The notification mechanism can be extended without modifying scheduling logic.

For example:

```text
Current:
NotificationService
       ↓
Console notification

Future:
EmailNotificationService
SmsNotificationService
PushNotificationService
```

If the requirement grows, `NotificationService` can be represented as an interface.

---

## Dependency Inversion

`MeetingScheduler` receives its dependencies through the constructor:

```java
MeetingScheduler(
    Map<Integer, MeetingRoom> meetingRooms,
    NotificationService notificationService
)
```

This avoids creating these dependencies inside the scheduler itself.

---

# 12. Important Design Decisions

### Why `LocalDateTime`?

Instead of:

```java
int startTime = 930;
```

the design uses:

```java
LocalDateTime
```

because it represents both date and time safely and avoids manual HHMM validation.

---

### Why use `Map<Integer, MeetingRoom>`?

Room lookup is based on:

```text
meetingRoomId
```

A map provides efficient lookup:

```text
O(1) average lookup
```

instead of searching through every room.

---

### Why use `meetingId` for meeting identity?

Meeting equality should be based on the identity of the meeting rather than all of its mutable/business properties.

Therefore, checking whether a meeting exists is based on:

```java
meeting.meetingId()
```

rather than complete object equality.

This is especially important during cancellation and rescheduling.

---

# 13. Complexity

Let:

```text
R = number of meeting rooms
M = number of meetings in a room
```

### Find Room

Using:

```java
Map<Integer, MeetingRoom>
```

Average:

```text
O(1)
```

### Check Meeting Exists

The current calendar uses a list:

```text
O(M)
```

### Check Slot Availability

The calendar scans existing meetings:

```text
O(M)
```

### Add Meeting

Using `ArrayList`:

```text
O(1) amortized
```

### Remove Meeting

Using list removal:

```text
O(M)
```

For an interview-scale implementation, this is acceptable.

For a production-scale system, availability could be optimized using sorted intervals, balanced trees, or a database-backed scheduling mechanism.

---

# 14. Important Edge Cases

The design should handle:

### Invalid meeting

```text
start >= end
```

Rejected.

---

### Overlapping meetings

```text
09:00 - 10:00
09:30 - 10:30
```

Rejected.

---

### Adjacent meetings

```text
09:00 - 10:00
10:00 - 11:00
```

Allowed.

---

### Same meeting ID

A meeting ID represents the identity of the meeting.

---

### Invalid room

Attempting to schedule a meeting in a room that doesn't exist is rejected.

---

### Cancel non-existing meeting

Rejected with a `SchedulerException`.

---

### Reschedule to unavailable room/time

The original meeting should not be removed unless the new scheduling operation has passed validation.

---

# 15. Future Extensions

Possible future requirements:

### User Calendar

```text
User
  |
  └── Calendar
```

Check organizer and participant availability before scheduling.

---

### Recurring Meetings

Support:

```text
Daily
Weekly
Monthly
```

---

### Multiple Notification Channels

```text
Email
SMS
Push
Slack
Teams
```

---

### Meeting Status

```text
SCHEDULED
CANCELLED
COMPLETED
```

---

### Meeting Room Capacity

```text
Room
 ├── capacity
 └── calendar
```

Validate:

```text
participants <= room.capacity
```

---

### Virtual Meetings

Add:

```text
Meeting
 ├── Physical Room
 └── Video Conference Link
```

---

### Persistence

Replace the in-memory:

```java
Map<Integer, MeetingRoom>
```

with a repository/database layer.

---

# 16. Interview Discussion Points

If asked to extend the design, discuss these in order:

### "What if participants also have conflicts?"

Add a calendar to `User` and check:

```text
Room availability
+
Organizer availability
+
Participant availability
```

---

### "What if two users book the same room simultaneously?"

Availability check + booking must be atomic.

Use:

```text
Room-level locking
```

or, in a distributed/persistent system:

```text
Database transaction
Distributed lock
Optimistic locking
```

depending on the architecture.

---

### "What if notification fails?"

Scheduling and notification should ideally be decoupled.

For a production system:

```text
Schedule meeting
      ↓
Persist meeting
      ↓
Publish event
      ↓
Notification consumer
```

This prevents notification failure from rolling back the booking unnecessarily.

---

### "What if we have millions of meetings?"

The current in-memory list scan:

```text
O(M)
```

may become expensive.

Possible improvements:

```text
Sorted intervals
Interval tree
Database indexes
Time-based partitioning
Caching
```

---

# 17. Design Summary

```text
                    MeetingScheduler
                    /       |       \
                   /        |        \
                  ↓         ↓         ↓
          MeetingRoom    Meeting   NotificationService
               |
               ↓
            Calendar
               |
               ↓
            Meetings
```

### Core principle

> **MeetingScheduler coordinates the workflow, while Calendar owns time-slot availability.**

This keeps the design simple, extensible, and suitable for an LLD interview without introducing unnecessary abstractions.

---

## 18. Quick Interview Revision

Before the interview, remember these five points:

```text
1. MeetingScheduler = orchestration

2. MeetingRoom owns Calendar

3. Calendar = availability + meeting management

4. Overlap:
   existing.start < new.end
   &&
   new.start < existing.end

5. Check availability + modify calendar atomically
   when concurrency matters
```

The key trade-off in this implementation is intentionally keeping **participant availability outside the current scope** while leaving a clear path to add User Calendars later.
