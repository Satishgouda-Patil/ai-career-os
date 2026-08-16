import React from 'react';
import {
  LayoutDashboard,
  Briefcase,
  Layers,
  Mail,
  Clock,
  Video,
  Activity,
  Settings,
} from 'lucide-react';

export type NavTab = 'dashboard' | 'jobs' | 'applications' | 'emails' | 'followups' | 'interviews' | 'control-center' | 'settings';

interface SidebarProps {
  activeTab: NavTab;
  setActiveTab: (tab: NavTab) => void;
  reviewRequiredCount?: number;
  followUpsDueCount?: number;
  upcomingInterviewsCount?: number;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  setActiveTab,
  reviewRequiredCount = 0,
  followUpsDueCount = 0,
  upcomingInterviewsCount = 0,
}) => {
  const menuItems = [
    { id: 'dashboard' as NavTab, label: 'Dashboard', icon: LayoutDashboard },
    { id: 'jobs' as NavTab, label: 'Job Discovery', icon: Briefcase },
    {
      id: 'applications' as NavTab,
      label: 'Applications',
      icon: Layers,
      badge: reviewRequiredCount > 0 ? `${reviewRequiredCount} review` : undefined,
      badgeColor: 'bg-amber-500/20 text-amber-300 border-amber-500/30'
    },
    { id: 'emails' as NavTab, label: 'Email Intelligence', icon: Mail },
    {
      id: 'followups' as NavTab,
      label: 'Follow-ups',
      icon: Clock,
      badge: followUpsDueCount > 0 ? `${followUpsDueCount} due` : undefined,
      badgeColor: 'bg-sky-500/20 text-sky-300 border-sky-500/30'
    },
    {
      id: 'interviews' as NavTab,
      label: 'Interview Prep',
      icon: Video,
      badge: upcomingInterviewsCount > 0 ? `${upcomingInterviewsCount}` : undefined,
      badgeColor: 'bg-purple-500/20 text-purple-300 border-purple-500/30'
    },
    { id: 'control-center' as NavTab, label: 'Control Center', icon: Activity },
    { id: 'settings' as NavTab, label: 'Settings & Safety', icon: Settings },
  ];

  return (
    <aside className="w-64 fixed left-0 top-16 bottom-0 border-r border-slate-800 bg-slate-900/60 p-4 flex flex-col justify-between">
      <nav className="space-y-1">
        <div className="px-3 py-2 text-xs font-semibold uppercase text-slate-400 tracking-wider">
          Navigation
        </div>
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl font-medium text-sm transition ${
                isActive
                  ? 'bg-sky-500/10 text-sky-400 border border-sky-500/20 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <div className="flex items-center gap-3">
                <Icon className={`w-4 h-4 ${isActive ? 'text-sky-400' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </div>
              {item.badge && (
                <span className={`text-xs font-semibold px-2 py-0.5 rounded-full border ${item.badgeColor}`}>
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      <div className="p-3 rounded-xl bg-slate-800/40 border border-slate-800 text-xs text-slate-400 space-y-1">
        <div className="font-semibold text-slate-300">Phase 5 Production Mode</div>
        <div>Spring Boot 3 + React + Vite</div>
        <div className="text-emerald-400 font-mono">Backend: http://localhost:8080</div>
      </div>
    </aside>
  );
};
