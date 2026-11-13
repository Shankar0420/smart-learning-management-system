package com.lms.service;

import com.lms.dto.CalendarEventRequest;
import com.lms.dto.CalendarEventResponse;
import com.lms.entity.AcademicCalendarEvent;
import com.lms.entity.Course;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.AcademicCalendarRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AcademicCalendarService {

    private final AcademicCalendarRepository calendarRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AcademicCalendarService(AcademicCalendarRepository calendarRepository,
                                   CourseRepository courseRepository,
                                   UserRepository userRepository,
                                   EnrollmentRepository enrollmentRepository) {
        this.calendarRepository = calendarRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<CalendarEventResponse> getEventsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<Long> enrolledCourseIds = new ArrayList<>();
        if (user.getRole() == Role.STUDENT) {
            enrolledCourseIds = enrollmentRepository.findByStudentId(user.getId()).stream()
                    .map(e -> e.getCourse().getId())
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.TEACHER) {
            enrolledCourseIds = courseRepository.findByTeacherId(user.getId()).stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
        } else {
            return calendarRepository.findByOrderByStartDateAsc().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if (enrolledCourseIds.isEmpty()) {
            return calendarRepository.findByCourseIdOrderByStartDateAsc(null).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return calendarRepository.findRelevantEvents(enrolledCourseIds).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CalendarEventResponse createEvent(CalendarEventRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.getCourseId()));

            if (user.getRole() != Role.ADMIN && !course.getTeacher().getId().equals(user.getId())) {
                throw new ForbiddenException("Only course faculty or admin can create course events");
            }
        } else {
            if (user.getRole() != Role.ADMIN) {
                throw new ForbiddenException("Only administrators can create university-wide events");
            }
        }

        AcademicCalendarEvent event = new AcademicCalendarEvent();
        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType().toUpperCase());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setCourse(course);
        event.setCreatedBy(user);
        event.setTargetRole(request.getTargetRole() != null ? request.getTargetRole().toUpperCase() : "ALL");

        AcademicCalendarEvent saved = calendarRepository.save(event);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteEvent(Long id, String userEmail) {
        AcademicCalendarEvent event = calendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (user.getRole() != Role.ADMIN && !event.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to delete this event");
        }

        calendarRepository.delete(event);
    }

    private CalendarEventResponse mapToResponse(AcademicCalendarEvent e) {
        CalendarEventResponse res = new CalendarEventResponse();
        res.setId(e.getId());
        res.setTitle(e.getTitle());
        res.setDescription(e.getDescription());
        res.setEventType(e.getEventType());
        res.setStartDate(e.getStartDate());
        res.setEndDate(e.getEndDate());
        if (e.getCourse() != null) {
            res.setCourseId(e.getCourse().getId());
            res.setCourseTitle(e.getCourse().getTitle());
        }
        res.setCreatedByName(e.getCreatedBy() != null ? e.getCreatedBy().getName() : "Galgotias Admin");
        res.setTargetRole(e.getTargetRole());
        res.setCreatedAt(e.getCreatedAt());
        return res;
    }
}

