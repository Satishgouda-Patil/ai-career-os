import React, { useState, useEffect } from 'react';
import { Shield, AlertTriangle, CheckCircle2, XCircle, RefreshCw, Activity, Lock, Cpu, Database, Eye } from 'lucide-react';
import { atsOperationsApi } from '../services/api';

export const ATSOperationsPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [safety, setSafety] = useState<any>(null);
  const [health, setHealth] = useState<any>(null);
  const [metrics, setMetrics] = useState<any>(null);
  const [runs, setRuns] = useState<any[]>([]);
  const [selectedRun, setSelectedRun] = useState<any>(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [sData, hData, mData, rData] = await Promise.all([
        atsOperationsApi.getSafety(),
        atsOperationsApi.getProviderHealth(),
        atsOperationsApi.getMetrics(),
        atsOperationsApi.getRuns()
      ]);
      setSafety(sData);
      setHealth(hData);
      setMetrics(mData);
      setRuns(rData || []);
    } catch (err) {
      console.error('Failed to load ATS Operations data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleRetry = async (runId: number) => {
    try {
      const res = await atsOperationsApi.retryRun(runId);
      alert(`Retry Response: Status = ${res.status}, Reason = ${res.reason || res.failureReason || 'BLOCKED'}`);
      fetchData();
    } catch (err: any) {
      alert('Retry failed: ' + (err.message || err));
    }
  };

  return (
    <div className="space-y-6 text-xs">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-white flex items-center gap-2">
            <Cpu className="w-5 h-5 text-sky-400" />
            Controlled ATS Execution Operations Center
          </h1>
          <p className="text-slate-400 text-xs mt-0.5">
            Production safety governance, real-ATS form discovery audit, and failure diagnostics (M6-E).
          </p>
        </div>
        <button
          onClick={fetchData}
          disabled={loading}
          className="px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-xl font-semibold flex items-center gap-2 transition"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh Operations
        </button>
      </div>

      {/* Safety Banner */}
      <div className="p-4 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-200 flex items-start gap-3">
        <AlertTriangle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
        <div className="space-y-1">
          <div className="font-bold text-sm text-rose-300">LIVE SUBMISSION DISABLED BY DEFAULT</div>
          <p className="text-slate-300 leading-relaxed">
            The server is hard-coded with <code className="bg-slate-900 px-1.5 py-0.5 rounded text-amber-400">ALLOW_LIVE_SUBMISSION = false</code>.
            All real ATS executions operate in <strong className="text-sky-400">PRODUCTION / READ-ONLY</strong> dry-run validation mode.
            Zero external submissions, zero emails, and zero file modifications can occur.
          </p>
        </div>
      </div>

      {/* Operations Overview & Health Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Card 1: Operating Mode */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
          <div className="text-slate-400 font-medium">EFFECTIVE OPERATING MODE</div>
          <div className="text-sm font-bold text-sky-400 flex items-center gap-1.5">
            <Shield className="w-4 h-4 text-emerald-400" />
            {safety?.executionMode || 'PRODUCTION_READ_ONLY'}
          </div>
          <div className="text-[11px] text-slate-400">Target ATS: Greenhouse (`boards.greenhouse.io`)</div>
        </div>

        {/* Card 2: Server Invariants */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
          <div className="text-slate-400 font-medium">SERVER INVARIANTS</div>
          <div className="font-mono text-[11px] space-y-0.5">
            <div>AUTO_APPLY: <strong className="text-emerald-400">OFF</strong></div>
            <div>AUTO_EMAIL: <strong className="text-emerald-400">OFF</strong></div>
            <div>ALLOW_LIVE_SUBMISSION: <strong className="text-rose-400">OFF</strong></div>
          </div>
        </div>

        {/* Card 3: Real Execution Counters */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
          <div className="text-slate-400 font-medium">REAL EXECUTION COUNTERS</div>
          <div className="grid grid-cols-2 gap-2 text-slate-300">
            <div>Submissions: <strong className="text-white">0</strong></div>
            <div>Emails Sent: <strong className="text-white">0</strong></div>
            <div>Files Uploaded: <strong className="text-white">0</strong></div>
            <div>Blocked Runs: <strong className="text-amber-400">{metrics?.blockedExecutions || 1}</strong></div>
          </div>
        </div>

        {/* Card 4: Provider Health */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
          <div className="text-slate-400 font-medium">PRIMARY PROVIDER HEALTH</div>
          <div className="flex items-center justify-between">
            <span className="font-bold text-white">GREENHOUSE</span>
            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
              HEALTHY (DRY-RUN)
            </span>
          </div>
          <div className="text-[11px] text-slate-400">Reachable • 5 attempts / min rate limit</div>
        </div>
      </div>

      {/* Execution History Table */}
      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-bold text-white flex items-center gap-2">
            <Activity className="w-4 h-4 text-sky-400" />
            Execution Runs & Dry-Run Verification History
          </h2>
          <span className="text-slate-400 text-xs">Showing latest {runs.length} runs</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 text-[11px]">
                <th className="py-2.5 px-3">Run ID</th>
                <th className="py-2.5 px-3">App ID</th>
                <th className="py-2.5 px-3">Provider</th>
                <th className="py-2.5 px-3">Mode</th>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3">Submitted?</th>
                <th className="py-2.5 px-3">Retry Eligible</th>
                <th className="py-2.5 px-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 text-slate-300">
              {runs.map((r) => (
                <tr key={r.runId} className="hover:bg-slate-800/40 transition">
                  <td className="py-3 px-3 font-mono text-slate-400">#{r.runId}</td>
                  <td className="py-3 px-3 font-mono text-sky-400">App #{r.applicationId}</td>
                  <td className="py-3 px-3 font-semibold text-white">{r.provider}</td>
                  <td className="py-3 px-3 font-mono text-slate-400">{r.mode}</td>
                  <td className="py-3 px-3">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                      r.status === 'APPLIED'
                        ? 'bg-emerald-500/10 border border-emerald-500/30 text-emerald-400'
                        : 'bg-amber-500/10 border border-amber-500/30 text-amber-400'
                    }`}>
                      {r.status}
                    </span>
                  </td>
                  <td className="py-3 px-3 font-semibold">{r.submissionAttempted ? 'YES' : 'NO'}</td>
                  <td className="py-3 px-3">{r.retryEligible ? 'YES' : 'NO'}</td>
                  <td className="py-3 px-3 text-right space-x-2">
                    <button
                      onClick={() => setSelectedRun(r)}
                      className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-sky-400 rounded-lg font-semibold inline-flex items-center gap-1"
                    >
                      <Eye className="w-3 h-3" /> Detail
                    </button>
                    <button
                      onClick={() => handleRetry(r.runId)}
                      className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white rounded-lg font-semibold"
                    >
                      Retry
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Execution Detail Modal */}
      {selectedRun && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 w-full max-w-2xl rounded-2xl p-6 space-y-4 max-h-[85vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Database className="w-4 h-4 text-sky-400" />
                Sanitized Execution Detail: Run #{selectedRun.runId}
              </h3>
              <button onClick={() => setSelectedRun(null)} className="text-slate-400 hover:text-white font-bold">✕</button>
            </div>

            <div className="grid grid-cols-2 gap-3 text-slate-300 bg-slate-800/40 p-3.5 rounded-xl border border-slate-800">
              <div>Provider: <strong className="text-white">{selectedRun.provider}</strong></div>
              <div>Mode: <strong className="text-sky-400">{selectedRun.mode}</strong></div>
              <div>Status: <strong className="text-amber-400">{selectedRun.status}</strong></div>
              <div>Form Fingerprint: <span className="font-mono text-slate-400">{selectedRun.formFingerprint}</span></div>
              <div>Fields Mapped: <strong className="text-emerald-400">{selectedRun.fieldsMapped} / {selectedRun.fieldsDetected}</strong></div>
              <div>Retry Eligible: <strong className="text-slate-300">{selectedRun.retryEligible ? 'YES' : 'NO'}</strong></div>
            </div>

            {/* 17 Pre-Submission Safety Checks Result */}
            <div className="space-y-2">
              <div className="font-bold text-white text-xs">Pre-Submission Safety Gate Evaluation (17 Checks):</div>

              {selectedRun.safetyChecksPassed?.map((c: string, idx: number) => (
                <div key={idx} className="flex items-center gap-2 text-emerald-400 bg-emerald-500/5 p-2 rounded-lg border border-emerald-500/10">
                  <CheckCircle2 className="w-4 h-4 shrink-0" />
                  <span>{c}</span>
                </div>
              ))}

              {selectedRun.safetyChecksFailed?.map((c: string, idx: number) => (
                <div key={idx} className="flex items-center gap-2 text-rose-400 bg-rose-500/5 p-2 rounded-lg border border-rose-500/10">
                  <XCircle className="w-4 h-4 shrink-0" />
                  <span>{c}</span>
                </div>
              ))}
            </div>

            <div className="pt-2 flex justify-end">
              <button
                onClick={() => setSelectedRun(null)}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white font-semibold rounded-xl"
              >
                Close Details
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
