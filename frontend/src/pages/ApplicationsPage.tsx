import React, { useState } from 'react';
import { Layers, CheckCircle2, Play, AlertCircle, Clock } from 'lucide-react';
import { applicationsApi } from '../services/api';
import { UnifiedApplicationWorkspaceModal } from '../components/UnifiedApplicationWorkspaceModal';

interface ApplicationsPageProps {
  applications: any[];
  onRefresh: () => void;
}

export const ApplicationsPage: React.FC<ApplicationsPageProps> = ({ applications, onRefresh }) => {
  const [selectedApp, setSelectedApp] = useState<any>(null);
  const [loadingAction, setLoadingAction] = useState(false);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'READY_FOR_REVIEW':
      case 'SUBMISSION_REQUIRES_REVIEW':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/10 border border-amber-500/20 text-amber-400">Review Required</span>;
      case 'APPROVED':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-indigo-500/10 border border-indigo-500/20 text-indigo-400">Approved</span>;
      case 'APPLYING':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-blue-500/10 border border-blue-500/20 text-blue-400 animate-pulse">Applying...</span>;
      case 'APPLIED':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">Applied</span>;
      case 'INTERVIEW':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-purple-500/10 border border-purple-500/20 text-purple-400">Interview Scheduled</span>;
      case 'REJECTED':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-500/10 border border-rose-500/20 text-rose-400">Rejected</span>;
      default:
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-800 text-slate-300">{status}</span>;
    }
  };

  const handleApproveAndPrepare = async (appId: number) => {
    setLoadingAction(true);
    try {
      await applicationsApi.approveAndPrepare(appId);
      alert('Application approved and form execution plan prepared!');
      onRefresh();
    } catch (err: any) {
      alert('Failed or already approved: ' + (err.message || err));
    } finally {
      setLoadingAction(false);
    }
  };

  const handleExecuteApplication = async (appId: number) => {
    setLoadingAction(true);
    try {
      await applicationsApi.executeApplication(appId);
      alert('Application submission executed!');
      onRefresh();
    } catch (err: any) {
      alert('Execution triggered: ' + (err.message || err));
      onRefresh();
    } finally {
      setLoadingAction(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white tracking-tight">Application Workspace & Pipeline</h2>
        <p className="text-sm text-slate-400">Human-in-the-loop review, approval, and browser execution controller</p>
      </div>

      {/* Applications List */}
      <div className="space-y-3">
        {applications.map((app) => (
          <div
            key={app.id}
            className="p-5 rounded-2xl border border-slate-800 bg-slate-900/60 hover:border-slate-700 transition flex items-center justify-between"
          >
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-white text-lg">
                {app.company ? app.company.charAt(0) : 'A'}
              </div>
              <div>
                <div className="flex items-center gap-3">
                  <h3 className="font-bold text-white text-base">{app.jobTitle || app.title || 'Software Position'}</h3>
                  {getStatusBadge(app.status)}
                </div>
                <div className="text-sm text-slate-400 mt-0.5">{app.company || 'Company'}</div>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <button
                onClick={() => setSelectedApp(app)}
                className="px-3.5 py-2 rounded-xl border border-slate-700 bg-slate-800 text-slate-300 hover:text-white text-xs font-medium transition"
              >
                Workspace Details
              </button>

              {(app.status === 'READY_FOR_REVIEW' || app.status === 'SUBMISSION_REQUIRES_REVIEW') && (
                <button
                  onClick={() => handleApproveAndPrepare(app.id)}
                  disabled={loadingAction}
                  className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium transition shadow-sm"
                >
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  Approve & Prepare
                </button>
              )}

              {app.status === 'APPROVED' && (
                <button
                  onClick={() => handleExecuteApplication(app.id)}
                  disabled={loadingAction}
                  className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-sky-600 hover:bg-sky-500 text-white text-xs font-medium transition shadow-sm"
                >
                  <Play className="w-3.5 h-3.5 fill-current" />
                  Execute Submission
                </button>
              )}
            </div>
          </div>
        ))}

        {applications.length === 0 && (
          <div className="p-12 text-center text-slate-400 text-sm border border-dashed border-slate-800 rounded-2xl">
            No applications created yet. Browse Job Discovery to select opportunities.
          </div>
        )}
      </div>

      {/* Unified Application Workspace Modal */}
      {selectedApp && (
        <UnifiedApplicationWorkspaceModal
          applicationId={selectedApp.id}
          onClose={() => setSelectedApp(null)}
          onRefresh={onRefresh}
        />
      )}
    </div>
  );
};
