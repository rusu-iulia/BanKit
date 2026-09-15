import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getMyAccounts, logout } from '../api'

function Navbar({ userName, puncteRev }) {
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
      <span className="navbar-brand">Ban<span>Kit</span></span>
      <div className="navbar-links">
        <Link to="/" className="active">Conturi</Link>
        <Link to="/transfer">Transfer</Link>
        <Link to="/cards">Carduri</Link>
        <Link to="/rewards">Recompense</Link>
        <Link to="/settings">Setări</Link>
        <button onClick={handleLogout}>Deconectare</button>
      </div>
    </nav>
  )
}

export default function DashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getMyAccounts()
      .then(setAccounts)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  const totalRon = accounts
    .filter(a => a.valuta === 'RON')
    .reduce((s, a) => s + a.sold, 0)

  return (
    <>
      <Navbar userName={user?.numeComplet} puncteRev={user?.puncteRev} />
      <div className="page-container">
        <div className="welcome-section">
          <div>
            <h1>Bună ziua, {user?.numeComplet || 'Client'}!</h1>
            <p>Iată un rezumat al conturilor tale</p>
          </div>
          <div className="rev-points-badge">
            <div className="points-value">{user?.puncteRev || 0}</div>
            <div className="points-label">RevPoints</div>
          </div>
        </div>

        <div className="quick-actions">
          <Link to="/transfer" className="btn btn-secondary">Depunere / Transfer</Link>
          <Link to="/cards" className="btn btn-accent">Cardurile mele</Link>
          <Link to="/rewards" className="btn btn-outline">Recompense</Link>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {loading ? (
          <div className="text-muted">Se încarcă conturile...</div>
        ) : accounts.length === 0 ? (
          <div className="card">
            <p className="text-muted">Nu ai niciun cont deschis.</p>
          </div>
        ) : (
          <>
            <div className="section-title">Conturile mele ({accounts.length})</div>
            <div className="grid-2">
              {accounts.map(acc => (
                <div
                  key={acc.iban}
                  className="account-card"
                  onClick={() => navigate(`/accounts/${acc.iban}`)}
                >
                  <div className="label">{acc.tip === 'CONT_CURENT' ? 'Cont Curent' : 'Cont de Economii'}</div>
                  <div className="iban">{acc.iban}</div>
                  <div>
                    <span className="balance">{acc.sold.toLocaleString('ro-RO', { minimumFractionDigits: 2 })}</span>
                    <span className="currency">{acc.valuta}</span>
                  </div>
                  <div className="type-badge">
                    {acc.tip === 'CONT_CURENT'
                      ? `Taxă: ${acc.taxaAdministrare} ${acc.valuta}/lună`
                      : `Dobândă: ${(acc.rataDobanda * 100).toFixed(1)}%`}
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </>
  )
}
