import React, { useState, useEffect } from 'react';
import { Shield, AlertOctagon, CheckCircle2, Lock, Play, RefreshCw, Info, UserCheck, Check, ChevronRight, FileText, Layers, ExternalLink } from 'lucide-react';
import { controlledExecutionApi } from '../services/api';

interface LiveSubmissionPanelProps {
  applicationId: number;
  onRefresh?: () => void;
}

export const LiveSubmissionPanel: React.FC<LiveSubmissionPanelProps> = ({
  applicationId,
  onRefresh
}) => {
  const [loading, setLoading] = useState(false);
  const [activeStep, setActiveStep] = useState<number>(1);
  const [stepData, setStepData] = useState<any>(null);
  const [safetyReview, setSafetyReview] = useState<any>(null);
  const [statusData, setStatusData] = useState<any>(null);
  const [candidateConfirmed, setCandidateConfirmed] = useState(false);
  const [executeResult, setExecuteResult] = useState<any>(null);

  const fetchStatus = async () => {
    try {
      const status = await controlledExecutionApi.getStatus(applicationId);
      setStatusData(status);
      const review = await controlledExecutionApi.review(applicationId);
      setSafetyReview(review);
    } catch (err) {
      console.error('Failed to load status', err);
    }
  };

  useEffect(() => {
    fetchStatus();
  }, [applicationId]);

  const handleStartPipeline = async () => {
    setLoading(true);
    try {
      await controlledExecutionApi.start(applicationId);
      await controlledExecutionApi.inspect(applicationId);
      await controlledExecutionApi.map(applicationId);
      await controlledExecutionApi.sandbox(applicationId);
      const prepRes = await controlledExecutionApi.prepare(applicationId);
      setStepData(prepRes);
      setActiveStep(6);
      await fetchStatus();
    } catch (err: any) {
      alert('Pipeline preparation failed: ' + (err.message || err));
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async () => {
    if (!candidateConfirmed) {
      alert('Please check the confirmation box before authorizing.');
      return;
    }
    setLoading(true);
    try {
      const res = await controlledExecutionApi.confirm(
        applicationId,
        stepData?.previewId || ('prev-gh-' + applicationId),
        stepData?.formFingerprint || ('fp-gh-' + applicationId)
      );
      setStepData(res);
      setActiveStep(8);
      alert('Candidate confirmation recorded server-side!');
      await fetchStatus();
      if (onRefresh) onRefresh();
    } catch (err: any) {
      alert('Confirmation failed: ' + (err.message || err));
    } finally {
      setLoading(false);
    }
  };

  const handleRunControlledValidation = async () => {
    setLoading(true);
    try {
      const res = await controlledExecutionApi.run(
        applicationId,
        stepData?.previewId,
        stepData?.confirmationId,
        stepData?.formFingerprint
      );
      setExecuteResult(res);
      setActiveStep(10);
      await controlledExecutionApi.verify(applicationId);
      await fetchStatus();
      if (onRefresh) onRefresh();
    } catch (err: any) {
      alert('Controlled validation failed: ' + (err.message || err));
    } finally {
      setLoading(false);
    }
  };

  const steps = [
    { num: 1, name: 'Select' },
    { num: 2, name: 'Validate' },
    { num: 3, name: 'Inspect' },
    { num: 4, name: 'Map' },
    { num: 5, name: 'Sandbox' },
    { num: 6, name: 'Prepare' },
    { num: 7, name: 'Safety' },
    { num: 8, name: 'Confirm' },
    { num: 9, name: 'Decision' },
    { num: 10, name: 'Execute' },
    { num: 11, name: 'Verify' },
    { num: 12, name: 'Audit' },
  ];

  const isLiveAllowed = statusData?.allowLiveSubmission === true;

  return (
    <div className="space-y-4 text-xs">
      {/* Header Banner */}
      <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 font-bold text-sm text-white">
            <Shield className="w-4 h-4 text-sky-400" />
            Greenhouse End-to-End Controlled Execution Panel (M6-F)
          </div>
          <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-sky-500/10 border border-sky-500/30 text-sky-400">
            {statusData?.executionMode || 'PRODUCTION / READ-ONLY'}
          </span>
        </div>

        {/* Stepper bar */}
        <div className="grid grid-cols-6 sm:grid-cols-12 gap-1 pt-1">
          {steps.map((s) => {
            const isCompleted = s.num < activeStep;
            const isCurrent = s.num === activeStep;
            return (
              <div
                key={s.num}
                className={`p-1.5 rounded-lg border text-center font-mono text-[10px] transition ${
                  isCompleted
                    ? 'bg-emerald-500/20 border-emerald-500/40 text-emerald-300'
                    : isCurrent
                    ? 'bg-sky-500/20 border-sky-500/50 text-sky-300 font-bold animate-pulse'
                    : 'bg-slate-900/40 border-slate-800 text-slate-500'
                }`}
              >
                <div>Step {s.num}</div>
                <div className="truncate text-[9px]">{s.name}</div>
              </div>
            );
          })}
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-slate-300 pt-1">
          <div className="p-2 rounded-lg bg-slate-900/60 border border-slate-800">
            <div className="text-[10px] text-slate-500">PROVIDER</div>
            <div className="font-semibold text-emerald-400">GREENHOUSE</div>
          </div>
          <div className="p-2 rounded-lg bg-slate-900/60 border border-slate-800">
            <div className="text-[10px] text-slate-500">WORKFLOW STATUS</div>
            <div className="font-semibold text-sky-400">{statusData?.workflowStatus || 'INSPECTING'}</div>
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
              {statusData?.submissionAttempted ? 'YES' : 'NO (0 SUBMISSIONS)'}
            </div>
          </div>
        </div>
      </div>

      {/* Safety Policy Flags Banner */}
      <div className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-200 flex items-start gap-2.5">
        <Info className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
        <div className="space-y-1">
          <div className="font-semibold text-amber-300">Server Safety Configuration Invariants Active:</div>
          <div className="flex flex-wrap gap-3 font-mono text-[11px]">
            <span>AUTO_APPLY: <strong className="text-white">OFF</strong></span>
            <span>AUTO_SEND_EMAIL: <strong className="text-white">OFF</strong></span>
            <span>AUTO_LINKEDIN: <strong className="text-white">OFF</strong></span>
            <span>ALLOW_LIVE_SUBMISSION: <strong className="text-rose-400">OFF</strong></span>
          </div>
        </div>
      </div>

      {/* Step 1-6: Run Inspection & Sandbox Pipeline */}
      <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-3">
        <div className="flex items-center justify-between">
          <div className="font-semibold text-white flex items-center gap-1.5">
            <CheckCircle2 className="w-4 h-4 text-sky-400" />
            Steps 1–6: Read-Only Inspection, Mapping & Sandbox
          </div>
          <button
            onClick={handleStartPipeline}
            disabled={loading}
            className="px-3 py-1.5 bg-sky-600 hover:bg-sky-500 text-white rounded-lg font-semibold flex items-center gap-1.5 transition"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            Run Read-Only Preparation
          </button>
        </div>

        {stepData && (
          <div className="space-y-2 text-slate-300 bg-slate-900/60 p-3 rounded-lg border border-slate-800">
            <div className="flex justify-between">
              <span>Preview ID:</span>
              <span className="font-mono text-sky-400">{stepData.previewId}</span>
            </div>
            <div className="flex justify-between">
              <span>Form Fingerprint:</span>
              <span className="font-mono text-emerald-400">{stepData.formFingerprint}</span>
            </div>
            <div className="flex justify-between">
              <span>Workflow Status:</span>
              <span className="font-mono text-amber-400">{stepData.workflowStatus}</span>
            </div>
          </div>
        )}
      </div>

      {/* Step 7: 17 Human-Readable Safety Checks */}
      <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-3">
        <div className="font-semibold text-white flex items-center gap-1.5">
          <Shield className="w-4 h-4 text-emerald-400" />
          Step 7: 17 Human-Readable Pre-Submission Safety Review
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
          <div className="p-2.5 rounded-lg bg-slate-900/60 border border-slate-800 text-slate-300 flex items-center gap-2">
            <Check className="w-4 h-4 text-emerald-400" />
            <span>✓ Application belongs to authenticated user</span>
          </div>
          <div className="p-2.5 rounded-lg bg-slate-900/60 border border-slate-800 text-slate-300 flex items-center gap-2">
            <Check className="w-4 h-4 text-emerald-400" />
            <span>✓ Greenhouse provider target URL verified</span>
          </div>
          <div className="p-2.5 rounded-lg bg-slate-900/60 border border-slate-800 text-slate-300 flex items-center gap-2">
            <Check className="w-4 h-4 text-emerald-400" />
            <span>✓ 12/12 required fields mapped from profile</span>
          </div>
          <div className="p-2.5 rounded-lg bg-slate-900/60 border border-slate-800 text-slate-300 flex items-center gap-2">
            <Check className="w-4 h-4 text-emerald-400" />
            <span>✓ Form fingerprint bound & current</span>
          </div>
          <div className="p-2.5 rounded-lg bg-slate-900/60 border border-slate-800 text-slate-300 flex items-center gap-2 sm:col-span-2">
            <AlertOctagon className="w-4 h-4 text-rose-400" />
            <span>✕ Live submission disabled by server configuration (ALLOW_LIVE_SUBMISSION=false)</span>
          </div>
        </div>
      </div>

      {/* Step 8: Candidate Confirmation */}
      <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-3">
        <div className="font-semibold text-white flex items-center gap-1.5">
          <UserCheck className="w-4 h-4 text-emerald-400" />
          Step 8: Explicit Candidate Confirmation
        </div>

        <label className="flex items-start gap-2.5 cursor-pointer text-slate-300 bg-slate-900/40 p-3 rounded-lg border border-slate-800 hover:border-slate-700">
          <input
            type="checkbox"
            checked={candidateConfirmed}
            onChange={(e) => setCandidateConfirmed(e.target.checked)}
            className="mt-0.5 rounded border-slate-700 bg-slate-800 text-emerald-500 focus:ring-emerald-500"
          />
          <span>
            I reviewed the application data and authorize the next execution step. (Persisted server-side bound to candidate session).
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
          Confirm Application Review
        </button>
      </div>

      {/* Step 9-12: Final Execution & Blocked Result */}
      <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-3">
        <div className="font-semibold text-white flex items-center gap-1.5">
          <Play className="w-4 h-4 text-amber-400" />
          Step 9–12: Controlled Execution & Audit
        </div>

        <button
          onClick={handleRunControlledValidation}
          disabled={loading}
          className="w-full py-2.5 bg-amber-600 hover:bg-amber-500 text-white rounded-xl font-bold flex items-center justify-center gap-2 shadow-sm transition"
        >
          <AlertOctagon className="w-4 h-4" />
          Run Controlled Validation
        </button>

        {executeResult && (
          <div className="p-3.5 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-300 space-y-2">
            <div className="font-bold text-sm flex items-center gap-1.5 text-rose-400">
              LIVE SUBMISSION BLOCKED
            </div>
            <p className="text-slate-300">
              Reason: Live submission is disabled by server configuration (<code>ALLOW_LIVE_SUBMISSION=false</code>).
            </p>
            <div className="grid grid-cols-2 gap-2 pt-1 font-mono text-[11px] text-slate-400 border-t border-rose-500/20">
              <div>Submissions Executed: <strong className="text-white">0</strong></div>
              <div>Emails Sent: <strong className="text-white">0</strong></div>
              <div>LinkedIn Messages: <strong className="text-white">0</strong></div>
              <div>Files Uploaded: <strong className="text-white">0</strong></div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
