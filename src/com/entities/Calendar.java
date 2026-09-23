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
		return meetings.stream().anyMatch(meet->meet.equals(meeting));
	}
	
	public boolean isSlotAvaliable(Meeting meeting) {
		for(Meeting scheduled:meetings) {
			if(!(scheduled.endTime()<meeting.startTime() || scheduled.startTime()>meeting.endTime()))
				return false;
		}
		return true;
	}
	
}
