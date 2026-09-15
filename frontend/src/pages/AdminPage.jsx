import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import {
  logout, getAllClients, createClient, openAccount, emitCard,
  blockCard, unblockCard, applyInterest, applyFees, collectSubscription,
  getClient, updateClient, deleteClient, closeAccount
} from '../api'

function AdminNavbar() {
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
      <span className="navbar-brand">Ban<span>Kit</span> Admin</span>
      <div className="navbar-links">
        <button onClick={handleLogout}>Deconectare</button>
      </div>
    </nav>
  )
}

function StatusMsg({ text, type }) {
  if (!text) return null
  return <div className={`alert alert-${type}`}>{text}</div>
}

// ===== Add Client Section =====
function AddClientSection() {
  const [tip, setTip] = useState('PERSOANA_FIZICA')
  const [adresa, setAdresa] = useState('')
  const [parola, setParola] = useState('')
  const [abonament, setAbonament] = useState('STANDARD')
  const [nume, setNume] = useState('')
  const [prenume, setPrenume] = useState('')
  const [cnp, setCnp] = useState('')
  const [denumire, setDenumire] = useState('')
  const [cui, setCui] = useState('')
  const [reprezentant, setReprezentant] = useState('')
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      const data = { tip, adresa, parola, abonament }
      if (tip === 'PERSOANA_FIZICA') {
        Object.assign(data, { nume, prenume, cnp })
      } else {
        Object.assign(data, { denumire, cui, reprezentant })
      }
      const res = await createClient(data)
      setMsg({ text: `Client creat cu ID: ${res.id}`, type: 'success' })
      setAdresa(''); setParola(''); setNume(''); setPrenume(''); setCnp('')
      setDenumire(''); setCui(''); setReprezentant('')
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h3>Adaugă client</h3>
      <StatusMsg text={msg.text} type={msg.type} />
      <div className="toggle-group">
        <button
          className={`toggle-btn ${tip === 'PERSOANA_FIZICA' ? 'active' : ''}`}
          onClick={() => { setTip('PERSOANA_FIZICA'); setAbonament('STANDARD') }}
          type="button"
        >
          Persoană Fizică
        </button>
        <button
          className={`toggle-btn ${tip === 'PERSOANA_JURIDICA' ? 'active' : ''}`}
          onClick={() => { setTip('PERSOANA_JURIDICA'); setAbonament('STANDARD') }}
          type="button"
        >
          Persoană Juridică
        </button>
      </div>
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <div className="form-group">
            <label className="form-label">Adresă</label>
            <input type="text" className="form-control" value={adresa} onChange={e => setAdresa(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Parolă</label>
            <input type="password" className="form-control" value={parola} onChange={e => setParola(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Abonament</label>
            <select className="form-control" value={abonament} onChange={e => setAbonament(e.target.value)}>
              {tip === 'PERSOANA_FIZICA' ? (
                <>
                  <option value="STANDARD">Standard</option>
                  <option value="PREMIUM">Premium</option>
                </>
              ) : (
                <>
                  <option value="STANDARD">Standard</option>
                  <option value="BUSINESS_PRO">Business Pro</option>
                </>
              )}
            </select>
          </div>

          {tip === 'PERSOANA_FIZICA' ? (
            <>
              <div className="form-group">
                <label className="form-label">Nume</label>
                <input type="text" className="form-control" value={nume} onChange={e => setNume(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Prenume</label>
                <input type="text" className="form-control" value={prenume} onChange={e => setPrenume(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">CNP (13 cifre)</label>
                <input type="text" className="form-control" maxLength={13} value={cnp} onChange={e => setCnp(e.target.value)} required />
              </div>
            </>
          ) : (
            <>
              <div className="form-group">
                <label className="form-label">Denumire Companie</label>
                <input type="text" className="form-control" value={denumire} onChange={e => setDenumire(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">CUI (6 cifre)</label>
                <input type="text" className="form-control" maxLength={6} value={cui} onChange={e => setCui(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Reprezentant Legal</label>
                <input type="text" className="form-control" value={reprezentant} onChange={e => setReprezentant(e.target.value)} required />
              </div>
            </>
          )}
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Se procesează...' : 'Adaugă Client'}
        </button>
      </form>
    </div>
  )
}

// ===== Open Account Section =====
function OpenAccountSection() {
  const [idClient, setIdClient] = useState('')
  const [tip, setTip] = useState('CONT_CURENT')
  const [valuta, setValuta] = useState('RON')
  const [sold, setSold] = useState('')
  const [taxa, setTaxa] = useState('')
  const [dobanda, setDobanda] = useState('')
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      const data = {
        idClient: parseInt(idClient),
        tip,
        valuta,
        sold: parseFloat(sold || 0),
        taxaAdministrare: parseFloat(taxa || 0),
        rataDobanda: parseFloat(dobanda || 0)
      }
      const res = await openAccount(data)
      setMsg({ text: `Cont deschis. IBAN: ${res.iban}`, type: 'success' })
      setSold(''); setTaxa(''); setDobanda('')
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h3>Deschide cont</h3>
      <StatusMsg text={msg.text} type={msg.type} />
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <div className="form-group">
            <label className="form-label">ID Client</label>
            <input type="number" className="form-control" value={idClient} onChange={e => setIdClient(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Tip Cont</label>
            <select className="form-control" value={tip} onChange={e => setTip(e.target.value)}>
              <option value="CONT_CURENT">Cont Curent</option>
              <option value="CONT_ECONOMII">Cont de Economii</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Valută</label>
            <select className="form-control" value={valuta} onChange={e => setValuta(e.target.value)}>
              <option value="RON">RON</option>
              <option value="EUR">EUR</option>
              <option value="USD">USD</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Sold Inițial</label>
            <input type="number" step="0.01" min="0" className="form-control" value={sold} onChange={e => setSold(e.target.value)} />
          </div>
          {tip === 'CONT_CURENT' ? (
            <div className="form-group">
              <label className="form-label">Taxă administrare</label>
              <input type="number" step="0.01" min="0" className="form-control" value={taxa} onChange={e => setTaxa(e.target.value)} />
            </div>
          ) : (
            <div className="form-group">
              <label className="form-label">Rată dobândă</label>
              <input type="number" step="0.001" min="0" className="form-control" value={dobanda} onChange={e => setDobanda(e.target.value)} />
            </div>
          )}
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Se procesează...' : 'Deschide Cont'}
        </button>
      </form>
    </div>
  )
}

// ===== Close Account Section =====
function CloseAccountSection() {
  const [iban, setIban] = useState('')
  const [confirm, setConfirm] = useState(false)
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!confirm) {
      setMsg({ text: 'Bifați confirmarea înainte de a închide contul.', type: 'error' })
      return
    }
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      await closeAccount(iban)
      setMsg({ text: 'Contul a fost închis cu succes.', type: 'success' })
      setIban('')
      setConfirm(false)
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h3>Închide Cont</h3>
      <div className="alert alert-info" style={{ marginBottom: 12 }}>
        Acțiune ireversibilă. Toate tranzacțiile și cardurile asociate vor fi șterse.
      </div>
      <StatusMsg text={msg.text} type={msg.type} />
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">IBAN cont</label>
          <input
            type="text"
            className="form-control font-mono"
            placeholder="RO00BTRL..."
            value={iban}
            onChange={e => setIban(e.target.value)}
            required
          />
        </div>
        <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <input
            type="checkbox"
            id="confirm-close-account"
            checked={confirm}
            onChange={e => setConfirm(e.target.checked)}
          />
          <label htmlFor="confirm-close-account" style={{ margin: 0, cursor: 'pointer' }}>
            Confirm că doresc să închid definitiv acest cont
          </label>
        </div>
        <button type="submit" className="btn btn-danger" disabled={loading}>
          {loading ? 'Se procesează...' : 'Închide Cont'}
        </button>
      </form>
    </div>
  )
}

// ===== Emit Card Section =====
function EmitCardSection() {
  const [iban, setIban] = useState('')
  const [pin, setPin] = useState('')
  const [tip, setTip] = useState('CARD_DEBIT')
  const [limitaContactless, setLimitaContactless] = useState('300')
  const [limitaCredit, setLimitaCredit] = useState('5000')
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      const data = {
        iban,
        pin,
        tip,
        limitaContactless: parseFloat(limitaContactless),
        limitaCredit: parseFloat(limitaCredit)
      }
      const res = await emitCard(data)
      setMsg({ text: `Card emis: ${res.numarCard}`, type: 'success' })
      setPin(''); setIban('')
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h3>Emite Card</h3>
      <StatusMsg text={msg.text} type={msg.type} />
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <div className="form-group" style={{ gridColumn: '1/-1' }}>
            <label className="form-label">IBAN cont curent</label>
            <input type="text" className="form-control font-mono" value={iban} onChange={e => setIban(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">PIN (4 cifre)</label>
            <input type="password" maxLength={4} className="form-control" value={pin} onChange={e => setPin(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Tip Card</label>
            <select className="form-control" value={tip} onChange={e => setTip(e.target.value)}>
              <option value="CARD_DEBIT">Debit</option>
              <option value="CARD_CREDIT">Credit</option>
            </select>
          </div>
          {tip === 'CARD_DEBIT' ? (
            <div className="form-group">
              <label className="form-label">Limită contactless</label>
              <input type="number" step="1" min="0" className="form-control" value={limitaContactless} onChange={e => setLimitaContactless(e.target.value)} />
            </div>
          ) : (
            <div className="form-group">
              <label className="form-label">Limită credit</label>
              <input type="number" step="1" min="1" className="form-control" value={limitaCredit} onChange={e => setLimitaCredit(e.target.value)} />
            </div>
          )}
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Se procesează...' : 'Emite Card'}
        </button>
      </form>
    </div>
  )
}

// ===== Block/Unblock Card Section =====
function BlockCardSection() {
  const [numarCard, setNumarCard] = useState('')
  const [action, setAction] = useState('block')
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      if (action === 'block') {
        await blockCard(numarCard)
        setMsg({ text: 'Card blocat cu succes.', type: 'success' })
      } else {
        await unblockCard(numarCard)
        setMsg({ text: 'Card deblocat cu succes.', type: 'success' })
      }
      setNumarCard('')
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h3>Blocare / Deblocare Card</h3>
      <StatusMsg text={msg.text} type={msg.type} />
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <div className="form-group" style={{ gridColumn: '1/-1' }}>
            <label className="form-label">Număr card (16 cifre)</label>
            <input type="text" maxLength={16} className="form-control font-mono" value={numarCard} onChange={e => setNumarCard(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Acțiune</label>
            <select className="form-control" value={action} onChange={e => setAction(e.target.value)}>
              <option value="block">Blochează</option>
              <option value="unblock">Deblochează</option>
            </select>
          </div>
        </div>
        <button type="submit" className={`btn ${action === 'block' ? 'btn-danger' : 'btn-success'}`} disabled={loading}>
          {loading ? 'Se procesează...' : (action === 'block' ? 'Blochează Card' : 'Deblochează Card')}
        </button>
      </form>
    </div>
  )
}

// ===== Search Client Section =====
function SearchClientSection() {
  const [query, setQuery] = useState('')
  const [clientData, setClientData] = useState(null)
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)

  async function handleSearch(e) {
    e.preventDefault()
    setLoading(true)
    setMsg({ text: '', type: '' })
    setClientData(null)
    try {
      const data = await getClient(parseInt(query))
      setClientData(data)
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h3>Caută Client după ID</h3>
      <StatusMsg text={msg.text} type={msg.type} />
      <form onSubmit={handleSearch} style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
        <input
          type="number"
          className="form-control"
          placeholder="ID Client"
          value={query}
          onChange={e => setQuery(e.target.value)}
          required
          style={{ maxWidth: 200 }}
        />
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Se caută...' : 'Caută'}
        </button>
      </form>
      {clientData && (
        <div style={{ background: '#f8f9fc', border: '1px solid var(--border)', borderRadius: 8, padding: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
            <div>
              <strong style={{ fontSize: '1.1rem' }}>{clientData.numeComplet}</strong>
              <div className="text-muted text-sm">ID: {clientData.id}</div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <span className={`badge ${clientData.abonament === 'PREMIUM' ? 'badge-warning' : clientData.abonament === 'BUSINESS_PRO' ? 'badge-info' : 'badge-neutral'}`}>
                {clientData.abonament}
              </span>
              <div className="text-sm mt-4">{clientData.puncteRev} RevPoints</div>
            </div>
          </div>
          <div className="text-sm text-muted" style={{ marginBottom: 12 }}>{clientData.adresa}</div>
          {clientData.conturi?.length > 0 && (
            <>
              <div style={{ fontWeight: 700, fontSize: '0.85rem', marginBottom: 8 }}>Conturi ({clientData.conturi.length}):</div>
              {clientData.conturi.map(acc => (
                <div key={acc.iban} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid var(--border)', fontSize: '0.88rem' }}>
                  <span className="font-mono">{acc.iban}</span>
                  <span>{acc.sold.toFixed(2)} {acc.valuta} &bull; <span className="text-muted">{acc.tip === 'CONT_CURENT' ? 'CC' : 'CE'}</span></span>
                </div>
              ))}
            </>
          )}
        </div>
      )}
    </div>
  )
}

// ===== Edit Client Section =====
function EditClientSection() {
  const [id, setId] = useState('')
  const [adresa, setAdresa] = useState('')
  const [abonament, setAbonament] = useState('STANDARD')
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      await updateClient(parseInt(id), { adresa, abonament })
      setMsg({ text: 'Clientul a fost actualizat cu succes.', type: 'success' })
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h3>Editează Client</h3>
      <StatusMsg text={msg.text} type={msg.type} />
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <div className="form-group">
            <label className="form-label">ID Client</label>
            <input type="number" className="form-control" value={id} onChange={e => setId(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label">Abonament</label>
            <select className="form-control" value={abonament} onChange={e => setAbonament(e.target.value)}>
              <option value="STANDARD">Standard</option>
              <option value="PREMIUM">Premium</option>
              <option value="BUSINESS_PRO">Business Pro</option>
            </select>
          </div>
          <div className="form-group" style={{ gridColumn: '1/-1' }}>
            <label className="form-label">Adresă nouă</label>
            <input type="text" className="form-control" value={adresa} onChange={e => setAdresa(e.target.value)} required />
          </div>
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Se procesează...' : 'Actualizează Client'}
        </button>
      </form>
    </div>
  )
}

// ===== Delete Client Section =====
function DeleteClientSection() {
  const [id, setId] = useState('')
  const [confirm, setConfirm] = useState(false)
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!confirm) {
      setMsg({ text: 'Bifați confirmarea înainte de a șterge clientul.', type: 'error' })
      return
    }
    setLoading(true)
    setMsg({ text: '', type: '' })
    try {
      await deleteClient(parseInt(id))
      setMsg({ text: 'Clientul a fost șters cu succes.', type: 'success' })
      setId('')
      setConfirm(false)
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h3>Șterge Client</h3>
      <div className="alert alert-info" style={{ marginBottom: 12 }}>
        Acțiune ireversibilă. Vor fi șterse toate conturile, cardurile, tranzacțiile și recompensele clientului.
      </div>
      <StatusMsg text={msg.text} type={msg.type} />
      <form onSubmit={handleSubmit}>
        <div className="form-group" style={{ maxWidth: 200 }}>
          <label className="form-label">ID Client</label>
          <input type="number" className="form-control" value={id} onChange={e => setId(e.target.value)} required />
        </div>
        <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <input
            type="checkbox"
            id="confirm-delete-client"
            checked={confirm}
            onChange={e => setConfirm(e.target.checked)}
          />
          <label htmlFor="confirm-delete-client" style={{ margin: 0, cursor: 'pointer' }}>
            Confirm că doresc să șterg definitiv acest client
          </label>
        </div>
        <button type="submit" className="btn btn-danger" disabled={loading}>
          {loading ? 'Se procesează...' : 'Șterge Client'}
        </button>
      </form>
    </div>
  )
}

// ===== Admin Operations Section =====
function AdminOpsSection() {
  const [msg, setMsg] = useState({ text: '', type: '' })
  const [loading, setLoading] = useState('')
  const [idClient, setIdClient] = useState('')
  const [suma, setSuma] = useState('')

  async function handleInterest() {
    setLoading('interest')
    setMsg({ text: '', type: '' })
    try {
      await applyInterest()
      setMsg({ text: 'Dobânzile lunare au fost aplicate cu succes!', type: 'success' })
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading('')
    }
  }

  async function handleFees() {
    setLoading('fees')
    setMsg({ text: '', type: '' })
    try {
      await applyFees()
      setMsg({ text: 'Taxele de administrare au fost aplicate cu succes!', type: 'success' })
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading('')
    }
  }

  async function handleSubscription(e) {
    e.preventDefault()
    setLoading('sub')
    setMsg({ text: '', type: '' })
    try {
      await collectSubscription(parseInt(idClient), parseFloat(suma))
      setMsg({ text: 'Taxa de abonament încasată cu succes!', type: 'success' })
      setIdClient(''); setSuma('')
    } catch (err) {
      setMsg({ text: err.message, type: 'error' })
    } finally {
      setLoading('')
    }
  }

  return (
    <div className="admin-section">
      <h3>Operațiuni Administrative</h3>
      <StatusMsg text={msg.text} type={msg.type} />
      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
        <button
          className="btn btn-secondary"
          onClick={handleInterest}
          disabled={!!loading}
        >
          {loading === 'interest' ? 'Se procesează...' : 'Aplică Dobânzile Lunare'}
        </button>
        <button
          className="btn btn-secondary"
          onClick={handleFees}
          disabled={!!loading}
        >
          {loading === 'fees' ? 'Se procesează...' : 'Aplică Taxele de Administrare'}
        </button>
      </div>

      <div style={{ borderTop: '1px solid var(--border)', paddingTop: 16 }}>
        <div style={{ fontWeight: 700, marginBottom: 12, color: 'var(--primary)' }}>Încasează Taxa Abonament</div>
        <form onSubmit={handleSubscription} style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <div className="form-group" style={{ margin: 0, flex: 1, minWidth: 120 }}>
            <label className="form-label">ID Client</label>
            <input type="number" className="form-control" value={idClient} onChange={e => setIdClient(e.target.value)} required />
          </div>
          <div className="form-group" style={{ margin: 0, flex: 1, minWidth: 120 }}>
            <label className="form-label">Sumă</label>
            <input type="number" step="0.01" min="0.01" className="form-control" value={suma} onChange={e => setSuma(e.target.value)} required />
          </div>
          <div style={{ display: 'flex', alignItems: 'flex-end' }}>
            <button type="submit" className="btn btn-primary" disabled={!!loading}>
              {loading === 'sub' ? 'Se procesează...' : 'Încasează'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

// ===== Clients List Section =====
function ClientsListSection() {
  const [clients, setClients] = useState([])
  const [loading, setLoading] = useState(false)
  const [loaded, setLoaded] = useState(false)
  const [error, setError] = useState('')

  async function handleLoad() {
    setLoading(true)
    setError('')
    try {
      const data = await getAllClients()
      setClients(data)
      setLoaded(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-section">
      <h3>Listă Clienți</h3>
      {error && <div className="alert alert-error">{error}</div>}
      {!loaded ? (
        <button className="btn btn-outline" onClick={handleLoad} disabled={loading}>
          {loading ? 'Se încarcă...' : 'Încarcă Clienți'}
        </button>
      ) : (
        <>
          <div className="text-sm text-muted" style={{ marginBottom: 12 }}>Total: {clients.length} clienți</div>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nume</th>
                  <th>Tip</th>
                  <th>Abonament</th>
                  <th>RevPoints</th>
                  <th>Conturi</th>
                  <th>Dată înrolare</th>
                </tr>
              </thead>
              <tbody>
                {clients.map(c => (
                  <tr key={c.id}>
                    <td>{c.id}</td>
                    <td className="font-bold">{c.numeComplet}</td>
                    <td><span className="badge badge-neutral">{c.tip === 'PERSOANA_FIZICA' ? 'PF' : 'PJ'}</span></td>
                    <td><span className={`badge ${c.abonament === 'PREMIUM' ? 'badge-warning' : c.abonament === 'BUSINESS_PRO' ? 'badge-info' : 'badge-neutral'}`}>{c.abonament}</span></td>
                    <td>{c.puncteRev}</td>
                    <td>{c.conturi?.length || 0}</td>
                    <td className="text-sm">{c.dataInrolare}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}

export default function AdminPage() {
  const [tab, setTab] = useState('clients')

  return (
    <>
      <AdminNavbar />
      <div className="page-container">
        <div className="page-header">
          <div>
            <div className="page-title">Panou Administrator</div>
            <div className="page-subtitle">Gestionează clienții, conturile și operațiunile băncii</div>
          </div>
        </div>

        <div className="tabs" style={{ flexWrap: 'wrap' }}>
          <button className={`tab-btn ${tab === 'clients' ? 'active' : ''}`} onClick={() => setTab('clients')}>Clienți</button>
          <button className={`tab-btn ${tab === 'add-client' ? 'active' : ''}`} onClick={() => setTab('add-client')}>Adaugă Client</button>
          <button className={`tab-btn ${tab === 'edit-client' ? 'active' : ''}`} onClick={() => setTab('edit-client')}>Editează / Șterge</button>
          <button className={`tab-btn ${tab === 'account' ? 'active' : ''}`} onClick={() => setTab('account')}>Cont</button>
          <button className={`tab-btn ${tab === 'card' ? 'active' : ''}`} onClick={() => setTab('card')}>Card</button>
          <button className={`tab-btn ${tab === 'search' ? 'active' : ''}`} onClick={() => setTab('search')}>Caută Client</button>
          <button className={`tab-btn ${tab === 'ops' ? 'active' : ''}`} onClick={() => setTab('ops')}>Operațiuni</button>
        </div>

        {tab === 'clients' && <ClientsListSection />}
        {tab === 'add-client' && <AddClientSection />}
        {tab === 'edit-client' && (
          <>
            <EditClientSection />
            <DeleteClientSection />
          </>
        )}
        {tab === 'account' && (
          <>
            <OpenAccountSection />
            <CloseAccountSection />
          </>
        )}
        {tab === 'card' && (
          <>
            <EmitCardSection />
            <BlockCardSection />
          </>
        )}
        {tab === 'search' && <SearchClientSection />}
        {tab === 'ops' && <AdminOpsSection />}
      </div>
    </>
  )
}
