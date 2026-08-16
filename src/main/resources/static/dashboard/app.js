const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
const state = { token: sessionStorage.getItem('incident-token'), user: null, applications: [], incidents: [], status: {} };

const labels = {
  HEALTHY: 'Saudável', DEGRADED: 'Degradado', DOWN: 'Indisponível', UNKNOWN: 'Aguardando',
  OPEN: 'Aberto', ACKNOWLEDGED: 'Reconhecido', RESOLVED: 'Resolvido',
  PRODUCTION: 'Produção', STAGING: 'Homologação', DEVELOPMENT: 'Desenvolvimento',
  CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Média', LOW: 'Baixa'
};

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  const response = await fetch(path, { ...options, headers });
  if (response.status === 401) { logout(false); throw new Error('Sua sessão expirou. Entre novamente.'); }
  if (!response.ok) {
    let error = {}; try { error = await response.json(); } catch (_) {}
    const details = error.fieldErrors ? Object.values(error.fieldErrors).join('. ') : '';
    throw new Error(details || error.message || `Não foi possível concluir (${response.status})`);
  }
  return response.status === 204 ? null : response.json();
}

function escapeHtml(value = '') {
  return String(value).replace(/[&<>'"]/g, char => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#39;', '"':'&quot;' }[char]));
}

function formatDate(value) {
  if (!value) return 'Ainda não verificado';
  return new Intl.DateTimeFormat('pt-BR', { dateStyle:'short', timeStyle:'short' }).format(new Date(value));
}

function initials(name = 'Usuário') {
  return name.split(/\s+/).slice(0, 2).map(word => word[0]).join('').toUpperCase();
}

function statusPill(status) {
  return `<span class="status ${escapeHtml(status)}">${escapeHtml(labels[status] || status)}</span>`;
}

function toast(message, error = false) {
  const element = $('#toast'); element.textContent = message; element.className = `toast show${error ? ' error' : ''}`;
  clearTimeout(toast.timer); toast.timer = setTimeout(() => element.className = 'toast', 3200);
}

function setBusy(button, busy) {
  if (!button) return; button.disabled = busy;
  if (busy) { button.dataset.label = button.textContent; button.textContent = 'Aguarde...'; }
  else if (button.dataset.label) button.textContent = button.dataset.label;
}

async function login(event) {
  event.preventDefault(); const button = $('#loginForm button'); setBusy(button, true); $('#loginError').textContent = '';
  try {
    const result = await api('/api/auth/login', { method:'POST', body:JSON.stringify({ username:$('#username').value, password:$('#password').value }) });
    state.token = result.token; sessionStorage.setItem('incident-token', state.token); state.user = result;
    await enterApp();
  } catch (error) { $('#loginError').textContent = error.message; }
  finally { setBusy(button, false); }
}

async function enterApp() {
  try {
    if (!state.user) state.user = await api('/api/auth/me');
    $('#loginView').classList.add('hidden'); $('#appView').classList.remove('hidden');
    $('#userName').textContent = state.user.name; $('#userRole').textContent = state.user.role; $('#userInitials').textContent = initials(state.user.name);
    $$('.admin-only').forEach(element => element.classList.toggle('hidden', state.user.role !== 'ADMIN'));
    await refresh();
  } catch (error) { toast(error.message, true); }
}

function logout(showMessage = true) {
  state.token = null; state.user = null; sessionStorage.removeItem('incident-token');
  $('#appView').classList.add('hidden'); $('#loginView').classList.remove('hidden');
  if (showMessage) toast('Sessão encerrada.');
}

async function refresh() {
  try {
    const [status, applications, incidents] = await Promise.all([api('/api/status'), api('/api/applications'), api('/api/incidents')]);
    state.status = status; state.applications = applications; state.incidents = incidents;
    renderAll();
  } catch (error) { toast(error.message, true); }
}

function renderAll() {
  const open = state.incidents.filter(item => item.status !== 'RESOLVED');
  $('#healthyCount').textContent = state.status.healthy ?? 0; $('#degradedCount').textContent = state.status.degraded ?? 0;
  $('#downCount').textContent = state.status.down ?? 0; $('#openCount').textContent = open.length; $('#incidentBadge').textContent = open.length;
  const total = state.applications.length;
  $('#environmentHeadline').textContent = total === 0 ? 'Seu ambiente está pronto para começar.' : open.length ? `${open.length} incidente${open.length > 1 ? 's' : ''} requer atenção.` : 'Todos os sistemas operando normalmente.';
  $('#lastUpdate').textContent = `Última atualização: ${new Intl.DateTimeFormat('pt-BR', { timeStyle:'medium' }).format(new Date())}`;
  renderServicePreview(); renderIncidentPreview(); renderServicesTable(); renderIncidentsTable();
  if (state.user?.role === 'ADMIN' && $('#usersPage').classList.contains('active')) loadUsers();
}

function renderServicePreview() {
  const container = $('#servicePreview');
  if (!state.applications.length) { container.className = 'service-list empty-state'; container.textContent = 'Cadastre sua primeira aplicação para iniciar o monitoramento.'; return; }
  container.className = 'service-list';
  container.innerHTML = state.applications.slice(0, 5).map(app => `<div class="service-row"><span class="service-avatar">${escapeHtml(initials(app.name))}</span><div><strong>${escapeHtml(app.name)}</strong><small>${escapeHtml(app.url)} · ${escapeHtml(labels[app.environment])}</small></div>${statusPill(app.status)}</div>`).join('');
}

function renderIncidentPreview() {
  const container = $('#incidentPreview'); const incidents = state.incidents.slice(0, 5);
  if (!incidents.length) { container.className = 'timeline empty-state'; container.textContent = 'Nenhum incidente registrado.'; return; }
  container.className = 'timeline';
  container.innerHTML = incidents.map(item => `<div class="timeline-item"><strong>${escapeHtml(item.title)}</strong><p>${escapeHtml(item.applicationName)} · ${statusPill(item.status)}</p><small>${formatDate(item.openedAt)}</small></div>`).join('');
}

function renderServicesTable() {
  const body = $('#servicesTable');
  if (!state.applications.length) { body.innerHTML = '<tr><td colspan="6" class="empty-state">Nenhuma aplicação cadastrada.</td></tr>'; return; }
  body.innerHTML = state.applications.map(app => `<tr><td><strong>${escapeHtml(app.name)}</strong><small>${escapeHtml(app.url)}</small></td><td>${escapeHtml(labels[app.environment])}</td><td>${statusPill(app.status)}</td><td>${app.lastResponseTimeMs == null ? '—' : `${app.lastResponseTimeMs} ms`}</td><td>${formatDate(app.lastCheckedAt)}</td><td>${state.user.role === 'ADMIN' ? `<button class="row-action" data-check="${app.id}">Verificar agora</button>` : ''}</td></tr>`).join('');
}

function renderIncidentsTable() {
  const filter = $('#incidentFilter').value;
  const list = filter ? state.incidents.filter(item => item.status === filter) : state.incidents;
  const body = $('#incidentsTable');
  if (!list.length) { body.innerHTML = '<tr><td colspan="6" class="empty-state">Nenhum incidente encontrado.</td></tr>'; return; }
  body.innerHTML = list.map(item => `<tr><td><strong>#${item.id} · ${escapeHtml(item.title)}</strong><small>${escapeHtml(item.description)}</small></td><td>${escapeHtml(item.applicationName)}</td><td><span class="severity ${item.severity}">${escapeHtml(labels[item.severity])}</span></td><td>${statusPill(item.status)}</td><td>${formatDate(item.openedAt)}</td><td>${incidentActions(item)}</td></tr>`).join('');
}

function incidentActions(item) {
  if (state.user.role !== 'ADMIN') return '';
  if (item.status === 'OPEN') return `<button class="row-action" data-ack="${item.id}">Reconhecer</button><button class="row-action" data-resolve="${item.id}">Resolver</button>`;
  if (item.status === 'ACKNOWLEDGED') return `<button class="row-action" data-resolve="${item.id}">Resolver</button>`;
  return '';
}

async function checkApplication(id, button) {
  setBusy(button, true); try { const result = await api(`/api/applications/${id}/check`, { method:'POST' }); toast(`${result.name}: ${labels[result.status]}`); await refresh(); }
  catch (error) { toast(error.message, true); } finally { setBusy(button, false); }
}

async function updateIncident(id, action, button) {
  setBusy(button, true); try { await api(`/api/incidents/${id}/${action}`, { method:'PATCH' }); toast(action === 'resolve' ? 'Incidente resolvido.' : 'Incidente reconhecido.'); await refresh(); }
  catch (error) { toast(error.message, true); } finally { setBusy(button, false); }
}

async function submitApplication(event) {
  event.preventDefault(); const form = event.currentTarget; const button = $('button[type="submit"]', form); setBusy(button, true); $('.form-error', form).textContent = '';
  const values = Object.fromEntries(new FormData(form)); values.monitoringEnabled = form.elements.monitoringEnabled.checked;
  try { await api('/api/applications', { method:'POST', body:JSON.stringify(values) }); form.reset(); $('#applicationDialog').close(); toast('Aplicação cadastrada.'); await refresh(); }
  catch (error) { $('.form-error', form).textContent = error.message; } finally { setBusy(button, false); }
}

async function submitUser(event) {
  event.preventDefault(); const form = event.currentTarget; const button = $('button[type="submit"]', form); setBusy(button, true); $('.form-error', form).textContent = '';
  const values = Object.fromEntries(new FormData(form));
  try { await api('/api/users', { method:'POST', body:JSON.stringify(values) }); form.reset(); $('#userDialog').close(); toast('Usuário criado.'); await loadUsers(); }
  catch (error) { $('.form-error', form).textContent = error.message; } finally { setBusy(button, false); }
}

async function loadUsers() {
  try { const users = await api('/api/users'); $('#usersTable').innerHTML = users.map(user => `<tr><td><strong>${escapeHtml(user.name)}</strong></td><td>${escapeHtml(user.username)}</td><td>${statusPill(user.role === 'ADMIN' ? 'ACKNOWLEDGED' : 'UNKNOWN').replace(labels.ACKNOWLEDGED, user.role).replace(labels.UNKNOWN, user.role)}</td><td>${statusPill(user.enabled ? 'enabled' : 'disabled')}</td><td>${formatDate(user.createdAt)}</td></tr>`).join(''); }
  catch (error) { toast(error.message, true); }
}

function navigate(page) {
  const titles = { overview:['COMMAND CENTER','Visão geral'], services:['MONITORAMENTO','Serviços'], incidents:['RESPOSTA OPERACIONAL','Incidentes'], users:['CONTROLE DE ACESSO','Usuários'] };
  $$('.page').forEach(element => element.classList.remove('active')); $(`#${page}Page`).classList.add('active');
  $$('.nav-item').forEach(element => element.classList.toggle('active', element.dataset.page === page));
  $('#pageEyebrow').textContent = titles[page][0]; $('#pageTitle').textContent = titles[page][1]; $('.sidebar').classList.remove('open');
  if (page === 'users') loadUsers();
}

document.addEventListener('click', event => {
  const target = event.target;
  if (target.closest('[data-refresh]')) refresh();
  if (target.closest('[data-go]')) navigate(target.closest('[data-go]').dataset.go);
  if (target.closest('[data-page]')) navigate(target.closest('[data-page]').dataset.page);
  if (target.closest('[data-check]')) checkApplication(target.closest('[data-check]').dataset.check, target.closest('[data-check]'));
  if (target.closest('[data-ack]')) updateIncident(target.closest('[data-ack]').dataset.ack, 'acknowledge', target.closest('[data-ack]'));
  if (target.closest('[data-resolve]')) updateIncident(target.closest('[data-resolve]').dataset.resolve, 'resolve', target.closest('[data-resolve]'));
  if (target.closest('[data-close]')) target.closest('dialog').close();
});

$('#loginForm').addEventListener('submit', login); $('#logoutButton').addEventListener('click', () => logout());
$('#menuButton').addEventListener('click', () => $('.sidebar').classList.toggle('open'));
$('#newApplicationButton').addEventListener('click', () => $('#applicationDialog').showModal());
$('#newUserButton').addEventListener('click', () => $('#userDialog').showModal());
$('#applicationForm').addEventListener('submit', submitApplication); $('#userForm').addEventListener('submit', submitUser);
$('#incidentFilter').addEventListener('change', renderIncidentsTable);

if (state.token) enterApp();
