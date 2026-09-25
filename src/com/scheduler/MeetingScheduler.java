package com.scheduler;

import java.util.Map;

import com.entities.Meeting;
import com.entities.MeetingRoom;
import com.exception.SchedulerException;
import com.notify.NotificationService;

public class MeetingScheduler {
	
	//DB to store Meeting rooms
	private Map<Integer,MeetingRoom> meetingRooms;
	private NotificationService notificationService;
	
	public MeetingScheduler(Map<Integer,MeetingRoom> meetingRooms,NotificationService notificationService) {
		this.meetingRooms=meetingRooms;
		this.notificationService=notificationService;
	}

	public void scheduleMeet(Meeting meeting) throws Exception {
		validateMeeting(meeting);
		MeetingRoom meetingRoom = validateAndGetMeetingRoom(meeting);
		synchronized (meetingRoom) {
			checkSlotForMeeting(meeting,meetingRoom);
			meetingRoom.addMeeting(meeting);
		}
		notificationService.notifyParticipants(meeting, "Meeting is Scheduled");
	}
	
	public void cancelMeet(Meeting meeting) throws Exception {
		validateMeeting(meeting);
		MeetingRoom meetingRoom = validateAndGetMeetingRoom(meeting);
		synchronized (meetingRoom) {
			validateMeetingInMeetingRoom(meeting,meetingRoom);
			meetingRoom.removeMeeting(meeting);
		}
		notificationService.notifyParticipants(meeting, "Meeting is Canceled");
	}
	
	public void reScheduleMeet(Meeting old, Meeting latest) throws Exception{
		validateMeeting(old);
		validateMeeting(latest);
		MeetingRoom oldMeetingRoom=validateAndGetMeetingRoom(old);
		validateMeetingInMeetingRoom(old, oldMeetingRoom);
		MeetingRoom latestMeetingRoom=validateAndGetMeetingRoom(latest);
		synchronized (oldMeetingRoom) {
			synchronized (latestMeetingRoom) {
				checkSlotForMeeting(latest, latestMeetingRoom);
				oldMeetingRoom.removeMeeting(old);
				latestMeetingRoom.addMeeting(latest);
			}
		}
		notificationService.notifyParticipants(old, "Meeting is rescheduled");
	}
	
	private MeetingRoom validateAndGetMeetingRoom(Meeting meeting) throws Exception {
		if(!meetingRooms.containsKey(meeting.meetingRoomId())){
			throw new SchedulerException("Meeting room is invalid");
		}
		return meetingRooms.get(meeting.meetingRoomId());
	}
	private void checkSlotForMeeting(Meeting meeting, MeetingRoom meetingRoom) throws Exception{
		if(!meetingRoom.isSlotAvailable(meeting)) {
			throw new SchedulerException("Meeting room is not Available");
		}
	}
	private void validateMeetingInMeetingRoom(Meeting meeting, MeetingRoom meetingRoom) throws Exception {
		if(!meetingRoom.isMeetingAvailable(meeting)) {
			throw new SchedulerException("Meeting is not present in the mentioned Meeting room to cancel");
		}
	}
	private void validateMeeting(Meeting meeting) throws Exception{
		if(!meeting.startTime().isBefore(meeting.endTime())) {
			throw new SchedulerException("Invalid Meeting");
		}
	}
	
}


