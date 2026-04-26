import api from './index'

export function startInterview(formData) {
  return api.post('/interview/start', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function answer(sessionId, answer) {
  return api.post('/interview/answer', { answer }, {
    params: { sessionId }
  })
}

export function getSession(id) {
  return api.get(`/interview/session/${id}`)
}

export function getHistory() {
  return api.get('/interview/history')
}

export function getReport(id) {
  return api.get(`/interview/report/${id}`)
}

export function deleteSession(id) {
  return api.delete(`/interview/session/${id}`)
}

export function endInterview(id) {
  return api.post(`/interview/end/${id}`)
}
