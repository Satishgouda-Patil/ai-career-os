import React, { useEffect, useState } from 'react';
import { controlCenterApi } from '../services/api';
import {
  ShieldAlert,
  ShieldCheck,
  Activity,
  Server,
  Lock,
  Radio,
  CheckCircle2,
  XCircle,
  Clock,
  AlertTriangle
} from 'lucide-react';

export const ControlCenterPage: React.FC = () => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const summary = await controlCenterApi.getSummary();
      setData(summary);
    } catch (e) {
      console.error('Failed to load control center data:', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  const mode = data?.operatingMode || 'PRODUCTION / READ-ONLY';
  const flags = data?.safetyFlags || {};
  const providers = data?.providers || [];
  const audits = data?.recentAudits || [];
  const failures = data?.recentFailures || [];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
            <Activity className="w-7 h-7 text-indigo-400" />
            Production Automation Control Center
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Real-time provider health, rate limits, audit trail, and security governance controls.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xs uppercase tracking-wider font-semibold text-slate-400">Mode:</span>
          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <Radio className="w-3.5 h-3.5 animate-pulse text-emerald-400" />
            {mode}
          </span>
        </div>
      </div>

      {/* Prominent Live Submission Safety Gate Banner */}
      <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-start gap-3">
        <ShieldAlert className="w-6 h-6 text-amber-400 shrink-0 mt-0.5" />
        <div className="flex-1">
          <div className="flex items-center gap-2 font-semibold text-amber-300">
            <span>LIVE ATS SUBMISSION IS CURRENTLY DISABLED</span>
            <span className="px-2 py-0.5 text-[10px] font-bold uppercase rounded bg-amber-500/20 border border-amber-500/40 text-amber-200">
              SAFETY GATE ACTIVE
            </span>
          </div>
          <p className="text-xs text-slate-300 mt-1">
            All Greenhouse execution providers operate in Safe Controlled Dry-Run mode. Real external job submissions will NOT occur without explicitly setting <code className="bg-slate-800 px-1 py-0.5 rounded text-amber-300">app.safety.allow-live-submission=true</code> on the backend.
          </p>
        </div>
      </div>

      {/* Safety Policy Governance Controls Grid */}
      <div className="bg-slate-900/60 rounded-xl border border-slate-800 p-5">
        <h2 className="text-sm font-semibold text-slate-300 mb-4 flex items-center gap-2">
          <Lock className="w-4 h-4 text-indigo-400" />
          Safety & Governance Flags
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-4 rounded-lg bg-slate-800/50 border border-slate-700/50 flex flex-col justify-between">
            <div className="text-xs text-slate-400 font-medium">AUTO_APPLY</div>
            <div className="mt-2 flex items-center justify-between">
              <span className="text-lg font-bold text-slate-200">
                {flags.autoApply ? 'ENABLED' : 'OFF'}
              </span>
              {flags.autoApply ? (
                <CheckCircle2 className="w-5 h-5 text-emerald-400" />
              ) : (
                <XCircle className="w-5 h-5 text-slate-500" />
              )}
            </div>
            <p className="text-[11px] text-slate-400 mt-1">Automatic background applications</p>
          </div>

          <div className="p-4 rounded-lg bg-slate-800/50 border border-slate-700/50 flex flex-col justify-between">
            <div className="text-xs text-slate-400 font-medium">AUTO_SEND_EMAIL</div>
            <div className="mt-2 flex items-center justify-between">
              <span className="text-lg font-bold text-slate-200">
                {flags.autoSendEmail ? 'ENABLED' : 'OFF'}
              </span>
              {flags.autoSendEmail ? (
                <CheckCircle2 className="w-5 h-5 text-emerald-400" />
              ) : (
                <XCircle className="w-5 h-5 text-slate-500" />
              )}
            </div>
            <p className="text-[11px] text-slate-400 mt-1">Outbound SMTP email dispatch</p>
          </div>

          <div className="p-4 rounded-lg bg-slate-800/50 border border-slate-700/50 flex flex-col justify-between">
            <div className="text-xs text-slate-400 font-medium">AUTO_LINKEDIN</div>
            <div className="mt-2 flex items-center justify-between">
              <span className="text-lg font-bold text-slate-200">
                {flags.autoLinkedIn ? 'ENABLED' : 'OFF'}
              </span>
              {flags.autoLinkedIn ? (
                <CheckCircle2 className="w-5 h-5 text-emerald-400" />
              ) : (
                <XCircle className="w-5 h-5 text-slate-500" />
              )}
            </div>
            <p className="text-[11px] text-slate-400 mt-1">Automated LinkedIn messaging</p>
          </div>

          <div className="p-4 rounded-lg bg-amber-500/10 border border-amber-500/30 flex flex-col justify-between">
            <div className="text-xs text-amber-300 font-medium">ALLOW_LIVE_SUBMISSION</div>
            <div className="mt-2 flex items-center justify-between">
              <span className="text-lg font-bold text-amber-200">
                {flags.allowLiveSubmission ? 'ENABLED' : 'OFF (SAFE)'}
              </span>
              <ShieldCheck className="w-5 h-5 text-emerald-400" />
            </div>
            <p className="text-[11px] text-amber-300/80 mt-1">Hard server-side submission gate</p>
          </div>
        </div>
      </div>

      {/* Production Provider Health & Rate Limits */}
      <div className="bg-slate-900/60 rounded-xl border border-slate-800 p-5">
        <h2 className="text-sm font-semibold text-slate-300 mb-4 flex items-center gap-2">
          <Server className="w-4 h-4 text-indigo-400" />
          Active Production Providers
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {providers.map((p: any, idx: number) => (
            <div key={idx} className="p-4 rounded-xl bg-slate-800/40 border border-slate-700/60 flex flex-col justify-between space-y-3">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-semibold text-slate-200 text-sm">{p.providerName}</h3>
                  <span className="text-[11px] text-slate-400">{p.category}</span>
                </div>
                <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                  <CheckCircle2 className="w-3 h-3" />
                  {p.status}
                </span>
              </div>
              <div className="space-y-1 text-xs text-slate-300">
                <div className="flex justify-between">
                  <span className="text-slate-400">Rate Limit:</span>
                  <span className="font-medium text-slate-200">{p.rateLimitInfo}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Status:</span>
                  <span className="font-medium text-slate-200">{p.lastSync}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Sanitized Integration Audit History */}
      <div className="bg-slate-900/60 rounded-xl border border-slate-800 p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-semibold text-slate-300 flex items-center gap-2">
            <Clock className="w-4 h-4 text-indigo-400" />
            Sanitized External Audit History
          </h2>
          <span className="text-xs text-slate-400">{audits.length} recent operations recorded</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 uppercase tracking-wider text-[10px]">
                <th className="py-2.5 px-3">Timestamp</th>
                <th className="py-2.5 px-3">Provider</th>
                <th className="py-2.5 px-3">Action</th>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3">Summary</th>
                <th className="py-2.5 px-3">Duration</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300">
              {audits.map((a: any) => (
                <tr key={a.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="py-2.5 px-3 whitespace-nowrap text-slate-400 font-mono">
                    {a.createdAt ? new Date(a.createdAt).toLocaleTimeString() : 'N/A'}
                  </td>
                  <td className="py-2.5 px-3 font-semibold text-indigo-300">{a.providerName}</td>
                  <td className="py-2.5 px-3 text-slate-300">{a.actionType}</td>
                  <td className="py-2.5 px-3">
                    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-semibold ${
                      a.status === 'SUCCESS' || a.status === 'PASSED'
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                        : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                    }`}>
                      {a.status}
                    </span>
                  </td>
                  <td className="py-2.5 px-3 font-mono text-[11px] text-slate-400 max-w-xs truncate">
                    {a.requestSummary}
                  </td>
                  <td className="py-2.5 px-3 text-slate-400 whitespace-nowrap">{a.executionTimeMs} ms</td>
                </tr>
              ))}
              {audits.length === 0 && (
                <tr>
                  <td colSpan={6} className="py-6 text-center text-slate-400">
                    No external audit records found yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
