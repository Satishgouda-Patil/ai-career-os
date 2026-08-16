import React, { useState } from 'react';
import { Video, BookOpen, MessageSquare, Award, ExternalLink, Sparkles } from 'lucide-react';
import { interviewApi } from '../services/api';

interface InterviewsPageProps {
  interviews: any[];
  onRefresh: () => void;
}

export const InterviewsPage: React.FC<InterviewsPageProps> = ({ interviews, onRefresh }) => {
  const [selectedInterview, setSelectedInterview] = useState<any>(null);
  const [prepData, setPrepData] = useState<any>(null);
  const [activeTab, setActiveTab] = useState<'prep' | 'mock'>('prep');

  const [mockCategory, setMockCategory] = useState('TECHNICAL');
  const [mockQuestion, setMockQuestion] = useState<any>(null);
  const [candidateAnswer, setCandidateAnswer] = useState('');
  const [evaluatedSession, setEvaluatedSession] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const handleOpenInterview = async (interview: any) => {
    setSelectedInterview(interview);
    setPrepData(null);
    setMockQuestion(null);
    setEvaluatedSession(null);

    try {
      const prep = await interviewApi.getPrep(interview.id);
      setPrepData(prep);
    } catch (err) {
      try {
        const generated = await interviewApi.generatePrep(interview.id);
        setPrepData(generated);
      } catch (e) {
        console.error(e);
      }
    }
  };

  const handleGenerateQuestion = async () => {
    if (!selectedInterview) return;
    setLoading(true);
    setEvaluatedSession(null);
    try {
      const q = await interviewApi.getMockQuestion(selectedInterview.id, mockCategory);
      setMockQuestion(q);
    } catch (err) {
      alert('Failed to generate mock question');
    } finally {
      setLoading(false);
    }
  };

  const handleEvaluateAnswer = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedInterview || !mockQuestion || !candidateAnswer.trim()) return;
    setLoading(true);
    try {
      const res = await interviewApi.evaluateMockAnswer(selectedInterview.id, mockQuestion.id, candidateAnswer);
      setEvaluatedSession(res);
    } catch (err) {
      alert('Failed to evaluate answer');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-white tracking-tight">Interview Intelligence Workspace</h2>
        <p className="text-sm text-slate-400">Personalized preparation kits, company intelligence, and AI mock interview practice</p>
      </div>

      {/* Interviews List */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {interviews.map((item) => (
          <div
            key={item.id}
            className="p-5 rounded-2xl border border-slate-800 bg-slate-900/60 hover:border-slate-700 transition flex flex-col justify-between space-y-4"
          >
            <div>
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-bold text-white text-base">{item.jobTitle || 'Software Position'}</h3>
                  <div className="text-sm text-sky-400 font-semibold">{item.companyName || 'Company'}</div>
                </div>

                <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-purple-500/10 border border-purple-500/20 text-purple-400">
                  {item.interviewType || 'TECHNICAL'}
                </span>
              </div>

              <div className="mt-3 space-y-1 text-xs text-slate-400">
                <div>Scheduled: {item.scheduledAt ? new Date(item.scheduledAt).toLocaleString() : 'Upcoming'}</div>
                {item.interviewerName && <div>Interviewer: {item.interviewerName} ({item.interviewerTitle})</div>}
              </div>
            </div>

            <div className="flex items-center justify-between pt-3 border-t border-slate-800">
              {item.meetingUrl ? (
                <a href={item.meetingUrl} target="_blank" rel="noreferrer" className="text-xs text-sky-400 hover:underline flex items-center gap-1">
                  <ExternalLink className="w-3.5 h-3.5" /> Meeting Link
                </a>
              ) : <span />}

              <button
                onClick={() => handleOpenInterview(item)}
                className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white text-xs font-medium transition shadow-sm"
              >
                <BookOpen className="w-3.5 h-3.5" /> Open Workspace
              </button>
            </div>
          </div>
        ))}

        {interviews.length === 0 && (
          <div className="p-12 text-center text-slate-400 text-sm border border-dashed border-slate-800 rounded-2xl md:col-span-2">
            No interviews scheduled yet. Ingesting email interview invites automatically provisions an interview workspace.
          </div>
        )}
      </div>

      {/* Workspace Modal */}
      {selectedInterview && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 w-full max-w-4xl rounded-2xl p-6 space-y-5 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h3 className="text-xl font-bold text-white">{selectedInterview.jobTitle} — Interview Preparation</h3>
                <div className="text-sm text-sky-400 font-semibold">{selectedInterview.companyName}</div>
              </div>
              <button onClick={() => setSelectedInterview(null)} className="text-slate-400 hover:text-white font-bold text-lg">✕</button>
            </div>

            {/* Mode Tabs */}
            <div className="flex border-b border-slate-800 gap-4 text-sm font-semibold">
              <button
                onClick={() => setActiveTab('prep')}
                className={`pb-2 border-b-2 transition ${activeTab === 'prep' ? 'border-purple-500 text-purple-400' : 'border-transparent text-slate-400 hover:text-slate-200'}`}
              >
                Preparation Kit
              </button>
              <button
                onClick={() => setActiveTab('mock')}
                className={`pb-2 border-b-2 transition ${activeTab === 'mock' ? 'border-purple-500 text-purple-400' : 'border-transparent text-slate-400 hover:text-slate-200'}`}
              >
                Mock Practice & AI Feedback
              </button>
            </div>

            {/* Tab 1: Preparation Kit */}
            {activeTab === 'prep' && (
              <div className="space-y-4 text-xs">
                <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-1">
                  <div className="font-bold text-sky-400 text-sm">Company Intelligence</div>
                  <p className="text-slate-300">{prepData?.companyOverview?.summary || 'Focus on company product architecture, scalability, and engineering culture.'}</p>
                </div>

                <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-2">
                  <div className="font-bold text-purple-400 text-sm">Role & Skill Focus Areas</div>
                  <ul className="list-disc list-inside text-slate-300 space-y-1">
                    {(prepData?.roleFocus?.requiredSkills || ['Java / Spring Boot', 'System Design', 'REST Microservices']).map((skill: string, i: number) => (
                      <li key={i}>{skill}</li>
                    ))}
                  </ul>
                </div>

                <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-2">
                  <div className="font-bold text-emerald-400 text-sm">Candidate Talking Points</div>
                  <p className="text-slate-300">{prepData?.candidateTalkingPoints?.strongestExperience || 'Highlight backend architecture, system design wins, and production system reliability.'}</p>
                </div>

                <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-2">
                  <div className="font-bold text-amber-400 text-sm">Strategic Questions to Ask Interviewer</div>
                  <ul className="list-disc list-inside text-slate-300 space-y-1">
                    {(prepData?.questionsToAsk || ['What does success look like in the first 90 days?', 'How does engineering collaboration work?']).map((q: string, i: number) => (
                      <li key={i}>{q}</li>
                    ))}
                  </ul>
                </div>
              </div>
            )}

            {/* Tab 2: Mock Interview Practice */}
            {activeTab === 'mock' && (
              <div className="space-y-4 text-xs">
                <div className="flex items-center gap-3">
                  <select
                    value={mockCategory}
                    onChange={(e) => setMockCategory(e.target.value)}
                    className="bg-slate-800 border border-slate-700 rounded-lg p-2 text-white focus:outline-none"
                  >
                    <option value="TECHNICAL">Technical Architecture</option>
                    <option value="BEHAVIORAL">Behavioral (STAR Method)</option>
                    <option value="ROLE_SPECIFIC">Role Specific</option>
                    <option value="COMPANY_SPECIFIC">Company Specific</option>
                  </select>

                  <button
                    onClick={handleGenerateQuestion}
                    disabled={loading}
                    className="flex items-center gap-1.5 px-3.5 py-2 bg-purple-600 hover:bg-purple-500 text-white rounded-lg font-medium"
                  >
                    <Sparkles className="w-3.5 h-3.5" /> Generate Question
                  </button>
                </div>

                {mockQuestion && (
                  <form onSubmit={handleEvaluateAnswer} className="p-4 rounded-xl bg-slate-800/50 border border-slate-800 space-y-3">
                    <div className="font-bold text-white text-sm">{mockQuestion.question}</div>

                    <textarea
                      rows={4}
                      placeholder="Type your response here..."
                      value={candidateAnswer}
                      onChange={(e) => setCandidateAnswer(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-700 rounded-lg p-3 text-slate-200 focus:outline-none focus:border-purple-500 text-xs"
                      required
                    />

                    <button
                      type="submit"
                      disabled={loading}
                      className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg font-semibold text-xs"
                    >
                      Submit Answer for AI Scoring
                    </button>
                  </form>
                )}

                {evaluatedSession && (
                  <div className="p-5 rounded-xl bg-slate-800/80 border border-purple-500/30 space-y-3">
                    <div className="flex items-center justify-between">
                      <div className="font-bold text-purple-300 text-sm flex items-center gap-2">
                        <Award className="w-4 h-4 text-amber-400" />
                        AI Evaluation Result
                      </div>
                      <div className="px-3 py-1 rounded-full bg-purple-500/20 text-purple-300 font-extrabold text-sm border border-purple-500/40">
                        Score: {evaluatedSession.score}/100
                      </div>
                    </div>

                    <div className="space-y-1">
                      <div className="font-semibold text-slate-300">Feedback:</div>
                      <p className="text-slate-300">{evaluatedSession.feedback}</p>
                    </div>

                    <div className="space-y-1">
                      <div className="font-semibold text-slate-300">Improved Response Template:</div>
                      <p className="text-slate-400 whitespace-pre-line leading-relaxed">{evaluatedSession.improvedAnswer}</p>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
