package com.entities;

import java.util.List;

public record Meeting(int meetingId, String title, String Description, int meetingRoomId, int startTime,
					  int endTime, User organizer, List<User> participants) {
	//Assuming time is 24hr format and HHMM
}

