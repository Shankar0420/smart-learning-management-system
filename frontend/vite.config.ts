import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import {defineConfig} from 'vite';

export default defineConfig(() => {
  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, '.'),
      },
    },
    build: {
      rollupOptions: {
        input: {
          main: path.resolve(__dirname, 'index.html'),
          login: path.resolve(__dirname, 'pages/login.html'),
          register: path.resolve(__dirname, 'pages/register.html'),
          studentDashboard: path.resolve(__dirname, 'student/dashboard.html'),
          studentCourses: path.resolve(__dirname, 'student/courses.html'),
          studentCourseDetails: path.resolve(__dirname, 'student/course-details.html'),
          studentLearning: path.resolve(__dirname, 'student/learning.html'),
          studentQuiz: path.resolve(__dirname, 'student/quiz.html'),
          studentAssignments: path.resolve(__dirname, 'student/assignments.html'),
          studentGrades: path.resolve(__dirname, 'student/grades.html'),
          studentProfile: path.resolve(__dirname, 'student/profile.html'),
          teacherDashboard: path.resolve(__dirname, 'teacher/dashboard.html'),
          teacherCourses: path.resolve(__dirname, 'teacher/courses.html'),
          teacherCourseForm: path.resolve(__dirname, 'teacher/course-form.html'),
          teacherCourseDetails: path.resolve(__dirname, 'teacher/course-details.html'),
          teacherQuizManagement: path.resolve(__dirname, 'teacher/quiz-management.html'),
          teacherAssignmentManagement: path.resolve(__dirname, 'teacher/assignment-management.html'),
          teacherSubmissions: path.resolve(__dirname, 'teacher/submissions.html'),
          teacherProfile: path.resolve(__dirname, 'teacher/profile.html'),
          adminDashboard: path.resolve(__dirname, 'admin/dashboard.html'),
          adminUsers: path.resolve(__dirname, 'admin/users.html'),
          adminCourses: path.resolve(__dirname, 'admin/courses.html'),
          adminProfile: path.resolve(__dirname, 'admin/profile.html'),
        },
      },
    },
    server: {
      // HMR is disabled in AI Studio via DISABLE_HMR env var.
      // Do not modify—file watching is disabled to prevent flickering during agent edits.
      hmr: process.env.DISABLE_HMR !== 'true',
      // Disable file watching when DISABLE_HMR is true to save CPU during agent edits.
      watch: process.env.DISABLE_HMR === 'true' ? null : {},
    },
  };
});
