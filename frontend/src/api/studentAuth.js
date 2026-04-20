import request from '../utils/request'

export function register(data) {
  return request.post('/student/auth/register', data)
}

export function login(data) {
  return request.post('/student/auth/login', data)
}

export function logout() {
  return request.post('/student/auth/logout')
}

export function fetchCurrentUser() {
  return request.get('/student/auth/me')
}
