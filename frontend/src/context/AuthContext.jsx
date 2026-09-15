import { createContext, useState, useContext, useEffect } from 'react'
import { getMe } from '../api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isAdmin, setIsAdmin] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getMe()
      .then(data => {
        if (data.role === 'admin') {
          setIsAdmin(true)
          setUser({ role: 'admin' })
        } else if (data.id) {
          setUser(data)
        }
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [])

  return (
    <AuthContext.Provider value={{ user, isAdmin, setUser, setIsAdmin, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
