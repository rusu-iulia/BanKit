import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import AccountDetailPage from './pages/AccountDetailPage'
import TransferPage from './pages/TransferPage'
import CardsPage from './pages/CardsPage'
import RewardsPage from './pages/RewardsPage'
import AdminPage from './pages/AdminPage'
import SettingsPage from './pages/SettingsPage'

function ProtectedRoute({ children }) {
  const { user, isAdmin, loading } = useAuth()
  if (loading) return <div className="loading">Se încarcă...</div>
  if (!user && !isAdmin) return <Navigate to="/login" replace />
  return children
}

function AdminRoute({ children }) {
  const { isAdmin, loading } = useAuth()
  if (loading) return <div className="loading">Se încarcă...</div>
  if (!isAdmin) return <Navigate to="/login" replace />
  return children
}

function AppRoutes() {
  const { user, isAdmin, loading } = useAuth()
  if (loading) return <div className="loading">Se încarcă...</div>

  return (
    <Routes>
      <Route
        path="/login"
        element={(user || isAdmin) ? <Navigate to={isAdmin ? '/admin' : '/'} replace /> : <LoginPage />}
      />
      <Route path="/" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
      <Route path="/accounts/:iban" element={<ProtectedRoute><AccountDetailPage /></ProtectedRoute>} />
      <Route path="/transfer" element={<ProtectedRoute><TransferPage /></ProtectedRoute>} />
      <Route path="/cards" element={<ProtectedRoute><CardsPage /></ProtectedRoute>} />
      <Route path="/rewards" element={<ProtectedRoute><RewardsPage /></ProtectedRoute>} />
      <Route path="/settings" element={<ProtectedRoute><SettingsPage /></ProtectedRoute>} />
      <Route path="/admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
