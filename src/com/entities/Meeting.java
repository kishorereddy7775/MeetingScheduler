package com.entities;

import java.time.LocalDateTime;
import java.util.List;

public record Meeting(int meetingId, String title, String Description, int meetingRoomId, LocalDateTime startTime,
					  LocalDateTime endTime, User organizer, List<User> participants) {
}

