package com.entities;

import java.util.List;

public record Calendar(List<Meeting> meetings) {
	
	public void addMeeting(Meeting meeting) {
		meetings.add(meeting);
	}
	
	public void removeMeeting(Meeting meeting) {
		meetings.remove(meeting);
	}
	public boolean isMeetingAvailable(Meeting meeting) {
		return meetings.stream().anyMatch(meet->meet.meetingId()==meeting.meetingId());
	}
	
	public boolean isSlotAvailable(Meeting meeting) {
		for(Meeting scheduled:meetings) {
			if(scheduled.startTime().isBefore(meeting.endTime()) && meeting.startTime().isBefore(scheduled.endTime()))
				return false;
		}
		return true;
	}
	
}
