import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { logout, getRewards, redeemReward } from '../api'

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
        <Link to="/rewards" className="active">Recompense</Link>
        <Link to="/settings">Setări</Link>
        <button onClick={handleLogout}>Deconectare</button>
      </div>
    </nav>
  )
}

function RewardItem({ reward, userPoints, onRedeem, redeeming }) {
  const canAfford = userPoints >= reward.costPuncte

  return (
    <div className="reward-card">
      <div className="reward-icon">{reward.tip === 'ZBOR' ? '✈' : '🏨'}</div>
      <div className="reward-name">{reward.numeOferta}</div>
      <div className="reward-details">
        {reward.tip === 'ZBOR' ? (
          <>
            <span>{reward.info1}</span> &rarr; <span>{reward.info2}</span>
          </>
        ) : (
          <>
            {reward.info1} &bull; {reward.info2} nopți
          </>
        )}
      </div>
      <div className="reward-cost">{reward.costPuncte.toLocaleString('ro-RO')} RevPoints</div>

      {reward.claimed ? (
        <span className="badge badge-success">Revendicat</span>
      ) : canAfford ? (
        <button
          className="btn btn-accent btn-sm"
          onClick={() => onRedeem(reward.idOferta)}
          disabled={redeeming === reward.idOferta}
        >
          {redeeming === reward.idOferta ? 'Se procesează...' : 'Revendică'}
        </button>
      ) : (
        <span className="badge badge-neutral">Îți lipsesc {(reward.costPuncte - userPoints).toLocaleString('ro-RO')} puncte</span>
      )}
    </div>
  )
}

export default function RewardsPage() {
  const { user, setUser } = useAuth()
  const [rewards, setRewards] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')
  const [redeeming, setRedeeming] = useState(null)

  function loadRewards() {
    setLoading(true)
    getRewards()
      .then(setRewards)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadRewards() }, [])

  async function handleRedeem(idOferta) {
    setRedeeming(idOferta)
    setMsg('')
    setError('')
    try {
      await redeemReward(idOferta)
      setMsg('Recompensa a fost revendicată cu succes!')
      const reward = rewards.find(r => r.idOferta === idOferta)
      if (reward && user) {
        setUser({ ...user, puncteRev: (user.puncteRev || 0) - reward.costPuncte })
      }
      loadRewards()
    } catch (err) {
      setError(err.message)
    } finally {
      setRedeeming(null)
    }
  }

  const flights = rewards.filter(r => r.tip === 'ZBOR')
  const accommodations = rewards.filter(r => r.tip === 'CAZARE')
  const userPoints = user?.puncteRev || 0

  return (
    <>
      <Navbar />
      <div className="page-container">
        <div className="welcome-section">
          <div>
            <h1>Catalog Recompense</h1>
            <p>Folosește RevPoints-urile tale pentru călătorii și sejururi</p>
          </div>
          <div className="rev-points-badge">
            <div className="points-value">{userPoints.toLocaleString('ro-RO')}</div>
            <div className="points-label">RevPoints disponibili</div>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {msg && <div className="alert alert-success">{msg}</div>}

        {loading ? (
          <div className="text-muted">Se încarcă recompensele...</div>
        ) : (
          <>
            <div className="section-title">Călătorii cu avionul ({flights.length})</div>
            <div className="grid-3 mb-6">
              {flights.map(r => (
                <RewardItem
                  key={r.idOferta}
                  reward={r}
                  userPoints={userPoints}
                  onRedeem={handleRedeem}
                  redeeming={redeeming}
                />
              ))}
            </div>

            <div className="section-title">Cazări ({accommodations.length})</div>
            <div className="grid-3">
              {accommodations.map(r => (
                <RewardItem
                  key={r.idOferta}
                  reward={r}
                  userPoints={userPoints}
                  onRedeem={handleRedeem}
                  redeeming={redeeming}
                />
              ))}
            </div>
          </>
        )}
      </div>
    </>
  )
}
