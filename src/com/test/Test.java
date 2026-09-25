package com.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.entities.Calendar;
import com.entities.Meeting;
import com.entities.MeetingRoom;
import com.entities.User;
import com.notify.NotificationService;
import com.scheduler.MeetingScheduler;

public class Test {

	public static void main(String[] args) {
		
		NotificationService notificationService=new NotificationService();
		
		MeetingRoom m1=new MeetingRoom(1,"Lotus",new Calendar(new ArrayList<>()));
		MeetingRoom m2=new MeetingRoom(2,"Rose",new Calendar(new ArrayList<>()));
		MeetingRoom m3=new MeetingRoom(3,"Lilly",new Calendar(new ArrayList<>()));
		
		HashMap<Integer, MeetingRoom> meetingRooms=new HashMap<>();
		meetingRooms.put(1,m1);
		meetingRooms.put(2,m2);
		meetingRooms.put(3,m3);
		MeetingScheduler scheduler = new MeetingScheduler(meetingRooms, notificationService);
		User u1=new User(1,"Rohit");
		User u2=new User(2,"Rahul");
		User u3=new User(3,"Ravi");
		
		LocalDateTime t1=LocalDateTime.now();
		LocalDateTime t2=LocalDateTime.now().plusMinutes(30);
		LocalDateTime t3=LocalDateTime.now().plusMinutes(60);
		LocalDateTime t4=LocalDateTime.now().plusMinutes(90);
		
		LocalDateTime t5=LocalDateTime.now().plusMinutes(120);
		LocalDateTime t6=LocalDateTime.now().plusMinutes(150);
		
		
		
		Meeting mt1=new Meeting(1,"Standup","Standup",1,t1,t2,u1,List.of(u2,u3));
		Meeting mt2=new Meeting(3,"Standup","Standup",1,t1,t2,u2,List.of(u1,u3));
		Meeting mt3=new Meeting(2,"Standup","Standup",4,t1,t2,u2,List.of(u1,u3));
		Meeting mt4=new Meeting(4,"Standup","Standup",1,t3,t4,u1,List.of(u2,u3));
		Meeting mt5 =new Meeting(4,"Standup","Standup",1,t5,t4,u1,List.of(u2,u3));
		Meeting mt6 =new Meeting(4,"Standup","Standup",2,t3,t4,u1,List.of(u2,u3));
		Meeting mt7=new Meeting(1,"Standup","Standup",1,t5,t6,u1,List.of(u2,u3));
		try {
			scheduler.scheduleMeet(mt1);
			scheduler.scheduleMeet(mt2);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			
			scheduler.scheduleMeet(mt3);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {	
			scheduler.scheduleMeet(mt4);
			scheduler.scheduleMeet(mt5);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			scheduler.cancelMeet(mt6);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			scheduler.reScheduleMeet(mt1,mt7);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
