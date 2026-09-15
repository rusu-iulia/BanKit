import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login, adminLogin } from '../api'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const navigate = useNavigate()
  const { setUser, setIsAdmin } = useAuth()

  const [tab, setTab] = useState('client')
  const [cod, setCod] = useState('')
  const [parola, setParola] = useState('')
  const [adminParola, setAdminParola] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleClientLogin(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await login(cod, parola)
      setUser(data)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleAdminLogin(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await adminLogin(adminParola)
      setIsAdmin(true)
      setUser({ role: 'admin' })
      navigate('/admin')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-container">
        <div className="login-logo">
          <h1>Ban<span>Kit</span></h1>
          <p>Banca ta digitală</p>
        </div>

        <div className="toggle-group" style={{ marginBottom: 24 }}>
          <button
            className={`toggle-btn ${tab === 'client' ? 'active' : ''}`}
            onClick={() => { setTab('client'); setError('') }}
          >
            Client
          </button>
          <button
            className={`toggle-btn ${tab === 'admin' ? 'active' : ''}`}
            onClick={() => { setTab('admin'); setError('') }}
          >
            Administrator
          </button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {tab === 'client' ? (
          <form onSubmit={handleClientLogin}>
            <div className="form-group">
              <label className="form-label">CNP / CUI</label>
              <input
                type="text"
                className="form-control"
                placeholder="Introduceți CNP sau CUI"
                value={cod}
                onChange={e => setCod(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Parolă</label>
              <input
                type="password"
                className="form-control"
                placeholder="Parola ta"
                value={parola}
                onChange={e => setParola(e.target.value)}
                required
              />
            </div>
            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', justifyContent: 'center', padding: '12px' }}
              disabled={loading}
            >
              {loading ? 'Se conectează...' : 'Autentificare'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleAdminLogin}>
            <div className="form-group">
              <label className="form-label">Parolă Administrator</label>
              <input
                type="password"
                className="form-control"
                placeholder="Parola de admin"
                value={adminParola}
                onChange={e => setAdminParola(e.target.value)}
                required
              />
            </div>
            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', justifyContent: 'center', padding: '12px' }}
              disabled={loading}
            >
              {loading ? 'Se conectează...' : 'Acces Admin'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}
