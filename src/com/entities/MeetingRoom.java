package com.entities;

public record MeetingRoom(int meetingRoomId, String name, Calendar calendar) {

	public boolean isMeetingAvailable(Meeting meeting) {
		return calendar.isMeetingAvailable(meeting);
	}
	
	public void addMeeting(Meeting meeting) {
		calendar.addMeeting(meeting);
	}
	
	public void removeMeeting(Meeting meeting) {
		calendar.removeMeeting(meeting);
	}
	
	public boolean isSlotAvaliable(Meeting meeting) {
		return calendar.isSlotAvailable(meeting);
	}
}
