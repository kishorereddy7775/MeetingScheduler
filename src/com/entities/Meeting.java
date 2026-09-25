package com.entities;

import java.time.LocalDateTime;
import java.util.List;

public record Meeting(int meetingId, String title, String description, int meetingRoomId, LocalDateTime startTime,
					  LocalDateTime endTime, User organizer, List<User> participants) {
}

