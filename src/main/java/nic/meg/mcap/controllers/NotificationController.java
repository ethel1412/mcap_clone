package nic.meg.mcap.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nic.meg.mcap.dto.response.ScheduleNotificationResponseDTO;
import nic.meg.mcap.services.ScheduleService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

	@Autowired
	private ScheduleService scheduleService;

	/**
	 * Get all notifications for institute
	 */
	@GetMapping
	public ResponseEntity<List<ScheduleNotificationResponseDTO>> getAllNotifications() {
		List<ScheduleNotificationResponseDTO> notifications = scheduleService.getInstituteScheduleNotifications();
		return ResponseEntity.ok(notifications);
	}

	/**
	 * Get notification count only (for badges)
	 */
	@GetMapping("/count")
	public ResponseEntity<Integer> getNotificationCount() {
		List<ScheduleNotificationResponseDTO> notifications = scheduleService.getInstituteScheduleNotifications();

		return ResponseEntity.ok(notifications.size());
	}

	@GetMapping("/count-new")
	public ResponseEntity<Integer> getNewNotificationCount() {
		try {
			List<ScheduleNotificationResponseDTO> notifications = scheduleService.getInstituteScheduleNotifications();

			return ResponseEntity.ok(notifications.size());

		} catch (DataAccessException e) {
			return ResponseEntity.ok(0); // explicit fallback decision
		}
	}

}
