import React, { useState } from 'react';
import { Clock, CheckCircle2, Send, XCircle, Sparkles } from 'lucide-react';
import { followUpApi } from '../services/api';

interface FollowUpsPageProps {
  followUps: any[];
  onRefresh: () => void;
}

export const FollowUpsPage: React.FC<FollowUpsPageProps> = ({ followUps, onRefresh }) => {
  const [selectedFollowUp, setSelectedFollowUp] = useState<any>(null);
  const [loadingAction, setLoadingAction] = useState(false);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'SCHEDULED':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-sky-500/10 border border-sky-500/20 text-sky-400">Scheduled</span>;
      case 'READY':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">Draft Ready</span>;
      case 'SENT':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-indigo-500/10 border border-indigo-500/20 text-indigo-400">Dispatched</span>;
      case 'CANCELLED':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-800 text-slate-400">Cancelled</span>;
      default:
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-800 text-slate-300">{status}</span>;
    }
  };

  const handleApprove = async (id: number) => {
    setLoadingAction(true);
    try {
      await followUpApi.approve(id);
      alert('Follow-up draft approved!');
      onRefresh();
    } catch (err) {
      alert('Failed to approve follow-up');
    } finally {
      setLoadingAction(false);
    }
  };

  const handleSend = async (id: number) => {
    setLoadingAction(true);
    try {
      await followUpApi.send(id);
      alert('Follow-up dispatched successfully!');
      onRefresh();
    } catch (err) {
      alert('Failed to send follow-up');
    } finally {
      setLoadingAction(false);
    }
  };

  const handleCancel = async (id: number) => {
    setLoadingAction(true);
    try {
      await followUpApi.cancel(id, 'Cancelled by user');
      alert('Follow-up cancelled');
      onRefresh();
    } catch (err) {
      alert('Failed to cancel follow-up');
    } finally {
      setLoadingAction(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white tracking-tight">Follow-up Automation Sequence</h2>
        <p className="text-sm text-slate-400">Multi-step rule engine (#1 at +3 days, #2 at +5 days, auto-cancel on recruiter response)</p>
      </div>

      {/* Follow-ups List */}
      <div className="space-y-3">
        {followUps.map((item) => (
          <div
            key={item.id}
            className="p-5 rounded-2xl border border-slate-800 bg-slate-900/60 hover:border-slate-700 transition flex items-center justify-between"
          >
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-xl bg-slate-800 border border-slate-700 flex items-center justify-center text-sky-400 font-bold">
                #{item.sequenceNumber || 1}
              </div>
              <div>
                <div className="flex items-center gap-3">
                  <h3 className="font-semibold text-white text-sm">{item.followUpSubject || 'Polite Check-in'}</h3>
                  {getStatusBadge(item.status)}
                </div>
                <div className="text-xs text-slate-400 mt-1 flex items-center gap-2">
                  <Clock className="w-3.5 h-3.5" />
                  Scheduled: {item.scheduledAt ? new Date(item.scheduledAt).toLocaleDateString() : 'In 3 days'}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setSelectedFollowUp(item)}
                className="px-3 py-1.5 rounded-lg border border-slate-700 bg-slate-800 text-slate-300 hover:text-white text-xs font-medium"
              >
                View Draft
              </button>

              {item.status !== 'SENT' && item.status !== 'CANCELLED' && (
                <>
                  {item.status === 'SCHEDULED' && (
                    <button
                      onClick={() => handleApprove(item.id)}
                      disabled={loadingAction}
                      className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium"
                    >
                      Approve
                    </button>
                  )}
                  {item.status === 'READY' && (
                    <button
                      onClick={() => handleSend(item.id)}
                      disabled={loadingAction}
                      className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 text-white text-xs font-medium"
                    >
                      <Send className="w-3.5 h-3.5" /> Send
                    </button>
                  )}
                  <button
                    onClick={() => handleCancel(item.id)}
                    disabled={loadingAction}
                    className="p-1.5 rounded-lg border border-slate-800 text-slate-400 hover:text-rose-400"
                  >
                    <XCircle className="w-4 h-4" />
                  </button>
                </>
              )}
            </div>
          </div>
        ))}

        {followUps.length === 0 && (
          <div className="p-12 text-center text-slate-400 text-sm border border-dashed border-slate-800 rounded-2xl">
            No follow-ups scheduled yet. Applications automatically schedule follow-up #1 upon submission.
          </div>
        )}
      </div>

      {/* Draft Modal */}
      {selectedFollowUp && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 w-full max-w-2xl rounded-2xl p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-lg font-bold text-white">Follow-up #{selectedFollowUp.sequenceNumber} Draft</h3>
              <button onClick={() => setSelectedFollowUp(null)} className="text-slate-400 hover:text-white">✕</button>
            </div>

            <div className="space-y-2 text-xs">
              <div className="font-semibold text-slate-300">Subject:</div>
              <div className="p-2.5 rounded-lg bg-slate-800 text-white font-mono">{selectedFollowUp.followUpSubject}</div>
            </div>

            <div className="space-y-2 text-xs">
              <div className="font-semibold text-slate-300">Body Content:</div>
              <div className="p-3 rounded-lg bg-slate-800 text-slate-200 whitespace-pre-line leading-relaxed">
                {selectedFollowUp.followUpBody || 'Polite check-in inquiring about application status.'}
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-800">
              <button onClick={() => setSelectedFollowUp(null)} className="px-4 py-2 rounded-xl border border-slate-700 text-slate-300 text-xs">Close</button>
              {selectedFollowUp.status !== 'SENT' && selectedFollowUp.status !== 'CANCELLED' && (
                <button
                  onClick={() => {
                    handleSend(selectedFollowUp.id);
                    setSelectedFollowUp(null);
                  }}
                  className="px-4 py-2 rounded-xl bg-sky-600 hover:bg-sky-500 text-white text-xs font-semibold"
                >
                  Send Follow-up Now
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
