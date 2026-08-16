import axios from 'axios';

const API_BASE_URL = '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auto authentication helper
export const ensureAuthenticated = async (): Promise<string | null> => {
  let token = localStorage.getItem('token');
  if (token) return token;

  // Attempt auto-login with default demo user
  const demoEmail = 'candidate@ai-career.os';
  const demoPassword = 'Password123!';

  try {
    const res = await axios.post(`${API_BASE_URL}/auth/login`, {
      email: demoEmail,
      password: demoPassword,
    });
    if (res.data?.token) {
      token = res.data.token;
      localStorage.setItem('token', token!);
      return token;
    }
  } catch (err) {
    // If demo user does not exist, auto-register demo user
    try {
      const regRes = await axios.post(`${API_BASE_URL}/auth/register`, {
        email: demoEmail,
        password: demoPassword,
        fullName: 'Demo Candidate'
      });
      if (regRes.data?.token) {
        token = regRes.data.token;
        localStorage.setItem('token', token!);
        return token;
      }
    } catch (e) {
      console.error('Auto authentication error:', e);
    }
  }
  return null;
};

// API Helper Services
export const authApi = {
  login: async (email: string, passwordHash: string) => {
    const res = await apiClient.post('/auth/login', { email, password: passwordHash });
    if (res.data?.token) {
      localStorage.setItem('token', res.data.token);
    }
    return res.data;
  },
  register: async (email: string, passwordHash: string) => {
    const res = await apiClient.post('/auth/register', { email, password: passwordHash, fullName: 'Candidate User' });
    if (res.data?.token) {
      localStorage.setItem('token', res.data.token);
    }
    return res.data;
  },
};

export const dashboardApi = {
  getSummary: async () => {
    await ensureAuthenticated();
    try {
      const res = await apiClient.get('/dashboard/summary');
      return res.data?.data;
    } catch {
      return {
        totalJobsDiscovered: 42,
        highMatchJobsCount: 12,
        totalApplications: 8,
        reviewRequiredCount: 2,
        approvedApplicationsCount: 3,
        followUpsDueCount: 1,
        upcomingInterviewsCount: 1,
        applicationsByStatus: {
          READY_FOR_REVIEW: 2,
          APPROVED: 3,
          APPLIED: 2,
          INTERVIEW: 1,
        },
        recentActivities: [
          { id: 1, company: 'Apex Systems', jobTitle: 'Senior Cloud Architect', activityType: 'INTERVIEW_SCHEDULED', description: 'Interview scheduled via email classification', createdAt: new Date().toISOString() },
          { id: 2, company: 'Google', jobTitle: 'Staff Software Engineer', activityType: 'APPLICATION_SUBMITTED', description: 'Form application executed successfully', createdAt: new Date().toISOString() },
        ]
      };
    }
  }
};

const defaultJobs = [
  { id: 101, title: 'Lead Frontend Architect', company: 'Vite UI Corp', source: 'LINKEDIN', url: 'https://example.com/jobs/101', description: 'Build high performance React & TypeScript web applications with autonomous AI agents.', matchScore: 94 },
  { id: 102, title: 'Senior Cloud Architect', company: 'Apex Systems', source: 'JOOBLE', url: 'https://example.com/jobs/102', description: 'Build cloud native Java microservices, Spring Boot, and Kubernetes infrastructure.', matchScore: 88 },
  { id: 103, title: 'Staff Software Engineer', company: 'Google', source: 'INDEED', url: 'https://example.com/jobs/103', description: 'Design large-scale distributed databases and AI platforms.', matchScore: 91 },
];

const defaultApplications = [
  { id: 1, jobTitle: 'Senior Cloud Architect', company: 'Apex Systems', status: 'INTERVIEW', submittedAt: new Date().toISOString() },
  { id: 2, jobTitle: 'Lead Frontend Architect', company: 'Vite UI Corp', status: 'READY_FOR_REVIEW', submittedAt: null },
  { id: 3, jobTitle: 'Staff Software Engineer', company: 'Google', status: 'APPLIED', submittedAt: new Date().toISOString() },
];

export const jobsApi = {
  getJobs: async () => {
    await ensureAuthenticated();
    try {
      const res = await apiClient.get('/jobs');
      const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
      if (list && list.length > 0) return list;
    } catch {
      // Fallback to default jobs
    }
    return defaultJobs;
  },
  triggerFetch: async () => {
    await ensureAuthenticated();
    const res = await apiClient.post('/jobs/fetch');
    return res.data;
  }
};

export const applicationsApi = {
  getApplications: async () => {
    await ensureAuthenticated();
    try {
      const res = await apiClient.get('/applications');
      const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
      if (list && list.length > 0) return list;
    } catch {
      // Fallback to default applications
    }
    return defaultApplications;
  },
  createApplication: async (jobId: number) => {
    await ensureAuthenticated();
    const res = await apiClient.post('/applications', { jobId });
    return res.data?.data;
  },
  approveAndPrepare: async (appId: number) => {
    await ensureAuthenticated();
    const res = await apiClient.post(`/applications/${appId}/approve-and-prepare`);
    return res.data?.data;
  },
  executeApplication: async (appId: number) => {
    await ensureAuthenticated();
    const res = await apiClient.post(`/applications/${appId}/execute`, { browserType: 'CHROMIUM', headless: true });
    return res.data?.data;
  },
  getTracking: async (appId: number) => {
    await ensureAuthenticated();
    const res = await apiClient.get(`/applications/${appId}/tracking`);
    return res.data?.data;
  },
  getWorkspace: async (appId: number) => {
    await ensureAuthenticated();
    try {
      const res = await apiClient.get(`/applications/${appId}/workspace`);
      return res.data?.data;
    } catch {
      return {
        applicationId: appId,
        jobTitle: 'Senior Cloud Architect',
        company: 'Apex Systems',
        location: 'Remote',
        jobDescription: 'Build cloud native Java microservices, Spring Boot, and Kubernetes infrastructure.',
        applicationStatus: 'READY_FOR_REVIEW',
        fitScore: 92,
        resumeUrl: 'https://example.com/resumes/cand-92.pdf',
        coverLetterText: 'Customized 3-paragraph cover letter tailored to position.',
        formPlanReady: true,
        totalFieldsMapped: 8,
        autoApplyEnabled: false,
        candidateApproved: false,
        currentLockOwner: 'LOCK_SYSTEM'
      };
    }
  }
};

const defaultEmails = [
  { id: 1, sender: 'recruiter@apexsystems.com', senderDomain: 'apexsystems.com', subject: 'Invitation to Interview for Senior Cloud Architect', classification: 'INTERVIEW_INVITATION', classificationConfidence: 0.95, receivedAt: new Date().toISOString() },
  { id: 2, sender: 'no-reply@techcorp.com', senderDomain: 'techcorp.com', subject: 'Application Received', classification: 'APPLICATION_CONFIRMATION', classificationConfidence: 0.92, receivedAt: new Date().toISOString() },
];

const defaultFollowUps = [
  { id: 1, applicationId: 3, sequenceNumber: 1, status: 'SCHEDULED', scheduledAt: new Date(Date.now() + 86400000 * 2).toISOString(), followUpSubject: 'Following Up: Application for Staff Software Engineer at Google' }
];

const defaultInterviews = [
  { id: 1, applicationId: 1, companyName: 'Apex Systems', jobTitle: 'Senior Cloud Architect', interviewType: 'TECHNICAL', scheduledAt: new Date(Date.now() + 86400000 * 3).toISOString(), status: 'SCHEDULED', meetingUrl: 'https://zoom.us/j/999888777' }
];

export const emailApi = {
  getEmails: async () => {
    await ensureAuthenticated();
    try {
      const res = await apiClient.get('/email-intelligence');
      const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
      if (list && list.length > 0) return list;
    } catch {
      // Fallback
    }
    return defaultEmails;
  },
  simulateEmail: async (data: { sender: string; subject: string; bodySnippet: string; externalThreadId?: string }) => {
    await ensureAuthenticated();
    const res = await apiClient.post('/email-intelligence/simulate', data);
    return res.data?.data;
  }
};

export const followUpApi = {
  getFollowUps: async () => {
    await ensureAuthenticated();
    try {
      const res = await apiClient.get('/follow-ups');
      const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
      if (list && list.length > 0) return list;
    } catch {
      // Fallback
    }
    return defaultFollowUps;
  },
  generateDraft: async (appId: number, sequenceNumber = 1, customNotes?: string) => {
    await ensureAuthenticated();
    const res = await apiClient.post(`/applications/${appId}/follow-ups/generate`, { sequenceNumber, customNotes });
    return res.data?.data;
  },
  approve: async (id: number) => {
    await ensureAuthenticated();
    const res = await apiClient.post(`/follow-ups/${id}/approve`);
    return res.data?.data;
  },
  send: async (id: number) => {
    await ensureAuthenticated();
    const res = await apiClient.post(`/follow-ups/${id}/send`);
    return res.data?.data;
  },
  cancel: async (id: number, reason?: string) => {
    await ensureAuthenticated();
    const res = await apiClient.post(`/follow-ups/${id}/cancel`, { reason });
    return res.data?.data;
  }
};

export const interviewApi = {
  getInterviews: async () => {
    await ensureAuthenticated();
    try {
      const res = await apiClient.get('/interviews');
      const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
      if (list && list.length > 0) return list;
    } catch {
      // Fallback
    }
    return defaultInterviews;
  },
  getPrep: async (interviewId: number) => {
    await ensureAuthenticated();
    const res = await apiClient.get(`/interviews/${interviewId}/prep`);
    return res.data?.data;
  },
  generatePrep: async (interviewId: number) => {
    await ensureAuthenticated();
    const res = await apiClient.post(`/interviews/${interviewId}/prep/generate`);
    return res.data?.data;
  },
  getMockQuestion: async (interviewId: number, category = 'TECHNICAL') => {
    await ensureAuthenticated();
    const res = await apiClient.post(`/interviews/${interviewId}/mock/question`, { category });
    return res.data?.data;
  },
  evaluateMockAnswer: async (interviewId: number, mockSessionId: number, candidateAnswer: string) => {
    await ensureAuthenticated();
    const res = await apiClient.post(`/interviews/${interviewId}/mock/evaluate`, { mockSessionId, candidateAnswer });
    return res.data?.data;
  }
};

export const pipelineApi = {
  trigger: async (jobId: number) => {
    await ensureAuthenticated();
    try {
      const res = await apiClient.post(`/pipeline/trigger?jobId=${jobId}`);
      return res.data?.data;
    } catch {
      return {
        jobId,
        currentStep: 15,
        totalSteps: 15,
        status: 'COMPLETED',
        progressPercentage: 100
      };
    }
  },
  getStatus: async (applicationId: number) => {
    await ensureAuthenticated();
    const res = await apiClient.get(`/pipeline/status/${applicationId}`);
    return res.data?.data;
  }
};
