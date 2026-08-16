import React, { useState } from 'react';
import { Shield, Lock, Bell, Cpu, Save } from 'lucide-react';

interface SettingsPageProps {
  userEmail?: string;
}

export const SettingsPage: React.FC<SettingsPageProps> = ({ userEmail }) => {
  const [autoApply, setAutoApply] = useState(false);
  const [autoSendEmail, setAutoSendEmail] = useState(false);
  const [autoFollowUp, setAutoFollowUp] = useState(false);
  const [minMatchScore, setMinMatchScore] = useState(80);

  const handleSave = () => {
    alert('Safety settings & automation policies saved!');
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white tracking-tight">Settings & Safety Policy</h2>
        <p className="text-sm text-slate-400">Configure safety locks, automation permissions, and security parameters</p>
      </div>

      {/* Safety Locks Panel */}
      <div className="p-6 rounded-2xl border border-slate-800 bg-slate-900/60 backdrop-blur space-y-5">
        <h3 className="text-lg font-bold text-white flex items-center gap-2">
          <Shield className="w-5 h-5 text-emerald-400" />
          Automation Safety Controls (Phase 5 Conservative Policy)
        </h3>

        <div className="space-y-4 text-xs">
          <div className="flex items-center justify-between p-4 rounded-xl bg-slate-800/40 border border-slate-800">
            <div>
              <div className="font-semibold text-white text-sm">AUTO_APPLY</div>
              <div className="text-slate-400">Automatically submit job applications without explicit candidate approval</div>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" checked={autoApply} onChange={(e) => setAutoApply(e.target.checked)} className="sr-only peer" />
              <div className="w-11 h-6 bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
            </label>
          </div>

          <div className="flex items-center justify-between p-4 rounded-xl bg-slate-800/40 border border-slate-800">
            <div>
              <div className="font-semibold text-white text-sm">AUTO_SEND_EMAIL</div>
              <div className="text-slate-400">Automatically send recruiter outreach emails without candidate review</div>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" checked={autoSendEmail} onChange={(e) => setAutoSendEmail(e.target.checked)} className="sr-only peer" />
              <div className="w-11 h-6 bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
            </label>
          </div>

          <div className="flex items-center justify-between p-4 rounded-xl bg-slate-800/40 border border-slate-800">
            <div>
              <div className="font-semibold text-white text-sm">AUTO_FOLLOW_UP</div>
              <div className="text-slate-400">Automatically dispatch scheduled follow-up emails post-submission</div>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" checked={autoFollowUp} onChange={(e) => setAutoFollowUp(e.target.checked)} className="sr-only peer" />
              <div className="w-11 h-6 bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
            </label>
          </div>
        </div>
      </div>

      {/* Preferences */}
      <div className="p-6 rounded-2xl border border-slate-800 bg-slate-900/60 backdrop-blur space-y-4">
        <h3 className="text-lg font-bold text-white flex items-center gap-2">
          <Cpu className="w-5 h-5 text-sky-400" />
          Discovery & Qualification Thresholds
        </h3>

        <div className="space-y-3 text-xs">
          <div>
            <label className="block font-semibold text-slate-300 mb-1">Minimum Job Match Score Target: {minMatchScore}%</label>
            <input
              type="range"
              min="50"
              max="95"
              value={minMatchScore}
              onChange={(e) => setMinMatchScore(Number(e.target.value))}
              className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer"
            />
          </div>
        </div>
      </div>

      <div className="flex justify-end">
        <button
          onClick={handleSave}
          className="flex items-center gap-2 px-6 py-2.5 bg-sky-600 hover:bg-sky-500 text-white font-medium rounded-xl text-sm transition shadow-md"
        >
          <Save className="w-4 h-4" /> Save Preferences
        </button>
      </div>
    </div>
  );
};
