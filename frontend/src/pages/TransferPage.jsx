import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { logout, deposit, transfer, exchange, getMyAccounts } from '../api'

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
        <Link to="/transfer" className="active">Transfer</Link>
        <Link to="/cards">Carduri</Link>
        <Link to="/rewards">Recompense</Link>
        <Link to="/settings">Setări</Link>
        <button onClick={handleLogout}>Deconectare</button>
      </div>
    </nav>
  )
}

function StatusMsg({ msg, type }) {
  if (!msg) return null
  return <div className={`alert alert-${type}`}>{msg}</div>
}

export default function TransferPage() {
  const [tab, setTab] = useState('deposit')
  const [accounts, setAccounts] = useState([])

  // Deposit
  const [depIban, setDepIban] = useState('')
  const [depSuma, setDepSuma] = useState('')
  const [depMsg, setDepMsg] = useState({ text: '', type: '' })
  const [depLoading, setDepLoading] = useState(false)

  // Transfer
  const [trSursa, setTrSursa] = useState('')
  const [trDest, setTrDest] = useState('')
  const [trSuma, setTrSuma] = useState('')
  const [trDetalii, setTrDetalii] = useState('')
  const [trMsg, setTrMsg] = useState({ text: '', type: '' })
  const [trLoading, setTrLoading] = useState(false)

  // Exchange
  const [exSursa, setExSursa] = useState('')
  const [exDest, setExDest] = useState('')
  const [exSuma, setExSuma] = useState('')
  const [exMsg, setExMsg] = useState({ text: '', type: '' })
  const [exLoading, setExLoading] = useState(false)

  useEffect(() => {
    getMyAccounts().then(setAccounts).catch(() => {})
  }, [])

  async function handleDeposit(e) {
    e.preventDefault()
    setDepLoading(true)
    setDepMsg({ text: '', type: '' })
    try {
      await deposit(depIban, parseFloat(depSuma))
      setDepMsg({ text: 'Depunere efectuată cu succes!', type: 'success' })
      setDepSuma('')
    } catch (err) {
      setDepMsg({ text: err.message, type: 'error' })
    } finally {
      setDepLoading(false)
    }
  }

  async function handleTransfer(e) {
    e.preventDefault()
    setTrLoading(true)
    setTrMsg({ text: '', type: '' })
    try {
      await transfer(trSursa, trDest, parseFloat(trSuma), trDetalii || 'Transfer bancar')
      setTrMsg({ text: 'Transfer efectuat cu succes!', type: 'success' })
      setTrSuma('')
      setTrDetalii('')
    } catch (err) {
      setTrMsg({ text: err.message, type: 'error' })
    } finally {
      setTrLoading(false)
    }
  }

  async function handleExchange(e) {
    e.preventDefault()
    setExLoading(true)
    setExMsg({ text: '', type: '' })
    try {
      await exchange(exSursa, exDest, parseFloat(exSuma))
      setExMsg({ text: 'Schimb valutar efectuat cu succes!', type: 'success' })
      setExSuma('')
    } catch (err) {
      setExMsg({ text: err.message, type: 'error' })
    } finally {
      setExLoading(false)
    }
  }

  return (
    <>
      <Navbar />
      <div className="page-container">
        <div className="page-header">
          <div>
            <div className="page-title">Plăți și Transferuri</div>
            <div className="page-subtitle">Depuneri, transferuri și schimb valutar</div>
          </div>
        </div>

        <div className="tabs">
          <button className={`tab-btn ${tab === 'deposit' ? 'active' : ''}`} onClick={() => setTab('deposit')}>
            Depunere
          </button>
          <button className={`tab-btn ${tab === 'transfer' ? 'active' : ''}`} onClick={() => setTab('transfer')}>
            Transfer
          </button>
          <button className={`tab-btn ${tab === 'exchange' ? 'active' : ''}`} onClick={() => setTab('exchange')}>
            Schimb Valutar
          </button>
        </div>

        {tab === 'deposit' && (
          <div className="card" style={{ maxWidth: 500 }}>
            <div className="section-title">Depunere Numerar</div>
            <StatusMsg msg={depMsg.text} type={depMsg.type} />
            <form onSubmit={handleDeposit}>
              <div className="form-group">
                <label className="form-label">Cont destinație (IBAN)</label>
                <select className="form-control" value={depIban} onChange={e => setDepIban(e.target.value)} required>
                  <option value="">-- Selectează cont --</option>
                  {accounts.map(a => (
                    <option key={a.iban} value={a.iban}>{a.iban} ({a.valuta})</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Sumă</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  className="form-control"
                  placeholder="0.00"
                  value={depSuma}
                  onChange={e => setDepSuma(e.target.value)}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary" disabled={depLoading}>
                {depLoading ? 'Se procesează...' : 'Depune'}
              </button>
            </form>
          </div>
        )}

        {tab === 'transfer' && (
          <div className="card" style={{ maxWidth: 500 }}>
            <div className="section-title">Transfer Bancar</div>
            <StatusMsg msg={trMsg.text} type={trMsg.type} />
            <form onSubmit={handleTransfer}>
              <div className="form-group">
                <label className="form-label">Cont sursă</label>
                <select className="form-control" value={trSursa} onChange={e => setTrSursa(e.target.value)} required>
                  <option value="">-- Selectează contul tău --</option>
                  {accounts.map(a => (
                    <option key={a.iban} value={a.iban}>{a.iban} ({a.sold.toFixed(2)} {a.valuta})</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">IBAN destinație</label>
                <input
                  type="text"
                  className="form-control font-mono"
                  placeholder="RO00BTRL..."
                  value={trDest}
                  onChange={e => setTrDest(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Sumă</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  className="form-control"
                  placeholder="0.00"
                  value={trSuma}
                  onChange={e => setTrSuma(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Detalii (opțional)</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Motivul transferului"
                  value={trDetalii}
                  onChange={e => setTrDetalii(e.target.value)}
                />
              </div>
              <button type="submit" className="btn btn-secondary" disabled={trLoading}>
                {trLoading ? 'Se procesează...' : 'Trimite'}
              </button>
            </form>
          </div>
        )}

        {tab === 'exchange' && (
          <div className="card" style={{ maxWidth: 500 }}>
            <div className="section-title">Schimb Valutar</div>
            <div className="alert alert-info" style={{ marginBottom: 16 }}>
              Schimbul valutar se poate face doar între conturile tale cu valute diferite.
              Se aplică comision în funcție de abonament.
            </div>
            <StatusMsg msg={exMsg.text} type={exMsg.type} />
            <form onSubmit={handleExchange}>
              <div className="form-group">
                <label className="form-label">Cont sursă</label>
                <select className="form-control" value={exSursa} onChange={e => setExSursa(e.target.value)} required>
                  <option value="">-- Cont de debit --</option>
                  {accounts.map(a => (
                    <option key={a.iban} value={a.iban}>{a.iban} — {a.sold.toFixed(2)} {a.valuta}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Cont destinație</label>
                <select className="form-control" value={exDest} onChange={e => setExDest(e.target.value)} required>
                  <option value="">-- Cont de credit --</option>
                  {accounts.map(a => (
                    <option key={a.iban} value={a.iban}>{a.iban} — {a.sold.toFixed(2)} {a.valuta}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Sumă de schimbat</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  className="form-control"
                  placeholder="0.00"
                  value={exSuma}
                  onChange={e => setExSuma(e.target.value)}
                  required
                />
              </div>
              <button type="submit" className="btn btn-accent" disabled={exLoading}>
                {exLoading ? 'Se procesează...' : 'Schimbă Valuta'}
              </button>
            </form>
          </div>
        )}
      </div>
    </>
  )
}
