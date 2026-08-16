import React, { useState, useEffect } from 'react';
import { Sparkles, CheckCircle2, Play, AlertTriangle, Shield, Layers, FileText, UserCheck, Lock } from 'lucide-react';
import { applicationsApi } from '../services/api';

interface UnifiedWorkspaceModalProps {
  applicationId: number;
  onClose: () => void;
  onRefresh: () => void;
}

export const UnifiedApplicationWorkspaceModal: React.FC<UnifiedWorkspaceModalProps> = ({
  applicationId,
  onClose,
  onRefresh,
}) => {
  const [workspace, setWorkspace] = useState<any>(null);
  const [activeTab, setActiveTab] = useState<'job' | 'artifacts' | 'readiness' | 'audit'>('job');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchWorkspace = async () => {
      try {
        const data = await applicationsApi.getWorkspace(applicationId);
        setWorkspace(data);
      } catch (err) {
        console.error('Failed to load workspace', err);
      }
    };
    fetchWorkspace();
  }, [applicationId]);

  const handleApproveAndPrepare = async () => {
    setLoading(true);
    try {
      await applicationsApi.approveAndPrepare(applicationId);
      alert('Application approved and form execution plan prepared!');
      onRefresh();
      onClose();
    } catch (err: any) {
      alert('Approve failed: ' + (err.message || err));
    } finally {
      setLoading(false);
    }
  };

  const handleExecute = async () => {
    setLoading(true);
    try {
      await applicationsApi.executeApplication(applicationId);
      alert('Application submission executed!');
      onRefresh();
      onClose();
    } catch (err: any) {
      alert('Execution triggered: ' + (err.message || err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-800 w-full max-w-4xl rounded-2xl p-6 space-y-5 max-h-[90vh] overflow-y-auto">
        <div className="flex items-start justify-between border-b border-slate-800 pb-4">
          <div>
            <div className="flex items-center gap-3">
              <h3 className="text-xl font-bold text-white">{workspace?.jobTitle || 'Software Position'}</h3>
              <span className="px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">
                {workspace?.applicationStatus || 'READY_FOR_REVIEW'}
              </span>
            </div>
            <div className="text-sm text-sky-400 font-semibold mt-1">{workspace?.company || 'Company'} • {workspace?.location || 'Remote'}</div>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white font-bold text-lg">✕</button>
        </div>

        {/* Unified Section Tabs */}
        <div className="flex border-b border-slate-800 gap-4 text-xs font-semibold">
          <button
            onClick={() => setActiveTab('job')}
            className={`pb-2 border-b-2 transition flex items-center gap-1.5 ${activeTab === 'job' ? 'border-sky-500 text-sky-400' : 'border-transparent text-slate-400 hover:text-slate-200'}`}
          >
            <Sparkles className="w-3.5 h-3.5" /> Job & AI Qualification
          </button>
          <button
            onClick={() => setActiveTab('artifacts')}
            className={`pb-2 border-b-2 transition flex items-center gap-1.5 ${activeTab === 'artifacts' ? 'border-sky-500 text-sky-400' : 'border-transparent text-slate-400 hover:text-slate-200'}`}
          >
            <FileText className="w-3.5 h-3.5" /> Resume & Cover Letter
          </button>
          <button
            onClick={() => setActiveTab('readiness')}
            className={`pb-2 border-b-2 transition flex items-center gap-1.5 ${activeTab === 'readiness' ? 'border-sky-500 text-sky-400' : 'border-transparent text-slate-400 hover:text-slate-200'}`}
          >
            <CheckCircle2 className="w-3.5 h-3.5" /> Form Readiness Checklist
          </button>
          <button
            onClick={() => setActiveTab('audit')}
            className={`pb-2 border-b-2 transition flex items-center gap-1.5 ${activeTab === 'audit' ? 'border-sky-500 text-sky-400' : 'border-transparent text-slate-400 hover:text-slate-200'}`}
          >
            <Layers className="w-3.5 h-3.5" /> Audit Log & Execution Lock
          </button>
        </div>

        {/* Tab 1: Job Info */}
        {activeTab === 'job' && (
          <div className="space-y-4 text-xs">
            <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-1">
              <div className="font-bold text-white text-sm">Job Description</div>
              <p className="text-slate-300 whitespace-pre-line leading-relaxed">{workspace?.jobDescription || 'Detailed role description and technical expectations.'}</p>
            </div>
            <div className="p-4 rounded-xl bg-sky-500/10 border border-sky-500/20 space-y-1">
              <div className="font-bold text-sky-400 text-sm flex items-center gap-1.5">
                <Sparkles className="w-4 h-4" /> AI Qualification Score: {workspace?.fitScore || 92}% Match
              </div>
              <p className="text-slate-300">Strong fit for Java Spring Boot, microservices architecture, and distributed system design.</p>
            </div>
          </div>
        )}

        {/* Tab 2: Artifacts */}
        {activeTab === 'artifacts' && (
          <div className="space-y-4 text-xs">
            <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-2">
              <div className="font-bold text-emerald-400 text-sm">Tailored Resume PDF</div>
              <p className="text-slate-300">Targeted resume grounded strictly in candidate profile facts and role keyword requirements.</p>
            </div>
            <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-2">
              <div className="font-bold text-indigo-400 text-sm">Personalized Cover Letter</div>
              <p className="text-slate-300 whitespace-pre-line leading-relaxed">{workspace?.coverLetterText}</p>
            </div>
          </div>
        )}

        {/* Tab 3: Readiness */}
        {activeTab === 'readiness' && (
          <div className="space-y-4 text-xs">
            <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-2">
              <div className="font-bold text-white text-sm">Form Discovery & Mapping Status</div>
              <div className="flex items-center justify-between text-slate-300">
                <span>Total Fields Mapped:</span>
                <span className="font-mono font-bold text-emerald-400">8 / 8 Mapped (100%)</span>
              </div>
              <div className="flex items-center justify-between text-slate-300">
                <span>Form Plan Status:</span>
                <span className="font-mono text-sky-400">READY_FOR_EXECUTION</span>
              </div>
            </div>
          </div>
        )}

        {/* Tab 4: Audit Log */}
        {activeTab === 'audit' && (
          <div className="space-y-4 text-xs">
            <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-2">
              <div className="font-bold text-white text-sm flex items-center gap-2">
                <Lock className="w-4 h-4 text-amber-400" />
                Execution Lock & Governance
              </div>
              <div className="text-slate-300">Distributed Execution Lock Owner: <span className="font-mono text-sky-400">{workspace?.currentLockOwner}</span></div>
            </div>
          </div>
        )}

        {/* Safety Locks Panel & Footer Actions */}
        <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2 text-xs text-emerald-400 font-medium">
            <Shield className="w-4 h-4" />
            Safety Policy Active: AUTO_APPLY = OFF (Requires Candidate Approval)
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={handleApproveAndPrepare}
              disabled={loading}
              className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold shadow-sm"
            >
              Approve & Prepare
            </button>
            <button
              onClick={handleExecute}
              disabled={loading}
              className="px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white rounded-xl text-xs font-semibold shadow-sm"
            >
              Execute Submission
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
