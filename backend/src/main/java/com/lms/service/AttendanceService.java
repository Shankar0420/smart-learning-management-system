package com.lms.service;

import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.exception.BadRequestException;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditLogService auditLogService;

    public AttendanceService(AttendanceSessionRepository sessionRepository,
                             AttendanceRecordRepository recordRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository,
                             EnrollmentRepository enrollmentRepository,
                             AuditLogService auditLogService) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AttendanceSessionResponse createSession(Long courseId, AttendanceSessionRequest request, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));

        if (!course.getTeacher().getId().equals(teacher.getId()) && teacher.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You are not authorized to mark attendance for this course");
        }

        if (sessionRepository.findByCourseIdAndSessionDate(courseId, request.getSessionDate()).isPresent()) {
            throw new BadRequestException("An attendance session already exists for this course on date " + request.getSessionDate());
        }

        AttendanceSession session = new AttendanceSession(course, teacher, request.getSessionDate(), request.getTopic());
        session = sessionRepository.save(session);

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<AttendanceRecord> records = new ArrayList<>();

        if (request.getRecords() != null && !request.getRecords().isEmpty()) {
            for (AttendanceRecordRequest recReq : request.getRecords()) {
                User student = userRepository.findById(recReq.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + recReq.getStudentId()));
                records.add(new AttendanceRecord(session, student, recReq.getStatus(), recReq.getRemarks()));
            }
        } else {
            // Default: All currently enrolled students marked PRESENT initially
            for (Enrollment enr : enrollments) {
                records.add(new AttendanceRecord(session, enr.getStudent(), AttendanceStatus.PRESENT, "Session Created"));
            }
        }

        recordRepository.saveAll(records);
        session.setRecords(records);

        auditLogService.logAction(teacherEmail, "ATTENDANCE_SESSION_CREATED", "AttendanceSession",
                String.valueOf(session.getId()), "Topic: " + session.getTopic() + " on " + session.getSessionDate(), null);

        return mapToSessionResponse(session, enrollments.size());
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> getCourseSessions(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        int totalEnrolled = enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);

        return sessionRepository.findByCourseIdOrderBySessionDateDesc(courseId).stream()
                .map(s -> mapToSessionResponse(s, totalEnrolled))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceSessionResponse getSessionById(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with id: " + sessionId));
        int totalEnrolled = enrollmentRepository.countByCourseIdAndStatus(session.getCourse().getId(), EnrollmentStatus.ACTIVE);
        return mapToSessionResponse(session, totalEnrolled);
    }

    @Transactional
    public AttendanceSessionResponse updateRecord(Long sessionId, Long studentId, AttendanceStatus status, String remarks, String userEmail) {
        AttendanceRecord record = recordRepository.findBySessionIdAndStudentId(sessionId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found for student in session"));

        record.setStatus(status);
        if (remarks != null) {
            record.setRemarks(remarks);
        }
        recordRepository.save(record);

        auditLogService.logAction(userEmail, "ATTENDANCE_RECORD_UPDATED", "AttendanceRecord",
                String.valueOf(record.getId()), "Student ID: " + studentId + " -> " + status, null);

        return getSessionById(sessionId);
    }

    @Transactional(readOnly = true)
    public StudentAttendanceSummaryResponse getStudentAttendanceSummary(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + studentEmail));

        long totalClasses = recordRepository.countByStudentId(student.getId());
        long presentClasses = recordRepository.countByStudentIdAndStatus(student.getId(), AttendanceStatus.PRESENT);
        long absentClasses = recordRepository.countByStudentIdAndStatus(student.getId(), AttendanceStatus.ABSENT);

        double overallPct = totalClasses > 0 ? (presentClasses * 100.0) / totalClasses : 100.0;

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        List<StudentAttendanceSummaryResponse.CourseAttendanceItem> courseItems = new ArrayList<>();

        for (Enrollment enr : enrollments) {
            Long courseId = enr.getCourse().getId();
            long cTotal = recordRepository.countByStudentIdAndCourseId(student.getId(), courseId);
            long cPresent = recordRepository.countByStudentIdAndCourseIdAndStatus(student.getId(), courseId, AttendanceStatus.PRESENT);
            double cPct = cTotal > 0 ? (cPresent * 100.0) / cTotal : 100.0;
            courseItems.add(new StudentAttendanceSummaryResponse.CourseAttendanceItem(
                    courseId,
                    enr.getCourse().getTitle(),
                    cTotal,
                    cPresent,
                    Math.round(cPct * 10.0) / 10.0
            ));
        }

        StudentAttendanceSummaryResponse summary = new StudentAttendanceSummaryResponse();
        summary.setTotalClasses(totalClasses);
        summary.setPresentClasses(presentClasses);
        summary.setAbsentClasses(absentClasses);
        summary.setOverallPercentage(Math.round(overallPct * 10.0) / 10.0);
        summary.setCourseBreakdown(courseItems);
        return summary;
    }

    private AttendanceSessionResponse mapToSessionResponse(AttendanceSession session, int totalEnrolled) {
        AttendanceSessionResponse res = new AttendanceSessionResponse();
        res.setId(session.getId());
        res.setCourseId(session.getCourse().getId());
        res.setCourseTitle(session.getCourse().getTitle());
        res.setTeacherId(session.getTeacher().getId());
        res.setTeacherName(session.getTeacher().getName());
        res.setSessionDate(session.getSessionDate());
        res.setTopic(session.getTopic());
        res.setCreatedAt(session.getCreatedAt());
        res.setTotalEnrolled(totalEnrolled);

        List<AttendanceRecord> recs = session.getRecords();
        if (recs == null || recs.isEmpty()) {
            recs = recordRepository.findBySessionId(session.getId());
        }

        int pres = 0;
        int abs = 0;
        List<AttendanceRecordResponse> recResponses = new ArrayList<>();
        for (AttendanceRecord r : recs) {
            if (r.getStatus() == AttendanceStatus.PRESENT) pres++;
            else if (r.getStatus() == AttendanceStatus.ABSENT) abs++;
            recResponses.add(new AttendanceRecordResponse(
                    r.getId(),
                    r.getStudent().getId(),
                    r.getStudent().getName(),
                    r.getStudent().getEmail(),
                    r.getStudent().getProfessionalId(),
                    r.getStatus(),
                    r.getRemarks(),
                    r.getMarkedAt()
            ));
        }
        res.setPresentCount(pres);
        res.setAbsentCount(abs);
        res.setRecords(recResponses);
        return res;
    }
}

