import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { Sidebar, NavTab } from './components/Sidebar';
import { AuthModal } from './components/AuthModal';

import { DashboardPage } from './pages/DashboardPage';
import { JobsPage } from './pages/JobsPage';
import { ApplicationsPage } from './pages/ApplicationsPage';
import { EmailIntelligencePage } from './pages/EmailIntelligencePage';
import { FollowUpsPage } from './pages/FollowUpsPage';
import { InterviewsPage } from './pages/InterviewsPage';
import { SettingsPage } from './pages/SettingsPage';

import {
  dashboardApi,
  jobsApi,
  applicationsApi,
  emailApi,
  followUpApi,
  interviewApi,
} from './services/api';

export function App() {
  const [activeTab, setActiveTab] = useState<NavTab>('dashboard');
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [userEmail, setUserEmail] = useState<string | undefined>(undefined);

  const [summary, setSummary] = useState<any>(null);
  const [jobs, setJobs] = useState<any[]>([]);
  const [applications, setApplications] = useState<any[]>([]);
  const [emails, setEmails] = useState<any[]>([]);
  const [followUps, setFollowUps] = useState<any[]>([]);
  const [interviews, setInterviews] = useState<any[]>([]);

  const loadAllData = async () => {
    try {
      const [sumData, jobsData, appsData, emailsData, fuData, intData] = await Promise.all([
        dashboardApi.getSummary(),
        jobsApi.getJobs(),
        applicationsApi.getApplications(),
        emailApi.getEmails(),
        followUpApi.getFollowUps(),
        interviewApi.getInterviews(),
      ]);

      setSummary(sumData);
      setJobs(jobsData);
      setApplications(appsData);
      setEmails(emailsData);
      setFollowUps(fuData);
      setInterviews(intData);
    } catch (e) {
      console.error('Data loading error:', e);
    }
  };

  useEffect(() => {
    loadAllData();
  }, []);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      <Navbar userEmail={userEmail} onOpenAuth={() => setIsAuthOpen(true)} />

      <div className="flex flex-1 pt-16">
        <Sidebar
          activeTab={activeTab}
          setActiveTab={setActiveTab}
          reviewRequiredCount={summary?.reviewRequiredCount || 0}
          followUpsDueCount={summary?.followUpsDueCount || 0}
          upcomingInterviewsCount={summary?.upcomingInterviewsCount || 0}
        />

        <main className="flex-1 ml-64 p-8 max-w-7xl">
          {activeTab === 'dashboard' && <DashboardPage summary={summary} onNavigate={setActiveTab} />}
          {activeTab === 'jobs' && <JobsPage jobs={jobs} onRefresh={loadAllData} />}
          {activeTab === 'applications' && <ApplicationsPage applications={applications} onRefresh={loadAllData} />}
          {activeTab === 'emails' && <EmailIntelligencePage emails={emails} onRefresh={loadAllData} />}
          {activeTab === 'followups' && <FollowUpsPage followUps={followUps} onRefresh={loadAllData} />}
          {activeTab === 'interviews' && <InterviewsPage interviews={interviews} onRefresh={loadAllData} />}
          {activeTab === 'settings' && <SettingsPage userEmail={userEmail} />}
        </main>
      </div>

      <AuthModal
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
        onSuccess={(email) => setUserEmail(email)}
      />
    </div>
  );
}

export default App;
