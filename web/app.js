/* CodeReview AI - browser UI. Talks to the Java servlets under /api. */
(() => {
  'use strict';

  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];
  const view = $('#view');
  const state = { me: null, reviewer: '', editor: null, drafts: {}, route: 0 };
  // Ignore results from a page the user already navigated away from.
  const stale = (token) => token !== state.route;

  const TEMPLATE = `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // Read the input, solve the problem, print the answer.

        sc.close();
    }
}
`;

  // ---------------------------------------------------------------- helpers
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g,
    (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const pct = (n) => `${Math.round(n || 0)}%`;
  const title = (s) => String(s || '').charAt(0) + String(s || '').slice(1).toLowerCase();
  const statusClass = (s) => ({
    'WRONG ANSWER': 'WRONG', 'RUNTIME ERROR': 'ERROR', 'TIME LIMIT EXCEEDED': 'TLE',
    'COMPILATION ERROR': 'COMPILE'
  }[s] || s);
  const pill = (text, cls) => `<span class="pill ${esc(cls ?? statusClass(text))}">${esc(text)}</span>`;
  const when = (iso) => {
    const d = new Date(iso);
    return isNaN(d) ? '' : d.toLocaleString(undefined, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
  };

  async function api(path, options = {}) {
    const init = { method: options.method || 'GET', headers: {}, credentials: 'same-origin' };
    if (options.body !== undefined) {
      init.headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(options.body);
    }
    const res = await fetch('/api' + path, init);
    let data = null;
    try { data = await res.json(); } catch (_) { /* empty body */ }
    if (res.status === 401 && path !== '/auth/login') {
      showAuth();
      throw new Error('Please log in.');
    }
    if (!res.ok) throw new Error((data && data.error) || `Request failed (${res.status})`);
    return data;
  }

  function toast(message) {
    const t = $('#toast');
    t.textContent = message;
    t.classList.remove('hidden');
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => t.classList.add('hidden'), 3200);
  }

  function modal(html) {
    $('#modal-body').innerHTML = html;
    $('#modal').classList.remove('hidden');
  }
  $('#modal').addEventListener('click', (e) => {
    if (e.target.id === 'modal' || e.target.classList.contains('modal-close')) $('#modal').classList.add('hidden');
  });
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') $('#modal').classList.add('hidden'); });

  // First lines of a Java stack trace are enough to see what went wrong.
  const shortError = (e) => String(e || '').split('\n').slice(0, 3).join('\n');

  // Small Markdown renderer for AI feedback: headings, lists, tables, code blocks,
  // **bold**, *italic* and `code`. Everything is escaped first, so it is safe.
  function inline(t) {
    return t
      .replace(/`([^`\n]+)`/g, '<code>$1</code>')
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/(^|[\s(])\*([^*\n]+)\*(?=[\s).,!?:;]|$)/g, '$1<em>$2</em>');
  }
  function formatFeedback(text) {
    const lines = esc(text || '').replace(/\r/g, '').split('\n');
    const out = [];
    let i = 0;
    const isTableRow = (l) => /^\s*\|.*\|\s*$/.test(l);
    const cells = (l) => l.trim().replace(/^\||\|$/g, '').split('|').map((c) => inline(c.trim()));
    while (i < lines.length) {
      const line = lines[i];
      if (/^\s*```/.test(line)) {
        const code = [];
        i++;
        while (i < lines.length && !/^\s*```/.test(lines[i])) code.push(lines[i++]);
        i++;
        out.push(`<pre><code>${code.join('\n')}</code></pre>`);
      } else if (/^\s*#{1,6}\s+/.test(line)) {
        out.push(`<h4>${inline(line.replace(/^\s*#{1,6}\s+/, ''))}</h4>`); i++;
      } else if (/^\s*(-{3,}|\*{3,}|_{3,})\s*$/.test(line)) {
        out.push('<hr>'); i++;
      } else if (isTableRow(line) && i + 1 < lines.length && /^\s*\|?\s*:?-{2,}/.test(lines[i + 1])) {
        const head = cells(line); i += 2;
        const rows = [];
        while (i < lines.length && isTableRow(lines[i])) rows.push(cells(lines[i++]));
        out.push(`<table><thead><tr>${head.map((c) => `<th>${c}</th>`).join('')}</tr></thead><tbody>${
          rows.map((r) => `<tr>${r.map((c) => `<td>${c}</td>`).join('')}</tr>`).join('')}</tbody></table>`);
      } else if (/^\s*([*+-]|\d+[.)])\s+/.test(line)) {
        const ordered = /^\s*\d/.test(line);
        const items = [];
        while (i < lines.length && /^\s*([*+-]|\d+[.)])\s+/.test(lines[i])) {
          items.push(inline(lines[i].replace(/^\s*([*+-]|\d+[.)])\s+/, ''))); i++;
        }
        const tag = ordered ? 'ol' : 'ul';
        out.push(`<${tag}>${items.map((t) => `<li>${t}</li>`).join('')}</${tag}>`);
      } else if (!line.trim()) {
        i++;
      } else {
        const para = [];
        while (i < lines.length && lines[i].trim() && !/^\s*(```|#{1,6}\s|([*+-]|\d+[.)])\s|(-{3,}|\*{3,}|_{3,})\s*$)/.test(lines[i]) && !isTableRow(lines[i])) {
          para.push(inline(lines[i++]));
        }
        if (!para.length) { para.push(inline(lines[i++])); }
        out.push(`<p>${para.join('<br>')}</p>`);
      }
    }
    return out.join('');
  }

  // ---------------------------------------------------------------- theme
  const media = window.matchMedia('(prefers-color-scheme: dark)');
  function applyTheme(choice) {
    const dark = choice === 'dark' || (choice === 'system' && media.matches);
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    $$('[data-theme-set]').forEach((b) => {
      const on = b.dataset.themeSet === choice;
      b.classList.toggle('active', on);
      b.setAttribute('aria-checked', on ? 'true' : 'false');
      b.setAttribute('role', 'radio');
    });
  }
  const themeChoice = () => localStorage.getItem('theme') || 'system';
  $$('[data-theme-set]').forEach((b) => b.addEventListener('click', () => {
    localStorage.setItem('theme', b.dataset.themeSet);
    applyTheme(b.dataset.themeSet);
    if (state.editor) state.editor.refresh();
  }));
  media.addEventListener('change', () => { if (themeChoice() === 'system') applyTheme('system'); });
  applyTheme(themeChoice());

  // ---------------------------------------------------------------- auth (Member 1)
  function showAuth() {
    state.me = null;
    $('#app').classList.add('hidden');
    $('#auth').classList.remove('hidden');
  }

  function showApp() {
    $('#auth').classList.add('hidden');
    $('#app').classList.remove('hidden');
    $('#me-name').textContent = state.me.name;
    $('#me-email').textContent = state.me.email;
    $('#me-avatar').textContent = state.me.name.trim().charAt(0).toUpperCase();
    $$('.admin-only').forEach((el) => el.classList.toggle('hidden', !state.me.admin));
    route();
  }

  $$('[data-auth-tab]').forEach((tab) => tab.addEventListener('click', () => {
    $$('[data-auth-tab]').forEach((t) => t.classList.toggle('active', t === tab));
    $('#login-form').classList.toggle('hidden', tab.dataset.authTab !== 'login');
    $('#register-form').classList.toggle('hidden', tab.dataset.authTab !== 'register');
  }));

  async function submitAuth(form, path) {
    const err = $('[data-error]', form);
    const button = $('button[type=submit]', form);
    err.textContent = '';
    button.disabled = true;
    try {
      const body = Object.fromEntries(new FormData(form));
      const data = await api(path, { method: 'POST', body });
      state.me = data.student;
      form.reset();
      if (!location.hash || location.hash === '#/') location.hash = '#/dashboard';
      showApp();
    } catch (e) {
      err.textContent = e.message;
    } finally {
      button.disabled = false;
    }
  }
  $('#login-form').addEventListener('submit', (e) => { e.preventDefault(); submitAuth(e.target, '/auth/login'); });
  $('#register-form').addEventListener('submit', (e) => { e.preventDefault(); submitAuth(e.target, '/auth/register'); });
  $('#demo-login').addEventListener('click', () => {
    const form = $('#login-form');
    form.email.value = 'demo@codereview.app';
    form.password.value = 'demo1234';
    submitAuth(form, '/auth/login');
  });
  $('#logout').addEventListener('click', async () => {
    try { await api('/auth/logout', { method: 'POST', body: {} }); } catch (_) { /* ignore */ }
    location.hash = '';
    showAuth();
  });

  // ---------------------------------------------------------------- router
  const routes = {
    dashboard: renderDashboard,
    problems: renderProblems,
    problem: renderSolve,
    history: renderHistory,
    leaderboard: renderLeaderboard,
    manage: renderManage,
    about: renderAbout,
  };

  function route() {
    if (!state.me) return;
    const [name, id] = location.hash.replace(/^#\//, '').split('/');
    const page = routes[name] ? name : 'dashboard';
    $$('[data-nav]').forEach((a) => a.classList.toggle('active',
      a.dataset.nav === page || (page === 'problem' && a.dataset.nav === 'problems')));
    state.editor = null;
    view.innerHTML = '<div class="empty">Loading…</div>';
    const token = ++state.route;
    routes[page](id, token).catch((e) => { if (!stale(token)) view.innerHTML = `<div class="card pad empty">${esc(e.message)}</div>`; });
    window.scrollTo(0, 0);
  }
  window.addEventListener('hashchange', route);

  // ---------------------------------------------------------------- dashboard (Member 5)
  async function renderDashboard(_, token) {
    const [d, recent] = await Promise.all([api('/stats/dashboard'), api('/submissions')]);
    if (stale(token)) return;
    const solvedPct = d.totalProblems ? (d.solved / d.totalProblems) * 100 : 0;
    view.innerHTML = `
      <div class="page-head">
        <div><h2>Welcome back, ${esc(state.me.name.split(' ')[0])}</h2>
        <p>Here's how your practice is going.</p></div>
        <a class="btn primary" href="#/problems">Solve a problem →</a>
      </div>
      <div class="grid stats">
        <div class="card stat"><small>Solved</small><strong>${d.solved}<span class="sub"> / ${d.totalProblems}</span></strong>
          <div class="bar" style="margin-top:10px"><span style="width:${solvedPct}%"></span></div></div>
        <div class="card stat"><small>Success rate</small><strong>${pct(d.successRate)}</strong>
          <span class="sub">${d.attempted} attempted</span></div>
        <div class="card stat"><small>Average score</small><strong>${pct(d.averageScore)}</strong>
          <span class="sub">${d.submissions} submissions</span></div>
        <div class="card stat"><small>Level</small><strong>${esc(d.level)}</strong>
          <span class="sub">avg run ${Math.round(d.averageTimeMs)} ms</span></div>
      </div>
      <div class="grid two" style="margin-top:18px">
        <div class="card pad">
          <h3>Recent submissions</h3>
          ${recent.length ? `<table><tbody>${recent.slice(0, 6).map((s) => `
            <tr class="clickable" data-sub="${s.id}"><td><strong>${esc(s.problemTitle)}</strong><br><small>${when(s.submittedAt)}</small></td>
            <td>${pill(s.status)}</td><td style="text-align:right"><strong>${pct(s.score)}</strong></td></tr>`).join('')}
            </tbody></table>` : '<div class="empty">No submissions yet. Pick a problem to get started.</div>'}
        </div>
        <div class="card pad">
          <h3>By difficulty</h3>
          ${d.byDifficulty.map((r) => `
            <div class="diff-row">${pill(title(r.difficulty), r.difficulty)}
              <div class="bar"><span style="width:${r.averageScore}%"></span></div>
              <small>${r.solved}/${r.attempts} passed · ${pct(r.averageScore)}</small></div>`).join('')}
          <p style="margin:18px 0 0"><small>Bars show your average score at each level.</small></p>
        </div>
      </div>`;
    $$('[data-sub]', view).forEach((row) => row.addEventListener('click', () => openSubmission(row.dataset.sub)));
  }

  // ---------------------------------------------------------------- problems (Member 2)
  async function renderProblems(_, token) {
    view.innerHTML = `
      <div class="page-head">
        <div><h2>Problems</h2><p>Each problem is checked against sample and hidden test cases.</p></div>
        <div class="filters">
          <select id="f-diff"><option value="">All difficulties</option><option>EASY</option><option>MEDIUM</option><option>HARD</option></select>
          <select id="f-cat"><option value="">All categories</option>
            ${['ARRAYS', 'STRINGS', 'MATH', 'SEARCHING', 'SORTING', 'OTHER'].map((c) => `<option value="${c}">${title(c)}</option>`).join('')}</select>
        </div>
      </div>
      <div class="card" id="problem-list"><div class="empty">Loading…</div></div>`;
    const load = async () => {
      const q = new URLSearchParams();
      if ($('#f-diff').value) q.set('difficulty', $('#f-diff').value);
      if ($('#f-cat').value) q.set('category', $('#f-cat').value);
      const rows = await api('/problems?' + q);
      if (stale(token)) return;
      $('#problem-list').innerHTML = rows.length ? `<table>
        <thead><tr><th>Problem</th><th>Difficulty</th><th>Category</th><th>Status</th><th style="text-align:right">Best</th></tr></thead>
        <tbody>${rows.map((p) => `<tr class="clickable" data-id="${p.id}">
          <td><strong>${esc(p.title)}</strong><br><small>${esc(p.description.slice(0, 90))}${p.description.length > 90 ? '…' : ''}</small></td>
          <td>${pill(title(p.difficulty), p.difficulty)}</td><td>${pill(title(p.category), 'cat')}</td>
          <td>${pill(title(p.status), p.status)}</td>
          <td style="text-align:right">${p.bestScore == null ? '—' : pct(p.bestScore)}</td></tr>`).join('')}</tbody></table>`
        : '<div class="empty">No problems match these filters.</div>';
      $$('[data-id]', view).forEach((r) => r.addEventListener('click', () => { location.hash = `#/problem/${r.dataset.id}`; }));
    };
    $('#f-diff').addEventListener('change', load);
    $('#f-cat').addEventListener('change', load);
    await load();
  }

  // ---------------------------------------------------------------- solve (Members 2 + 3 + 4)
  async function renderSolve(id, token) {
    const p = await api(`/problems/${encodeURIComponent(id)}`);
    if (stale(token)) return;
    view.innerHTML = `
      <div class="page-head">
        <div><a href="#/problems"><small>← All problems</small></a>
          <h2>${esc(p.title)}</h2>
          <p>${pill(title(p.difficulty), p.difficulty)} ${pill(title(p.category), 'cat')}
             <small>· ${p.totalTests} test cases (${p.samples.length} shown)</small></p></div>
      </div>
      <div class="solve">
        <div class="card statement">
          <p>${esc(p.description)}</p>
          <h4>Input format</h4><p>${esc(p.inputFormat)}</p>
          <h4>Output format</h4><p>${esc(p.outputFormat)}</p>
          <h4>Constraints</h4><p>${esc(p.constraints)}</p>
          ${p.samples.map((s, i) => `<h4>Sample ${i + 1}</h4>
            <div class="sample"><div><small>Input</small><pre>${esc(s.input)}</pre></div>
            <div><small>Expected output</small><pre>${esc(s.output)}</pre></div></div>`).join('')}
          <h4>Notes</h4><p><small>Read from standard input and print to standard output. Use one public class (for example <code>Main</code>) with no package line. Each test has a 5 second limit.</small></p>
        </div>
        <div>
          <div class="card editor-card">
            <div class="editor-bar"><span class="lang">Main.java</span>
              <div style="display:flex;gap:8px;align-items:center">
                <span class="editor-hint"><kbd>Ctrl</kbd> + <kbd>Enter</kbd> to submit</span>
                <button class="btn ghost small" id="reset">Reset</button>
                <button class="btn primary small" id="submit">Submit code</button>
              </div></div>
            <textarea id="code"></textarea>
          </div>
          <div id="result"></div>
        </div>
      </div>`;
    const textarea = $('#code');
    textarea.value = state.drafts[id] || TEMPLATE;
    if (window.CodeMirror) {
      state.editor = CodeMirror.fromTextArea(textarea, {
        mode: 'text/x-java', lineNumbers: true, indentUnit: 4, tabSize: 4,
        matchBrackets: true, autoCloseBrackets: true,
        extraKeys: { Tab: (cm) => cm.replaceSelection('    '), 'Ctrl-Enter': () => submit(), 'Cmd-Enter': () => submit() },
      });
      state.editor.on('change', (cm) => { state.drafts[id] = cm.getValue(); });
    } else {
      textarea.style.height = '420px';
      textarea.addEventListener('input', () => { state.drafts[id] = textarea.value; });
    }
    const getCode = () => (state.editor ? state.editor.getValue() : textarea.value);
    $('#reset').addEventListener('click', () => {
      if (!confirm('Replace your code with the starter template?')) return;
      if (state.editor) state.editor.setValue(TEMPLATE); else textarea.value = TEMPLATE;
    });
    const button = $('#submit');
    async function submit() {
      if (button.disabled) return;
      button.disabled = true;
      button.innerHTML = '<span class="spinner"></span> Running…';
      $('#result').innerHTML = `<div class="card result"><div class="running"><span class="spinner dark"></span>Compiling and running your code on ${p.totalTests} test cases, then asking ${esc(state.reviewer)} for feedback…</div></div>`;
      try {
        const r = await api('/submissions', { method: 'POST', body: { problemId: p.id, code: getCode() } });
        $('#result').innerHTML = resultCard(r);
      } catch (e) {
        $('#result').innerHTML = `<div class="card pad result"><span class="form-error">${esc(e.message)}</span></div>`;
      } finally {
        button.disabled = false;
        button.textContent = 'Submit code';
      }
    }
    button.addEventListener('click', submit);
  }

  function resultCard(r) {
    const kind = r.status === 'PASSED' ? 'ok' : r.status === 'COMPILATION ERROR' ? 'err' : 'bad';
    const heading = r.status === 'PASSED' ? 'All tests passed' : r.status === 'COMPILATION ERROR' ? 'Compilation error' : 'Some tests failed';
    return `<div class="card result">
      <div class="verdict ${kind}"><div><h3>${heading}</h3><small>${r.passed} of ${r.total} tests passed · ${r.timeMs} ms</small></div>
        <div class="score">${pct(r.score)}</div></div>
      <div class="result-body">
        ${r.compilationError ? `<div><small><strong>Compiler output</strong></small><pre>${esc(r.compilationError)}</pre></div>` : ''}
        ${r.tests.length ? `<div class="chips">${r.tests.map((t, i) => `
          <div class="chip ${t.status === 'ACCEPTED' ? 'pass' : 'fail'}">
            <b>Test ${i + 1} ${t.sample ? '· sample' : '· hidden'}</b><span>${esc(title(t.status))}</span><small>${t.timeMs} ms</small></div>`).join('')}</div>` : ''}
        ${r.tests.filter((t) => t.sample && t.status !== 'ACCEPTED').map((t) => `
          <div class="sample"><div><small>Expected</small><pre>${esc(t.expected)}</pre></div>
          <div><small>${t.actual ? 'Your output' : 'Error'}</small><pre>${esc(t.actual || shortError(t.error) || '(nothing)')}</pre></div></div>`).join('')}
        <div class="feedback"><div class="feedback-head"><span class="ai-badge">✦</span>AI feedback <small>· ${esc(r.reviewer)}</small></div>
          <div class="feedback-body">${formatFeedback(r.review)}</div></div>
      </div></div>`;
  }

  // ---------------------------------------------------------------- history (Member 5)
  async function renderHistory(_, token) {
    const rows = await api('/submissions');
    if (stale(token)) return;
    view.innerHTML = `
      <div class="page-head"><div><h2>Submissions</h2><p>Every attempt is saved with its score and feedback. Click one to review it.</p></div></div>
      <div class="card">${rows.length ? `<table>
        <thead><tr><th>Problem</th><th>Difficulty</th><th>Status</th><th>Tests</th><th>Score</th><th>Submitted</th></tr></thead>
        <tbody>${rows.map((s) => `<tr class="clickable" data-sub="${s.id}">
          <td><strong>${esc(s.problemTitle)}</strong></td><td>${pill(title(s.difficulty), s.difficulty)}</td>
          <td>${pill(s.status)}</td><td>${s.passed}/${s.total}</td><td><strong>${pct(s.score)}</strong></td>
          <td><small>${when(s.submittedAt)}</small></td></tr>`).join('')}</tbody></table>`
        : '<div class="empty">No submissions yet.</div>'}</div>`;
    $$('[data-sub]', view).forEach((row) => row.addEventListener('click', () => openSubmission(row.dataset.sub)));
  }

  async function openSubmission(id) {
    try {
      const s = await api(`/submissions/${encodeURIComponent(id)}`);
      modal(`<h2>${esc(s.problemTitle)}</h2>
        <p>${pill(s.status)} <strong>${pct(s.score)}</strong> <small>· ${s.passed}/${s.total} tests · ${when(s.submittedAt)}</small></p>
        <pre class="code">${esc(s.code)}</pre>
        <div class="feedback"><div class="feedback-head"><span class="ai-badge">✦</span>AI feedback</div><div class="feedback-body">${formatFeedback(s.review || '')}</div></div>
        <p style="margin-top:16px"><a class="btn ghost small" href="#/problem/${s.problemId}" onclick="document.getElementById('modal').classList.add('hidden')">Open problem</a></p>`);
    } catch (e) { toast(e.message); }
  }

  // ---------------------------------------------------------------- leaderboard (Member 5)
  async function renderLeaderboard(_, token) {
    const rows = await api('/stats/leaderboard');
    if (stale(token)) return;
    const medal = (n) => ({ 1: '🥇', 2: '🥈', 3: '🥉' }[n] || n);
    view.innerHTML = `
      <div class="page-head"><div><h2>Leaderboard</h2><p>Total score = your best score on each problem, added up. Re-submitting the same problem can't inflate it.</p></div></div>
      <div class="card">${rows.length ? `<table>
        <thead><tr><th style="width:80px">Rank</th><th>Student</th><th>Problems solved</th><th style="text-align:right">Total score</th></tr></thead>
        <tbody>${rows.map((r) => `<tr class="${r.me ? 'me-row' : ''}"><td style="font-size:18px">${medal(r.rank)}</td>
          <td><strong>${esc(r.name)}</strong>${r.me ? ' <small>(you)</small>' : ''}</td><td>${r.solved}</td>
          <td style="text-align:right"><strong>${Math.round(r.score)}</strong></td></tr>`).join('')}</tbody></table>`
        : '<div class="empty">No one has submitted yet. Be the first!</div>'}</div>`;
  }

  // ---------------------------------------------------------------- manage problems (Member 2 admin)
  async function renderManage(_, token = state.route) {
    if (!state.me.admin) { view.innerHTML = '<div class="card pad empty">Only admins can manage problems.</div>'; return; }
    const problems = await api('/problems');
    if (stale(token)) return;
    view.innerHTML = `
      <div class="page-head"><div><h2>Manage problems</h2><p>Add problems with sample (visible) and hidden test cases.</p></div></div>
      <div class="grid two">
        <form class="card pad form" id="new-problem">
          <h3>New problem</h3>
          <div class="form-grid">
            <label class="full">Title<input name="title" maxlength="200" required></label>
            <label class="full">Description<textarea name="description" rows="3" required></textarea></label>
            <label>Input format<textarea name="inputFormat" rows="2" required></textarea></label>
            <label>Output format<textarea name="outputFormat" rows="2" required></textarea></label>
            <label class="full">Constraints<input name="constraints" required></label>
            <label>Difficulty<select name="difficulty"><option>EASY</option><option>MEDIUM</option><option>HARD</option></select></label>
            <label>Category<select name="category">${['ARRAYS', 'STRINGS', 'MATH', 'SEARCHING', 'SORTING', 'OTHER'].map((c) => `<option>${c}</option>`).join('')}</select></label>
          </div>
          <div><strong>Test cases</strong> <small>(at least one sample and one hidden)</small></div>
          <div id="tests" class="grid" style="gap:10px"></div>
          <button type="button" class="btn ghost small" id="add-test">+ Add test case</button>
          <p class="form-error" data-error></p>
          <button class="btn primary" type="submit">Save problem</button>
        </form>
        <div class="card"><table><thead><tr><th>Problem</th><th>Level</th><th></th></tr></thead><tbody>
          ${problems.map((p) => `<tr><td><strong>${esc(p.title)}</strong><br><small>${esc(title(p.category))}</small></td>
          <td>${pill(title(p.difficulty), p.difficulty)}</td>
          <td style="text-align:right"><button class="btn danger small" data-del="${p.id}" data-title="${esc(p.title)}">Delete</button></td></tr>`).join('')}
        </tbody></table></div>
      </div>`;
    const addTest = (sample) => {
      const row = document.createElement('div');
      row.className = 'test-row';
      row.innerHTML = `<textarea rows="2" placeholder="Input" data-in></textarea><textarea rows="2" placeholder="Expected output" data-out></textarea>
        <label class="check"><input type="checkbox" data-sample ${sample ? 'checked' : ''}> Sample</label>
        <button type="button" class="btn ghost small" aria-label="Remove">×</button>`;
      $('button', row).addEventListener('click', () => row.remove());
      $('#tests').appendChild(row);
    };
    addTest(true); addTest(false);
    $('#add-test').addEventListener('click', () => addTest(false));
    $('#new-problem').addEventListener('submit', async (e) => {
      e.preventDefault();
      const form = e.target;
      const body = Object.fromEntries(new FormData(form));
      body.tests = $$('.test-row', form).map((r) => ({
        input: $('[data-in]', r).value, output: $('[data-out]', r).value, sample: $('[data-sample]', r).checked,
      }));
      try {
        await api('/problems', { method: 'POST', body });
        toast('Problem saved.');
        renderManage(null, state.route);
      } catch (err) { $('[data-error]', form).textContent = err.message; }
    });
    $$('[data-del]', view).forEach((b) => b.addEventListener('click', async () => {
      if (!confirm(`Delete "${b.dataset.title}" and all its test cases and submissions?`)) return;
      try { await api(`/problems/${b.dataset.del}`, { method: 'DELETE' }); toast('Problem deleted.'); renderManage(null, state.route); }
      catch (err) { toast(err.message); }
    }));
  }

  // ---------------------------------------------------------------- about
  async function renderAbout() {
    const modules = [
      ['Member 1', 'User & Authentication', 'Registration, login with hashed passwords, sessions, student pages.', 'com.bcorps.auth · com.bcorps.ui'],
      ['Member 2', 'Problems & Test Cases', 'Problem bank, difficulty and category, sample vs hidden test cases, file import/export.', 'com.bcorps.problems'],
      ['Member 3', 'Code Execution Engine', 'Compiles and runs code in a separate process with a time limit, compares output, scores it.', 'com.bcorps.execution'],
      ['Member 4', 'AI Code Review', 'Groq or Gemini AI explains errors and suggests improvements; offline rule checker as a fallback.', 'com.bcorps.review'],
      ['Member 5', 'Progress & Database', 'H2 database, submissions, dashboard, difficulty stats, leaderboard, web integration.', 'com.bcorps.progress · com.bcorps.web'],
    ];
    view.innerHTML = `
      <div class="page-head"><div><h2>About the project</h2><p>AI-Powered Code Review & Programming Practice System · Java group project</p></div></div>
      <div class="card pad"><h3>What happens when you press Submit</h3>
        <div class="flow">
          <div class="step"><b>1 · Login</b><small>Member 1 checks your session</small></div>
          <div class="step"><b>2 · Problem</b><small>Member 2 loads sample + hidden tests</small></div>
          <div class="step"><b>3 · Execute</b><small>Member 3 compiles, runs, compares, scores</small></div>
          <div class="step"><b>4 · Review</b><small>Member 4 asks ${esc(state.reviewer)}</small></div>
          <div class="step"><b>5 · Save</b><small>Member 5 stores it and updates stats</small></div>
        </div></div>
      <div class="card" style="margin-top:18px"><table>
        <thead><tr><th>Member</th><th>Module</th><th>What it does</th><th>Package</th></tr></thead>
        <tbody>${modules.map((m) => `<tr><td><strong>${m[0]}</strong></td><td>${m[1]}</td><td>${m[2]}</td><td><code>${m[3]}</code></td></tr>`).join('')}</tbody></table></div>
      <div class="card pad" style="margin-top:18px"><h3>Built with</h3>
        <p style="margin:0">Java 17+, Jakarta Servlets on embedded Jetty, JDBC with an H2 database, ProcessBuilder and multithreading for code execution, the Groq and Google Gemini APIs for review, and plain HTML/CSS/JavaScript for this interface.</p></div>`;
  }

  // ---------------------------------------------------------------- start
  (async () => {
    try {
      const data = await api('/auth/me');
      state.reviewer = data.reviewer;
      if (data.student) { state.me = data.student; showApp(); } else { showAuth(); }
    } catch (_) { showAuth(); }
  })();
})();
