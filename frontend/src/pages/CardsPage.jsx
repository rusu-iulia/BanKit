import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { logout, getMyCards, blockCard, unblockCard, changePin, setDailyLimit } from '../api'

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
        <Link to="/cards" className="active">Carduri</Link>
        <Link to="/rewards">Recompense</Link>
        <Link to="/settings">Setări</Link>
        <button onClick={handleLogout}>Deconectare</button>
      </div>
    </nav>
  )
}

function CardItem({ card, onRefresh }) {
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)
  const [showPinForm, setShowPinForm] = useState(false)
  const [showLimitForm, setShowLimitForm] = useState(false)
  const [pinVechi, setPinVechi] = useState('')
  const [pinNou, setPinNou] = useState('')
  const [limita, setLimita] = useState('')

  async function handleBlock() {
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      await blockCard(card.numarCard)
      setMsg({ text: 'Cardul a fost blocat.', type: 'success' })
      onRefresh()
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  async function handleUnblock() {
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      await unblockCard(card.numarCard)
      setMsg({ text: 'Cardul a fost deblocat.', type: 'success' })
      onRefresh()
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  async function handleChangePin(e) {
    e.preventDefault()
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      await changePin(card.numarCard, pinVechi, pinNou)
      setMsg({ text: 'PIN schimbat cu succes!', type: 'success' })
      setPinVechi('')
      setPinNou('')
      setShowPinForm(false)
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  async function handleSetLimit(e) {
    e.preventDefault()
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      await setDailyLimit(card.numarCard, parseFloat(limita))
      setMsg({ text: `Limită zilnică setată la ${limita}.`, type: 'success' })
      setLimita('')
      setShowLimitForm(false)
      onRefresh()
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card">
      <div className="bank-card" style={{ marginBottom: 16 }} data-blocked={card.isBlocat}>
        <div className={`bank-card ${card.isBlocat ? 'blocked' : ''}`}>
          <div className="card-type">{card.tip === 'CARD_DEBIT' ? 'Debit' : 'Credit'}</div>
          <div className="card-number">{card.numarCardMascat}</div>
          <div style={{ fontSize: '0.8rem', opacity: 0.75 }}>IBAN: {card.ibanCont?.substring(0, 14)}...</div>
          <div className="card-status">{card.isBlocat ? 'Blocat' : 'Activ'}</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 12, fontSize: '0.85rem' }}>
        <div>
          <span className="text-muted">Limită zilnică:</span><br />
          <strong>{card.limitaZilnica.toLocaleString('ro-RO')} RON</strong>
        </div>
        {card.tip === 'CARD_DEBIT' && (
          <div>
            <span className="text-muted">Limită contactless:</span><br />
            <strong>{card.limitaContactless} RON</strong>
          </div>
        )}
        {card.tip === 'CARD_CREDIT' && (
          <>
            <div>
              <span className="text-muted">Limită credit:</span><br />
              <strong>{card.limitaCredit.toLocaleString('ro-RO')} RON</strong>
            </div>
            <div>
              <span className="text-muted">Datorie curentă:</span><br />
              <strong className="text-danger">{card.datorieCurenta.toLocaleString('ro-RO')} RON</strong>
            </div>
          </>
        )}
      </div>

      {msg.text && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
        {card.isBlocat ? (
          <button className="btn btn-success btn-sm" onClick={handleUnblock} disabled={loading}>
            Deblochează
          </button>
        ) : (
          <button className="btn btn-danger btn-sm" onClick={handleBlock} disabled={loading}>
            Blochează
          </button>
        )}
        <button className="btn btn-outline btn-sm" onClick={() => { setShowPinForm(!showPinForm); setShowLimitForm(false) }}>
          Schimbă PIN
        </button>
        <button className="btn btn-outline btn-sm" onClick={() => { setShowLimitForm(!showLimitForm); setShowPinForm(false) }}>
          Setează limita
        </button>
      </div>

      {showPinForm && (
        <form onSubmit={handleChangePin} style={{ marginTop: 12, borderTop: '1px solid var(--border)', paddingTop: 12 }}>
          <div className="form-group">
            <label className="form-label">PIN vechi</label>
            <input
              type="password"
              maxLength={4}
              className="form-control"
              placeholder="****"
              value={pinVechi}
              onChange={e => setPinVechi(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">PIN nou (4 cifre)</label>
            <input
              type="password"
              maxLength={4}
              className="form-control"
              placeholder="****"
              value={pinNou}
              onChange={e => setPinNou(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn btn-secondary btn-sm" disabled={loading}>
            Schimbă PIN
          </button>
        </form>
      )}

      {showLimitForm && (
        <form onSubmit={handleSetLimit} style={{ marginTop: 12, borderTop: '1px solid var(--border)', paddingTop: 12 }}>
          <div className="form-group">
            <label className="form-label">Limită zilnică nouă (RON)</label>
            <input
              type="number"
              step="1"
              min="1"
              className="form-control"
              placeholder="ex: 5000"
              value={limita}
              onChange={e => setLimita(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn btn-secondary btn-sm" disabled={loading}>
            Setează limita
          </button>
        </form>
      )}
    </div>
  )
}

export default function CardsPage() {
  const [cards, setCards] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  function loadCards() {
    setLoading(true)
    getMyCards()
      .then(setCards)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadCards()
  }, [])

  return (
    <>
      <Navbar />
      <div className="page-container">
        <div className="page-header">
          <div>
            <div className="page-title">Cardurile Mele</div>
            <div className="page-subtitle">Gestionează cardurile tale</div>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {loading ? (
          <div className="text-muted">Se încarcă cardurile...</div>
        ) : cards.length === 0 ? (
          <div className="card">
            <p className="text-muted">Nu ai niciun card emis.</p>
          </div>
        ) : (
          <div className="grid-2">
            {cards.map(card => (
              <CardItem key={card.numarCard} card={card} onRefresh={loadCards} />
            ))}
          </div>
        )}
      </div>
    </>
  )
}
