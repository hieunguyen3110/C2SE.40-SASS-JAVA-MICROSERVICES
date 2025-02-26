package com.capstone1.sasscapstone1.event;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationEvent {
    @MessageMapping("/notify-with-upload")
    @SendTo("/queue/notifications-with-upload")
    public String sendNotificationWithTypeUploadFile(@Payload String message) {
        System.out.println(message);
        return message;
    }

    @MessageMapping("/notify-with-follow")
    @SendTo("/queue/notifications-with-follow")
    public String sendNotificationWithTypeFollow(@Payload String message) {
        System.out.println(message);
        return message;
    }
}
