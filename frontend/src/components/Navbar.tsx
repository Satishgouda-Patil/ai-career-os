import React from 'react';
import { Bot, Bell, Shield, User as UserIcon } from 'lucide-react';

interface NavbarProps {
  userEmail?: string;
  onOpenAuth: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ userEmail, onOpenAuth }) => {
  return (
    <header className="h-16 border-b border-slate-800 bg-slate-900/90 backdrop-blur fixed top-0 left-0 right-0 z-40 px-6 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-sky-500/20">
          <Bot className="w-6 h-6 text-white" />
        </div>
        <div>
          <h1 className="text-lg font-bold text-white tracking-tight flex items-center gap-2">
            AI Career OS
            <span className="px-2 py-0.5 text-xs font-semibold bg-sky-500/10 text-sky-400 border border-sky-500/20 rounded-full">
              v1.0 Pro
            </span>
          </h1>
          <p className="text-xs text-slate-400">Autonomous Application & Career Execution Platform</p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <div className="hidden md:flex items-center gap-2 px-3 py-1.5 rounded-lg bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-medium">
          <Shield className="w-4 h-4" />
          Safety Locks ACTIVE (Auto-Apply: OFF)
        </div>

        <button className="p-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition relative">
          <Bell className="w-5 h-5" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-sky-500 animate-pulse" />
        </button>

        <button
          onClick={onOpenAuth}
          className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-800 border border-slate-700 text-slate-200 hover:bg-slate-700 transition text-sm font-medium"
        >
          <UserIcon className="w-4 h-4 text-sky-400" />
          <span>{userEmail ? userEmail : 'Sign In'}</span>
        </button>
      </div>
    </header>
  );
};
