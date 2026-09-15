const BASE = '/api'

async function request(method, path, body) {
  const options = {
    method,
    credentials: 'include',
    headers: {}
  }
  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(body)
  }
  const res = await fetch(BASE + path, options)
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: res.statusText }))
    throw new Error(err.error || 'Eroare necunoscuta')
  }
  return res.json()
}

// Auth
export const login = (cod, parola) =>
  request('POST', '/auth/login', { cod, parola })

export const adminLogin = (parola) =>
  request('POST', '/auth/admin-login', { parola })

export const logout = () =>
  request('POST', '/auth/logout')

export const getMe = () =>
  request('GET', '/auth/me')

// Clients
export const getAllClients = () =>
  request('GET', '/clients')

export const getClient = (id) =>
  request('GET', `/clients/${id}`)

export const createClient = (data) =>
  request('POST', '/clients', data)

export const getMyAccounts = () =>
  request('GET', '/clients/me/accounts')

export const getMyCards = () =>
  request('GET', '/clients/me/cards')

// Accounts
export const getAccount = (iban) =>
  request('GET', `/accounts/${iban}`)

export const getStatement = (iban, start, end) =>
  request('GET', `/accounts/${iban}/statement?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`)

export const deposit = (iban, suma) =>
  request('POST', '/accounts/deposit', { iban, suma })

export const transfer = (ibanSursa, ibanDestinatie, suma, detalii) =>
  request('POST', '/accounts/transfer', { ibanSursa, ibanDestinatie, suma, detalii })

export const exchange = (ibanSursa, ibanDestinatie, suma) =>
  request('POST', '/accounts/exchange', { ibanSursa, ibanDestinatie, suma })

export const openAccount = (data) =>
  request('POST', '/accounts/open', data)

// Cards
export const emitCard = (data) =>
  request('POST', '/cards/emit', data)

export const blockCard = (numarCard) =>
  request('POST', `/cards/${numarCard}/block`)

export const unblockCard = (numarCard) =>
  request('POST', `/cards/${numarCard}/unblock`)

export const changePin = (numarCard, pinVechi, pinNou) =>
  request('PUT', `/cards/${numarCard}/pin`, { pinVechi, pinNou })

export const setDailyLimit = (numarCard, limita) =>
  request('PUT', `/cards/${numarCard}/limit`, { limita })

export const cardPayment = (numarCard, suma, comerciant) =>
  request('POST', '/cards/pay', { numarCard, suma, comerciant })

// Rewards
export const getRewards = () =>
  request('GET', '/rewards')

export const redeemReward = (idOferta) =>
  request('POST', '/rewards/redeem', { idOferta })

export const changePassword = (parolaVeche, parolaNou) =>
  request('PUT', '/auth/password', { parolaVeche, parolaNou })

export const updateClient = (id, data) =>
  request('PUT', `/clients/${id}`, data)

export const deleteClient = (id) =>
  request('DELETE', `/clients/${id}`)

export const closeAccount = (iban) =>
  request('DELETE', `/accounts/${iban}`)

// Admin
export const applyInterest = () =>
  request('POST', '/admin/interest')

export const applyFees = () =>
  request('POST', '/admin/fees')

export const collectSubscription = (idClient, suma) =>
  request('POST', '/admin/subscription', { idClient, suma })
