import React from 'react';
import {
  Briefcase,
  Layers,
  AlertTriangle,
  Clock,
  Video,
  CheckCircle2,
  TrendingUp,
} from 'lucide-react';
import { NavTab } from '../components/Sidebar';

interface DashboardPageProps {
  summary: any;
  onNavigate: (tab: NavTab) => void;
}

export const DashboardPage: React.FC<DashboardPageProps> = ({ summary, onNavigate }) => {
  const stats = [
    { label: 'Jobs Discovered Today', value: summary?.totalJobsDiscovered || 0, icon: Briefcase, color: 'text-sky-400', bg: 'bg-sky-500/10 border-sky-500/20', action: () => onNavigate('jobs') },
    { label: 'High Match Opportunities (80%+)', value: summary?.highMatchJobsCount || 0, icon: TrendingUp, color: 'text-emerald-400', bg: 'bg-emerald-500/10 border-emerald-500/20', action: () => onNavigate('jobs') },
    { label: 'Review Required', value: summary?.reviewRequiredCount || 0, icon: AlertTriangle, color: 'text-amber-400', bg: 'bg-amber-500/10 border-amber-500/20', action: () => onNavigate('applications') },
    { label: 'Approved Applications', value: summary?.approvedApplicationsCount || 0, icon: CheckCircle2, color: 'text-indigo-400', bg: 'bg-indigo-500/10 border-indigo-500/20', action: () => onNavigate('applications') },
    { label: 'Follow-ups Due', value: summary?.followUpsDueCount || 0, icon: Clock, color: 'text-blue-400', bg: 'bg-blue-500/10 border-blue-500/20', action: () => onNavigate('followups') },
    { label: 'Upcoming Interviews', value: summary?.upcomingInterviewsCount || 0, icon: Video, color: 'text-purple-400', bg: 'bg-purple-500/10 border-purple-500/20', action: () => onNavigate('interviews') },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Executive Career Dashboard</h2>
          <p className="text-sm text-slate-400">Real-time application pipeline and response intelligence summary</p>
        </div>

        <button
          onClick={() => onNavigate('jobs')}
          className="px-4 py-2 bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white font-medium rounded-xl shadow-lg shadow-sky-500/20 text-sm transition"
        >
          Discover New Opportunities
        </button>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {stats.map((stat, idx) => {
          const Icon = stat.icon;
          return (
            <div
              key={idx}
              onClick={stat.action}
              className={`p-5 rounded-2xl border ${stat.bg} backdrop-blur cursor-pointer hover:scale-[1.01] transition shadow-md`}
            >
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-slate-300">{stat.label}</span>
                <Icon className={`w-5 h-5 ${stat.color}`} />
              </div>
              <div className="text-3xl font-extrabold text-white mt-3">{stat.value}</div>
            </div>
          );
        })}
      </div>

      {/* Activity Timeline */}
      <div className="p-6 rounded-2xl border border-slate-800 bg-slate-900/50 backdrop-blur space-y-4">
        <h3 className="text-lg font-semibold text-white flex items-center gap-2">
          <Layers className="w-5 h-5 text-sky-400" />
          Recent Activity Timeline
        </h3>

        {summary?.recentActivities && summary.recentActivities.length > 0 ? (
          <div className="space-y-3">
            {summary.recentActivities.map((act: any, i: number) => (
              <div key={i} className="p-3.5 rounded-xl border border-slate-800 bg-slate-800/40 flex items-center justify-between">
                <div>
                  <div className="font-semibold text-white text-sm">
                    {act.company} — {act.jobTitle}
                  </div>
                  <div className="text-xs text-slate-400 mt-0.5">{act.description}</div>
                </div>
                <span className="text-xs font-mono text-slate-400">
                  {act.createdAt ? new Date(act.createdAt).toLocaleTimeString() : 'Just now'}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-8 text-center text-slate-400 text-sm border border-dashed border-slate-800 rounded-xl">
            No activities recorded yet. Trigger job discovery or application workspace to begin.
          </div>
        )}
      </div>
    </div>
  );
};
