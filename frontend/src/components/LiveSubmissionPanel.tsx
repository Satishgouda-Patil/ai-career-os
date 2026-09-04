import React, { useState, useEffect } from 'react';
import { Shield, AlertOctagon, CheckCircle2, Lock, Play, RefreshCw, Info, UserCheck } from 'lucide-react';
import { liveExecutionApi } from '../services/api';

interface LiveSubmissionPanelProps {
  applicationId: number;
  onRefresh?: () => void;
}

export const LiveSubmissionPanel: React.FC<LiveSubmissionPanelProps> = ({
  applicationId,
  onRefresh
}) => {
  const [loading, setLoading] = useState(false);
  const [prepareData, setPrepareData] = useState<any>(null);
  const [statusData, setStatusData] = useState<any>(null);
  const [candidateConfirmed, setCandidateConfirmed] = useState(false);
  const [executeResult, setExecuteResult] = useState<any>(null);

  const fetchStatus = async () => {
    try {
      const data = await liveExecutionApi.getStatus(applicationId);
      setStatusData(data);
    } catch (err) {
      console.error('Failed to load live status', err);
    }
  };

  useEffect(() => {
    fetchStatus();
  }, [applicationId]);

  const handlePrepare = async () => {
    setLoading(true);
    setExecuteResult(null);
    try {
      const res = await liveExecutionApi.prepare(applicationId);
      setPrepareData(res);
      await fetchStatus();
    } catch (err: any) {
      alert('Prepare failed: ' + (err.message || err));
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async () => {
    if (!candidateConfirmed) {
      alert('Please check the authorization box before confirming.');
      return;
    }
    setLoading(true);
    try {
      const res = await liveExecutionApi.confirm(applicationId, prepareData?.formFingerprint);
      alert(res?.message || 'Submission confirmed!');
      await fetchStatus();
      if (onRefresh) onRefresh();
    } catch (err: any) {
      alert('Confirm failed: ' + (err.message || err));
    } finally {
      setLoading(false);
    }
  };

  const handleExecute = async () => {
    setLoading(true);
    try {
      const res = await liveExecutionApi.execute(applicationId, prepareData?.formFingerprint);
      setExecuteResult(res);
      await fetchStatus();
      if (onRefresh) onRefresh();
    } catch (err: any) {
      alert('Execution failed: ' + (err.message || err));
    } finally {
      setLoading(false);
    }
  };

  const isLiveAllowed = statusData?.allowLiveSubmission === true;
  const isConfirmed = statusData?.state === 'CONFIRMED_SUBMISSION';

  return (
    <div className="space-y-4 text-xs">
      {/* Header Banner */}
      <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 font-bold text-sm text-white">
            <Shield className="w-4 h-4 text-sky-400" />
            Greenhouse Real ATS Controlled Execution Panel
          </div>
          <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-sky-500/10 border border-sky-500/30 text-sky-400">
            {statusData?.mode || 'PRODUCTION / READ-ONLY'}
          </span>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-slate-300">
          <div className="p-2 rounded-lg bg-slate-900/60 border border-slate-800">
            <div className="text-[10px] text-slate-500">PROVIDER</div>
            <div className="font-semibold text-emerald-400">{statusData?.provider || 'GREENHOUSE'}</div>
          </div>
          <div className="p-2 rounded-lg bg-slate-900/60 border border-slate-800">
            <div className="text-[10px] text-slate-500">STATE</div>
            <div className="font-semibold text-sky-400">{statusData?.state || 'READY_FOR_REVIEW'}</div>
          </div>
          <div className="p-2 rounded-lg bg-slate-900/60 border border-slate-800">
            <div className="text-[10px] text-slate-500">LIVE SUBMISSION</div>
            <div className={`font-semibold ${isLiveAllowed ? 'text-amber-400' : 'text-rose-400'}`}>
              {isLiveAllowed ? 'ENABLED' : 'DISABLED BY DEFAULT'}
            </div>
          </div>
          <div className="p-2 rounded-lg bg-slate-900/60 border border-slate-800">
            <div className="text-[10px] text-slate-500">SUBMISSION ATTEMPTED</div>
            <div className="font-semibold text-slate-300">
              {statusData?.submissionAttempted ? 'YES' : 'NO (DRY-RUN)'}
            </div>
          </div>
        </div>
      </div>

      {/* Safety Policy Flags */}
      <div className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-200 flex items-start gap-2.5">
        <Info className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
        <div className="space-y-1">
          <div className="font-semibold text-amber-300">Server Safety Invariants Active:</div>
          <div className="flex flex-wrap gap-3 font-mono text-[11px]">
            <span>AUTO_APPLY: <strong className="text-white">OFF</strong></span>
            <span>AUTO_SEND_EMAIL: <strong className="text-white">OFF</strong></span>
            <span>AUTO_LINKEDIN: <strong className="text-white">OFF</strong></span>
            <span>ALLOW_LIVE_SUBMISSION: <strong className="text-rose-400">OFF</strong></span>
          </div>
        </div>
      </div>

      {/* Step 1: Prepare Read-Only Discovery */}
      <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-3">
        <div className="flex items-center justify-between">
          <div className="font-semibold text-white flex items-center gap-1.5">
            <CheckCircle2 className="w-4 h-4 text-sky-400" />
            Step 1 & 2: Form Discovery & Preview
          </div>
          <button
            onClick={handlePrepare}
            disabled={loading}
            className="px-3 py-1.5 bg-slate-700 hover:bg-slate-600 text-white rounded-lg font-semibold flex items-center gap-1.5 transition"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            Run Form Inspection
          </button>
        </div>

        {prepareData && (
          <div className="space-y-2 text-slate-300 bg-slate-900/60 p-3 rounded-lg border border-slate-800">
            <div className="flex justify-between">
              <span>Target URL:</span>
              <span className="font-mono text-sky-400 truncate max-w-xs">{prepareData.targetUrl}</span>
            </div>
            <div className="flex justify-between">
              <span>Fields Detected:</span>
              <span className="font-mono text-emerald-400">{prepareData.fieldsDetected} detected / {prepareData.fieldsMapped} mapped</span>
            </div>
            <div className="flex justify-between">
              <span>Form Fingerprint:</span>
              <span className="font-mono text-slate-400">{prepareData.formFingerprint}</span>
            </div>
          </div>
        )}
      </div>

      {/* Step 3: Candidate Authorization & Confirmation */}
      <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-3">
        <div className="font-semibold text-white flex items-center gap-1.5">
          <UserCheck className="w-4 h-4 text-emerald-400" />
          Step 3: Explicit Candidate Authorization
        </div>

        <label className="flex items-start gap-2.5 cursor-pointer text-slate-300 bg-slate-900/40 p-3 rounded-lg border border-slate-800 hover:border-slate-700">
          <input
            type="checkbox"
            checked={candidateConfirmed}
            onChange={(e) => setCandidateConfirmed(e.target.checked)}
            className="mt-0.5 rounded border-slate-700 bg-slate-800 text-emerald-500 focus:ring-emerald-500"
          />
          <span>
            I have reviewed the application fields and target URL, and I explicitly authorize submission of this candidate application.
          </span>
        </label>

        <button
          onClick={handleConfirm}
          disabled={loading || !candidateConfirmed}
          className={`w-full py-2.5 rounded-xl font-bold flex items-center justify-center gap-2 transition ${
            candidateConfirmed
              ? 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm'
              : 'bg-slate-800 text-slate-500 cursor-not-allowed'
          }`}
        >
          <Lock className="w-4 h-4" />
          Confirm & Transition to CONFIRMED_SUBMISSION
        </button>
      </div>

      {/* Step 4: Final Execution Gate */}
      <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-3">
        <div className="font-semibold text-white flex items-center gap-1.5">
          <Play className="w-4 h-4 text-amber-400" />
          Final Live Execution Gate
        </div>

        {!isLiveAllowed && (
          <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs">
            <strong>Production execution is currently disabled.</strong>
            <p className="text-slate-400 mt-1">
              Enable live submission explicitly in server configuration (<code>app.execution.allow-live-submission=true</code>) before execution.
            </p>
          </div>
        )}

        <button
          onClick={handleExecute}
          disabled={loading || !isConfirmed}
          className={`w-full py-2.5 rounded-xl font-bold flex items-center justify-center gap-2 transition ${
            isConfirmed && isLiveAllowed
              ? 'bg-amber-600 hover:bg-amber-500 text-white'
              : 'bg-slate-800 text-slate-400 hover:bg-slate-700 border border-slate-700'
          }`}
        >
          <AlertOctagon className="w-4 h-4" />
          Execute Live Submission Gate
        </button>

        {executeResult && (
          <div className={`p-3 rounded-lg border text-xs space-y-1 ${
            executeResult.status === 'APPLIED'
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
              : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
          }`}>
            <div className="font-bold flex items-center gap-1.5">
              Status: {executeResult.status} ({executeResult.mode})
            </div>
            {executeResult.errorMessage && <div>Error: {executeResult.errorMessage}</div>}
            <div>Submission Attempted: {executeResult.submissionAttempted ? 'TRUE' : 'FALSE (BLOCKED)'}</div>
          </div>
        )}
      </div>
    </div>
  );
};
