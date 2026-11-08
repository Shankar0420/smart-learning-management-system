import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import {defineConfig} from 'vite';

export default defineConfig(() => {
  const rootDir = __dirname;
  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        '@': path.resolve(rootDir, '.'),
      },
    },
    build: {
      rollupOptions: {
        input: {
          main: path.resolve(rootDir, 'index.html'),
          login: path.resolve(rootDir, 'pages/login.html'),
          register: path.resolve(rootDir, 'pages/register.html'),
          registerTeacher: path.resolve(rootDir, 'pages/register-teacher.html'),
          registerAdmin: path.resolve(rootDir, 'pages/register-admin.html'),
          forgotPassword: path.resolve(rootDir, 'pages/forgot-password.html'),
          resetPassword: path.resolve(rootDir, 'pages/reset-password.html'),

          studentDashboard: path.resolve(rootDir, 'student/dashboard.html'),
          studentCourses: path.resolve(rootDir, 'student/courses.html'),
          studentCourseDetails: path.resolve(rootDir, 'student/course-details.html'),
          studentLearning: path.resolve(rootDir, 'student/learning.html'),
          studentQuiz: path.resolve(rootDir, 'student/quiz.html'),
          studentAssignments: path.resolve(rootDir, 'student/assignments.html'),
          studentGrades: path.resolve(rootDir, 'student/grades.html'),
          studentProfile: path.resolve(rootDir, 'student/profile.html'),
          teacherDashboard: path.resolve(rootDir, 'teacher/dashboard.html'),
          teacherCourses: path.resolve(rootDir, 'teacher/courses.html'),
          teacherCourseForm: path.resolve(rootDir, 'teacher/course-form.html'),
          teacherCourseDetails: path.resolve(rootDir, 'teacher/course-details.html'),
          teacherQuizManagement: path.resolve(rootDir, 'teacher/quiz-management.html'),
          teacherAssignmentManagement: path.resolve(rootDir, 'teacher/assignment-management.html'),
          teacherSubmissions: path.resolve(rootDir, 'teacher/submissions.html'),
          teacherProfile: path.resolve(rootDir, 'teacher/profile.html'),
          adminDashboard: path.resolve(rootDir, 'admin/dashboard.html'),
          adminUsers: path.resolve(rootDir, 'admin/users.html'),
          adminCourses: path.resolve(rootDir, 'admin/courses.html'),
          adminProfile: path.resolve(rootDir, 'admin/profile.html'),
        },
      },
    },
    server: {
      port: 5500,
      host: '0.0.0.0',
      hmr: process.env.DISABLE_HMR !== 'true',
      watch: process.env.DISABLE_HMR === 'true' ? null : {},
    },
  };
});
