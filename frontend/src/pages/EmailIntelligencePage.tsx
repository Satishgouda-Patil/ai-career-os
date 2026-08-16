import React, { useState } from 'react';
import { Mail, Sparkles, Send, Calendar, AlertTriangle } from 'lucide-react';
import { emailApi } from '../services/api';

interface EmailIntelligencePageProps {
  emails: any[];
  onRefresh: () => void;
}

export const EmailIntelligencePage: React.FC<EmailIntelligencePageProps> = ({ emails, onRefresh }) => {
  const [selectedEmail, setSelectedEmail] = useState<any>(null);
  const [showSimulateModal, setShowSimulateModal] = useState(false);

  const [simSender, setSimSender] = useState('recruiter@google.com');
  const [simSubject, setSimSubject] = useState('Invitation to Interview for Staff Software Engineer');
  const [simBody, setSimBody] = useState('Hi, we reviewed your profile and would like to schedule a technical interview. Please pick a slot on Zoom: https://zoom.us/j/123456');

  const getClassificationBadge = (cat: string) => {
    switch (cat) {
      case 'INTERVIEW_INVITATION':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-purple-500/10 border border-purple-500/20 text-purple-400">Interview Invitation</span>;
      case 'REJECTION':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-500/10 border border-rose-500/20 text-rose-400">Rejection Notice</span>;
      case 'APPLICATION_CONFIRMATION':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">Confirmation Received</span>;
      case 'RECRUITER_RESPONSE':
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-sky-500/10 border border-sky-500/20 text-sky-400">Recruiter Follow-up</span>;
      default:
        return <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-800 text-slate-300">{cat || 'Classified'}</span>;
    }
  };

  const handleSimulateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await emailApi.simulateEmail({
        sender: simSender,
        subject: simSubject,
        bodySnippet: simBody
      });
      alert('Simulated email ingested, classified, and auto-matched!');
      setShowSimulateModal(false);
      onRefresh();
    } catch (err) {
      alert('Failed to simulate email');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Email & Recruiter Response Intelligence</h2>
          <p className="text-sm text-slate-400">AI parsing, classification, and application auto-association pipeline</p>
        </div>

        <button
          onClick={() => setShowSimulateModal(true)}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white font-medium rounded-xl text-sm transition shadow-md"
        >
          <Sparkles className="w-4 h-4" />
          Simulate Recruiter Email
        </button>
      </div>

      {/* Email List */}
      <div className="space-y-3">
        {emails.map((email) => (
          <div
            key={email.id}
            onClick={() => setSelectedEmail(email)}
            className="p-5 rounded-2xl border border-slate-800 bg-slate-900/60 hover:border-slate-700 transition cursor-pointer flex items-center justify-between"
          >
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-xl bg-slate-800 border border-slate-700 flex items-center justify-center text-sky-400">
                <Mail className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-3">
                  <h3 className="font-semibold text-white text-sm">{email.subject}</h3>
                  {getClassificationBadge(email.classification)}
                </div>
                <div className="text-xs text-slate-400 mt-1">From: {email.sender}</div>
              </div>
            </div>

            <div className="text-xs text-slate-400 font-mono">
              {email.receivedAt ? new Date(email.receivedAt).toLocaleDateString() : 'Today'}
            </div>
          </div>
        ))}

        {emails.length === 0 && (
          <div className="p-12 text-center text-slate-400 text-sm border border-dashed border-slate-800 rounded-2xl">
            No recruiter emails ingested yet. Click 'Simulate Recruiter Email' to test live parsing.
          </div>
        )}
      </div>

      {/* Simulate Modal */}
      {showSimulateModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <form onSubmit={handleSimulateSubmit} className="bg-slate-900 border border-slate-800 w-full max-w-xl rounded-2xl p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-sky-400" />
                Simulate Recruiter Email Ingestion
              </h3>
              <button type="button" onClick={() => setShowSimulateModal(false)} className="text-slate-400 hover:text-white">✕</button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 font-semibold mb-1">Sender Email</label>
                <input
                  type="email"
                  value={simSender}
                  onChange={(e) => setSimSender(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg p-2.5 text-white focus:outline-none focus:border-sky-500"
                  required
                />
              </div>

              <div>
                <label className="block text-slate-300 font-semibold mb-1">Subject</label>
                <input
                  type="text"
                  value={simSubject}
                  onChange={(e) => setSimSubject(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg p-2.5 text-white focus:outline-none focus:border-sky-500"
                  required
                />
              </div>

              <div>
                <label className="block text-slate-300 font-semibold mb-1">Body Snippet</label>
                <textarea
                  rows={4}
                  value={simBody}
                  onChange={(e) => setSimBody(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-lg p-2.5 text-white focus:outline-none focus:border-sky-500"
                  required
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-800">
              <button type="button" onClick={() => setShowSimulateModal(false)} className="px-4 py-2 rounded-xl border border-slate-700 text-slate-300 text-xs">Cancel</button>
              <button type="submit" className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold">Simulate & Ingest</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
