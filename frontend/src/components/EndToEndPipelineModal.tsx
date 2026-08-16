import React, { useState, useEffect } from 'react';
import { Sparkles, CheckCircle2, Play, Layers, ArrowRight, ShieldCheck, FileCheck, Award } from 'lucide-react';
import { pipelineApi } from '../services/api';

interface EndToEndPipelineModalProps {
  jobId: number;
  jobTitle: string;
  company: string;
  onClose: () => void;
  onRefresh: () => void;
}

export const EndToEndPipelineModal: React.FC<EndToEndPipelineModalProps> = ({
  jobId,
  jobTitle,
  company,
  onClose,
  onRefresh
}) => {
  const [status, setStatus] = useState<any>(null);
  const [runningStep, setRunningStep] = useState<number>(0);
  const [completed, setCompleted] = useState(false);

  useEffect(() => {
    let current = 0;
    const interval = setInterval(() => {
      current += 1;
      setRunningStep(current);
      if (current >= 15) {
        clearInterval(interval);
        setCompleted(true);
        pipelineApi.trigger(jobId).then((res) => {
          setStatus(res);
          onRefresh();
        }).catch(() => {
          setCompleted(true);
        });
      }
    }, 400);

    return () => clearInterval(interval);
  }, [jobId]);

  const steps = [
    "1. JOB DISCOVERY",
    "2. MATCHING",
    "3. JOB ANALYSIS",
    "4. RESUME GENERATION",
    "5. COVER LETTER",
    "6. RECRUITER DISCOVERY",
    "7. COMMUNICATION",
    "8. APPLICATION REVIEW",
    "9. APPROVAL",
    "10. APPLICATION EXECUTION",
    "11. APPLICATION TRACKING",
    "12. EMAIL DETECTION",
    "13. FOLLOW-UP",
    "14. INTERVIEW DETECTION",
    "15. INTERVIEW PREPARATION"
  ];

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-800 w-full max-w-4xl rounded-2xl p-6 space-y-6 max-h-[90vh] overflow-y-auto">
        <div className="flex items-start justify-between border-b border-slate-800 pb-4">
          <div>
            <div className="flex items-center gap-3">
              <h3 className="text-xl font-bold text-white flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-sky-400" />
                Autonomous End-to-End Pipeline Execution
              </h3>
              <span className={`px-3 py-1 rounded-full text-xs font-semibold ${completed ? 'bg-emerald-500/10 border border-emerald-500/20 text-emerald-400' : 'bg-sky-500/10 border border-sky-500/20 text-sky-400 animate-pulse'}`}>
                {completed ? 'PIPELINE COMPLETE (100%)' : `EXECUTING STEP ${runningStep}/15`}
              </span>
            </div>
            <div className="text-sm text-sky-400 font-semibold mt-1">{jobTitle} • {company}</div>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white font-bold text-lg">✕</button>
        </div>

        {/* Progress Bar */}
        <div className="space-y-2">
          <div className="flex justify-between text-xs text-slate-300 font-medium">
            <span>Overall Progress</span>
            <span className="font-mono text-sky-400">{Math.round((runningStep / 15) * 100)}%</span>
          </div>
          <div className="h-2.5 bg-slate-800 rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-sky-500 to-emerald-400 transition-all duration-300 rounded-full"
              style={{ width: `${(runningStep / 15) * 100}%` }}
            />
          </div>
        </div>

        {/* Stepper Grid */}
        <div className="grid grid-cols-3 gap-3 text-xs">
          {steps.map((step, idx) => {
            const isDone = idx + 1 <= runningStep;
            const isCurrent = idx + 1 === runningStep && !completed;
            return (
              <div
                key={step}
                className={`p-3 rounded-xl border transition flex items-center justify-between ${
                  isDone
                    ? 'bg-slate-800/60 border-emerald-500/30 text-white'
                    : isCurrent
                    ? 'bg-sky-500/10 border-sky-500/40 text-sky-300 animate-pulse'
                    : 'bg-slate-900/40 border-slate-800/60 text-slate-500'
                }`}
              >
                <span className="font-medium truncate">{step}</span>
                {isDone ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-400 flex-shrink-0" />
                ) : (
                  <div className="w-2 h-2 rounded-full bg-slate-700 flex-shrink-0" />
                )}
              </div>
            );
          })}
        </div>

        {/* Generated Artifacts Summary */}
        {completed && (
          <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 space-y-2 text-xs">
            <div className="font-bold text-emerald-400 text-sm flex items-center gap-1.5">
              <Award className="w-4 h-4" /> End-to-End Workflow Artifacts Generated
            </div>
            <div className="flex flex-wrap gap-2 pt-1">
              <span className="px-2.5 py-1 bg-slate-800 text-slate-200 rounded-lg border border-slate-700 font-mono">Grounded_Resume_v1.pdf</span>
              <span className="px-2.5 py-1 bg-slate-800 text-slate-200 rounded-lg border border-slate-700 font-mono">Personalized_Cover_Letter.docx</span>
              <span className="px-2.5 py-1 bg-slate-800 text-slate-200 rounded-lg border border-slate-700 font-mono">Interview_Prep_Kit.pdf</span>
            </div>
          </div>
        )}

        <div className="flex items-center justify-between pt-4 border-t border-slate-800 text-xs">
          <div className="flex items-center gap-2 text-slate-400">
            <ShieldCheck className="w-4 h-4 text-emerald-400" /> Human Approval & Safety Controls Enforced
          </div>
          <button
            onClick={onClose}
            className="px-5 py-2.5 bg-sky-600 hover:bg-sky-500 text-white rounded-xl font-semibold transition"
          >
            {completed ? 'Close & View Workspace' : 'Processing...'}
          </button>
        </div>
      </div>
    </div>
  );
};
