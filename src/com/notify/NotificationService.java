package com.notify;

import com.entities.Meeting;
import com.entities.User;

public class NotificationService {
	
	public void notifyParticipants(Meeting meeting, String message) {
		for(User participant:meeting.participants()) {
			notify(participant,message);
		}
	}
	private void notify(User user, String message) {
		System.out.println("Hi "+user.name()+", "+message);
	}
}
