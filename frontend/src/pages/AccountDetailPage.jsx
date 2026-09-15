import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { getAccount, getStatement } from '../api'
import { useAuth } from '../context/AuthContext'
import { logout } from '../api'

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
        <Link to="/settings">Setări</Link>
        <button onClick={handleLogout}>Deconectare</button>
      </div>
    </nav>
  )
}

function getTipBadge(tip) {
  const map = {
    DEPUNERE: 'badge-success',
    TRANSFER_BANCAR: 'badge-info',
    PLATA_CARD: 'badge-warning',
    SCHIMB_VALUTAR: 'badge-neutral',
  }
  return map[tip] || 'badge-neutral'
}

function getStatusBadge(status) {
  if (status === 'COMPLETATA') return 'badge-success'
  if (status === 'ESUAT') return 'badge-danger'
  return 'badge-warning'
}

export default function AccountDetailPage() {
  const { iban } = useParams()
  const [account, setAccount] = useState(null)
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadingTx, setLoadingTx] = useState(false)
  const [error, setError] = useState('')
  const [txError, setTxError] = useState('')
  const now = new Date()
  const defaultMonth = String(now.getMonth() + 1).padStart(2, '0') + '/' + now.getFullYear()
  const [start, setStart] = useState(defaultMonth)
  const [end, setEnd] = useState(defaultMonth)

  useEffect(() => {
    getAccount(iban)
      .then(setAccount)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [iban])

  async function loadStatement(e) {
    e.preventDefault()
    setTxError('')
    setLoadingTx(true)
    try {
      const data = await getStatement(iban, start, end)
      setTransactions(data)
    } catch (err) {
      setTxError(err.message)
    } finally {
      setLoadingTx(false)
    }
  }

  if (loading) return <><Navbar /><div className="page-container"><div className="loading">Se încarcă...</div></div></>
  if (error) return <><Navbar /><div className="page-container"><div className="alert alert-error">{error}</div></div></>

  return (
    <>
      <Navbar />
      <div className="page-container">
        <div className="page-header">
          <div>
            <div className="page-title">Detalii Cont</div>
            <div className="page-subtitle font-mono">{iban}</div>
          </div>
          <Link to="/" className="btn btn-outline">Înapoi</Link>
        </div>

        {account && (
          <div className="grid-2 mb-6">
            <div className="account-card" style={{ cursor: 'default' }}>
              <div className="label">{account.tip === 'CONT_CURENT' ? 'Cont Curent' : 'Cont de Economii'}</div>
              <div className="iban">{account.iban}</div>
              <div>
                <span className="balance">
                  {account.sold.toLocaleString('ro-RO', { minimumFractionDigits: 2 })}
                </span>
                <span className="currency">{account.valuta}</span>
              </div>
              <div className="type-badge">
                {account.tip === 'CONT_CURENT'
                  ? `Taxă adm.: ${account.taxaAdministrare} ${account.valuta}/lună`
                  : `Dobândă: ${(account.rataDobanda * 100).toFixed(2)}%`}
              </div>
            </div>
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <div className="section-title">Carduri asociate ({account.carduri?.length || 0})</div>
              {account.carduri?.length === 0 && <span className="text-muted text-sm">Niciun card</span>}
              {account.carduri?.map(card => (
                <div key={card.numarCard} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span className="font-mono text-sm">{card.numarCardMascat}</span>
                  <span className={`badge ${card.isBlocat ? 'badge-danger' : 'badge-success'}`}>
                    {card.isBlocat ? 'Blocat' : 'Activ'}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="card">
          <div className="section-title">Extras de Cont</div>
          <form onSubmit={loadStatement} style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
            <div className="form-group" style={{ margin: 0, flex: 1, minWidth: 150 }}>
              <label className="form-label">De la (MM/AAAA)</label>
              <input
                type="text"
                className="form-control"
                placeholder="01/2026"
                value={start}
                onChange={e => setStart(e.target.value)}
              />
            </div>
            <div className="form-group" style={{ margin: 0, flex: 1, minWidth: 150 }}>
              <label className="form-label">Până la (MM/AAAA)</label>
              <input
                type="text"
                className="form-control"
                placeholder="12/2026"
                value={end}
                onChange={e => setEnd(e.target.value)}
              />
            </div>
            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button type="submit" className="btn btn-primary" disabled={loadingTx}>
                {loadingTx ? 'Se încarcă...' : 'Caută'}
              </button>
            </div>
          </form>

          {txError && <div className="alert alert-error">{txError}</div>}

          {transactions.length > 0 ? (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Dată</th>
                    <th>Tip</th>
                    <th>Sumă</th>
                    <th>Din cont</th>
                    <th>În cont</th>
                    <th>Detalii</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map(tx => (
                    <tr key={tx.id}>
                      <td className="text-sm">{tx.dataTranzactie}</td>
                      <td><span className={`badge ${getTipBadge(tx.tip)}`}>{tx.tip.replace(/_/g, ' ')}</span></td>
                      <td className="font-bold">
                        {tx.suma.toLocaleString('ro-RO', { minimumFractionDigits: 2 })} {tx.valuta}
                      </td>
                      <td className="font-mono text-sm">{tx.ibanSursa?.substring(0, 12)}...</td>
                      <td className="font-mono text-sm">{tx.ibanDestinatie ? tx.ibanDestinatie.substring(0, 12) + '...' : '-'}</td>
                      <td className="text-sm">{tx.detalii}</td>
                      <td><span className={`badge ${getStatusBadge(tx.status)}`}>{tx.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            !loadingTx && <p className="text-muted text-sm">Caută tranzacții prin filtrele de mai sus.</p>
          )}
        </div>
      </div>
    </>
  )
}
