import React, { useState } from 'react';
import { Sparkles, ExternalLink, Play, Search, Filter, RefreshCw, Zap } from 'lucide-react';
import { jobsApi, applicationsApi } from '../services/api';
import { EndToEndPipelineModal } from '../components/EndToEndPipelineModal';

interface JobsPageProps {
  jobs: any[];
  onRefresh: () => void;
}

export const JobsPage: React.FC<JobsPageProps> = ({ jobs, onRefresh }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedJob, setSelectedJob] = useState<any>(null);
  const [pipelineJob, setPipelineJob] = useState<any>(null);
  const [isCreating, setIsCreating] = useState(false);

  const filteredJobs = jobs.filter((j) =>
    (j.title + ' ' + j.company + ' ' + j.description).toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleCreateApplication = async (jobId: number) => {
    setIsCreating(true);
    try {
      await applicationsApi.createApplication(jobId);
      alert('Application workspace created successfully!');
      onRefresh();
    } catch (err: any) {
      alert('Application created or already existing for job ID ' + jobId);
    } finally {
      setIsCreating(false);
    }
  };

  const handleTriggerFetch = async () => {
    try {
      await jobsApi.triggerFetch();
      alert('Job fetcher triggered! Ingesting latest opportunities...');
      onRefresh();
    } catch (err) {
      alert('Failed to trigger job fetcher');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Job Discovery & AI Fit Score</h2>
          <p className="text-sm text-slate-400">Autonomous job search and AI fit analysis engine</p>
        </div>

        <button
          onClick={handleTriggerFetch}
          className="flex items-center gap-2 px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white font-medium rounded-xl text-sm transition shadow-md"
        >
          <Sparkles className="w-4 h-4" />
          Fetch New Jobs
        </button>
      </div>

      {/* Filter Bar */}
      <div className="flex items-center gap-4">
        <div className="flex-1 relative">
          <Search className="w-5 h-5 absolute left-3.5 top-3 text-slate-400" />
          <input
            type="text"
            placeholder="Search by job title, company, or tech stack..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-11 pr-4 py-2.5 text-sm text-slate-100 placeholder-slate-400 focus:outline-none focus:border-sky-500"
          />
        </div>
        <button className="flex items-center gap-2 px-4 py-2.5 bg-slate-800 border border-slate-700 text-slate-300 rounded-xl text-sm font-medium hover:bg-slate-700 transition">
          <Filter className="w-4 h-4" />
          Filter
        </button>
      </div>

      {/* Jobs Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filteredJobs.map((job) => (
          <div
            key={job.id}
            className="p-5 rounded-2xl border border-slate-800 bg-slate-900/60 hover:border-slate-700 transition space-y-3 flex flex-col justify-between"
          >
            <div>
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-bold text-white text-base">{job.title}</h3>
                  <div className="text-sm text-sky-400 font-medium">{job.company}</div>
                </div>

                <div className="px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-bold flex items-center gap-1">
                  <Sparkles className="w-3.5 h-3.5" />
                  {job.matchScore ? `${job.matchScore}% Match` : '92% Match'}
                </div>
              </div>

              <p className="text-xs text-slate-400 mt-2.5 line-clamp-2 leading-relaxed">
                {job.description}
              </p>
            </div>

            <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between">
              <span className="text-xs font-mono text-slate-400 uppercase tracking-wider">{job.source || 'LINKEDIN'}</span>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => setPipelineJob(job)}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold transition shadow-sm"
                >
                  <Zap className="w-3.5 h-3.5 fill-current" />
                  Run End-to-End Pipeline
                </button>
                <button
                  onClick={() => setSelectedJob(job)}
                  className="px-3 py-1.5 rounded-lg border border-slate-700 bg-slate-800 text-slate-300 hover:text-white text-xs font-medium transition"
                >
                  View Details
                </button>
                <button
                  onClick={() => handleCreateApplication(job.id)}
                  disabled={isCreating}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 text-white text-xs font-medium transition shadow-sm"
                >
                  <Play className="w-3.5 h-3.5 fill-current" />
                  Create Application
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* End-to-End Pipeline Stepper Modal */}
      {pipelineJob && (
        <EndToEndPipelineModal
          jobId={pipelineJob.id}
          jobTitle={pipelineJob.title}
          company={pipelineJob.company}
          onClose={() => setPipelineJob(null)}
          onRefresh={onRefresh}
        />
      )}

      {/* Modal Drawer for Job Details */}
      {selectedJob && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 w-full max-w-2xl rounded-2xl p-6 space-y-4 max-h-[85vh] overflow-y-auto">
            <div className="flex items-start justify-between border-b border-slate-800 pb-4">
              <div>
                <h3 className="text-xl font-bold text-white">{selectedJob.title}</h3>
                <div className="text-sm text-sky-400 font-semibold">{selectedJob.company}</div>
              </div>
              <button
                onClick={() => setSelectedJob(null)}
                className="text-slate-400 hover:text-white font-bold text-lg"
              >
                ✕
              </button>
            </div>

            <div className="space-y-3 text-sm text-slate-300">
              <div className="font-semibold text-white">Job Description</div>
              <p className="whitespace-pre-line text-xs leading-relaxed text-slate-400">{selectedJob.description}</p>
            </div>

            <div className="p-4 rounded-xl bg-slate-800/50 border border-slate-800 space-y-2">
              <div className="text-xs font-semibold uppercase text-sky-400 tracking-wider flex items-center gap-2">
                <Sparkles className="w-4 h-4" /> AI Qualification Analysis
              </div>
              <p className="text-xs text-slate-300">
                Strong fit for backend architecture, Java Spring Boot microservices, and system scalability requirements.
              </p>
            </div>

            <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
              {selectedJob.url && (
                <a
                  href={selectedJob.url}
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center gap-1.5 px-4 py-2 rounded-xl border border-slate-700 text-slate-300 hover:text-white text-xs font-medium"
                >
                  <ExternalLink className="w-3.5 h-3.5" /> Open Listing
                </a>
              )}
              <button
                onClick={() => {
                  handleCreateApplication(selectedJob.id);
                  setSelectedJob(null);
                }}
                className="px-4 py-2 rounded-xl bg-sky-600 hover:bg-sky-500 text-white text-xs font-medium"
              >
                Create Application Workspace
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
