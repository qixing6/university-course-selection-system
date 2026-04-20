const TOKEN_KEY = 'crs_student_token'
const USER_KEY = 'crs_student_profile'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function setProfile(profile) {
  if (profile) {
    localStorage.setItem(USER_KEY, JSON.stringify(profile))
  }
}

export function getProfile() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}
