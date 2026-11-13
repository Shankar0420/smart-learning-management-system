package com.lms.service;

import com.lms.dto.DiscussionReplyRequest;
import com.lms.dto.DiscussionReplyResponse;
import com.lms.dto.DiscussionThreadRequest;
import com.lms.dto.DiscussionThreadResponse;
import com.lms.entity.Course;
import com.lms.entity.DiscussionReply;
import com.lms.entity.DiscussionThread;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.ForbiddenException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.DiscussionReplyRepository;
import com.lms.repository.DiscussionThreadRepository;
import com.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DiscussionService {

    private final DiscussionThreadRepository threadRepository;
    private final DiscussionReplyRepository replyRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService;

    public DiscussionService(DiscussionThreadRepository threadRepository,
                             DiscussionReplyRepository replyRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository,
                             EnrollmentService enrollmentService,
                             NotificationService notificationService) {
        this.threadRepository = threadRepository;
        this.replyRepository = replyRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.notificationService = notificationService;
    }

    public List<DiscussionThreadResponse> getCourseThreads(Long courseId, String userEmail) {
        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(courseId, userEmail);

        return threadRepository.findByCourseIdOrderByPinnedDescCreatedAtDesc(courseId).stream()
                .map(this::mapThreadSummary)
                .collect(Collectors.toList());
    }

    public DiscussionThreadResponse getThreadDetails(Long threadId, String userEmail) {
        DiscussionThread thread = threadRepository.findWithRepliesById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion thread not found with id: " + threadId));

        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(thread.getCourse().getId(), userEmail);
        return mapThreadWithReplies(thread);
    }

    @Transactional
    public DiscussionThreadResponse createThread(Long courseId, DiscussionThreadRequest request, String userEmail) {
        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(courseId, userEmail);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        DiscussionThread thread = new DiscussionThread();
        thread.setCourse(course);
        thread.setAuthor(author);
        thread.setTitle(request.getTitle().trim());
        thread.setContent(request.getContent().trim());

        DiscussionThread saved = threadRepository.save(thread);
        return mapThreadSummary(saved);
    }

    @Transactional
    public DiscussionReplyResponse addReply(Long threadId, DiscussionReplyRequest request, String userEmail) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));

        enrollmentService.checkStudentEnrollmentOrTeacherAdmin(thread.getCourse().getId(), userEmail);

        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        DiscussionReply reply = new DiscussionReply();
        reply.setThread(thread);
        reply.setAuthor(author);
        reply.setContent(request.getContent().trim());

        DiscussionReply saved = replyRepository.save(reply);

        // Notify thread author if someone else replies
        if (!thread.getAuthor().getId().equals(author.getId())) {
            notificationService.sendNotification(
                    thread.getAuthor(),
                    "New Reply to Your Discussion",
                    author.getName() + " replied to '" + thread.getTitle() + "'",
                    "NOTICE",
                    "/student/learning.html?courseId=" + thread.getCourse().getId()
            );
        }

        return mapReply(saved);
    }

    @Transactional
    public void togglePin(Long threadId, String userEmail) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (user.getRole() != Role.ADMIN && !thread.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Only faculty or administrators can pin threads");
        }

        thread.setPinned(!thread.isPinned());
        threadRepository.save(thread);
    }

    @Transactional
    public void markAcceptedSolution(Long replyId, String userEmail) {
        DiscussionReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("Reply not found with id: " + replyId));

        DiscussionThread thread = reply.getThread();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        boolean isThreadAuthor = thread.getAuthor().getId().equals(user.getId());
        boolean isTeacher = thread.getCourse().getTeacher().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isThreadAuthor && !isTeacher && !isAdmin) {
            throw new ForbiddenException("Not authorized to mark accepted solution");
        }

        reply.setAcceptedSolution(!reply.isAcceptedSolution());
        replyRepository.save(reply);
    }

    @Transactional
    public void deleteThread(Long threadId, String userEmail) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (user.getRole() != Role.ADMIN && !thread.getAuthor().getId().equals(user.getId()) && !thread.getCourse().getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to delete this thread");
        }

        threadRepository.delete(thread);
    }

    private DiscussionThreadResponse mapThreadSummary(DiscussionThread t) {
        DiscussionThreadResponse res = new DiscussionThreadResponse();
        res.setId(t.getId());
        res.setCourseId(t.getCourse().getId());
        res.setCourseTitle(t.getCourse().getTitle());
        res.setAuthorId(t.getAuthor().getId());
        res.setAuthorName(t.getAuthor().getName());
        res.setAuthorRole(t.getAuthor().getRole().name());
        res.setTitle(t.getTitle());
        res.setContent(t.getContent());
        res.setPinned(t.isPinned());
        res.setReplyCount(t.getReplies() != null ? t.getReplies().size() : 0);
        res.setCreatedAt(t.getCreatedAt());
        res.setUpdatedAt(t.getUpdatedAt());
        return res;
    }

    private DiscussionThreadResponse mapThreadWithReplies(DiscussionThread t) {
        DiscussionThreadResponse res = mapThreadSummary(t);
        if (t.getReplies() != null) {
            res.setReplies(t.getReplies().stream().map(this::mapReply).collect(Collectors.toList()));
            res.setReplyCount(t.getReplies().size());
        }
        return res;
    }

    private DiscussionReplyResponse mapReply(DiscussionReply r) {
        DiscussionReplyResponse res = new DiscussionReplyResponse();
        res.setId(r.getId());
        res.setAuthorId(r.getAuthor().getId());
        res.setAuthorName(r.getAuthor().getName());
        res.setAuthorRole(r.getAuthor().getRole().name());
        res.setContent(r.getContent());
        res.setAcceptedSolution(r.isAcceptedSolution());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }
}

