import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { logout, changePassword } from '../api'

function Navbar() {
  const navigate = useNavigate()
  const { setUser, setIsAdmin } = useAuth()

  async function handleLogout() {
    await logout()
    setUser(null)
    setIsAdmin(false)
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">Ban<span>Kit</span></Link>
      <div className="navbar-links">
        <Link to="/">Conturi</Link>
        <Link to="/transfer">Transfer</Link>
        <Link to="/cards">Carduri</Link>
        <Link to="/rewards">Recompense</Link>
        <Link to="/settings" className="active">Setări</Link>
        <button onClick={handleLogout}>Deconectare</button>
      </div>
    </nav>
  )
}

export default function SettingsPage() {
  const [parolaVeche, setParolaVeche] = useState('')
  const [parolaNou, setParolaNou] = useState('')
  const [parolaConfirm, setParolaConfirm] = useState('')
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)

  async function handleChangePassword(e) {
    e.preventDefault()
    if (parolaNou !== parolaConfirm) {
      setMsg({ text: 'Parolele noi nu corespund.', type: 'error' })
      return
    }
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      await changePassword(parolaVeche, parolaNou)
      setMsg({ text: 'Parola a fost schimbată cu succes!', type: 'success' })
      setParolaVeche('')
      setParolaNou('')
      setParolaConfirm('')
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Navbar />
      <div className="page-container">
        <div className="page-header">
          <div>
            <div className="page-title">Setări Cont</div>
            <div className="page-subtitle">Gestionează setările contului tău</div>
          </div>
        </div>

        <div className="card" style={{ maxWidth: 480 }}>
          <div className="section-title">Schimbă Parola</div>
          {msg.text && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}
          <form onSubmit={handleChangePassword}>
            <div className="form-group">
              <label className="form-label">Parolă curentă</label>
              <input
                type="password"
                className="form-control"
                value={parolaVeche}
                onChange={e => setParolaVeche(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Parolă nouă</label>
              <input
                type="password"
                className="form-control"
                value={parolaNou}
                onChange={e => setParolaNou(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Confirmă parola nouă</label>
              <input
                type="password"
                className="form-control"
                value={parolaConfirm}
                onChange={e => setParolaConfirm(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Se procesează...' : 'Schimbă Parola'}
            </button>
          </form>
        </div>
      </div>
    </>
  )
}
