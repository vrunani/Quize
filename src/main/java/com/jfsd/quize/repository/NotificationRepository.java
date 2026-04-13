package com.jfsd.quize.repository;

import com.jfsd.quize.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // All notifications for a user (ordered newest first)
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Only unread notifications
    List<Notification> findByUserIdAndIsRead(String userId, Boolean isRead);
}