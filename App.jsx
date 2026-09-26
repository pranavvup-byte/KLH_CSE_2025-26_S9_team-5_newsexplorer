import { Routes, Route, useLocation } from 'react-router-dom'
import { useEffect } from 'react'
import Header from './components/Header.jsx'
import Toast from './components/Toast.jsx'
import { useToastListener } from './hooks/useToast.js'
import Home from './pages/Home.jsx'
import Explore from './pages/Explore.jsx'
import ArticleDetails from './pages/ArticleDetails.jsx'
import Categories from './pages/Categories.jsx'
import CategoryDetail from './pages/CategoryDetail.jsx'
import Recommendations from './pages/Recommendations.jsx'
import Saved from './pages/Saved.jsx'
import HowItWorks from './pages/HowItWorks.jsx'

function ScrollToTop() {
  const { pathname } = useLocation()
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])
  return null
}

export default function App() {
  const toastMessage = useToastListener()

  return (
    <>
      <ScrollToTop />
      <Header />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/explore" element={<Explore />} />
          <Route path="/article/:id" element={<ArticleDetails />} />
          <Route path="/categories" element={<Categories />} />
          <Route path="/categories/:name" element={<CategoryDetail />} />
          <Route path="/recommendations" element={<Recommendations />} />
          <Route path="/saved" element={<Saved />} />
          <Route path="/how-it-works" element={<HowItWorks />} />
        </Routes>
      </main>
      <Toast message={toastMessage} />
    </>
  )
}
