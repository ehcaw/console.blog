import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Navbar from './components/Navbar';
import Home from './components/Home';
import PostList from './components/PostList';
import PostDetail from './components/PostDetail';
import CreatePost from './components/CreatePost';
import EditPost from './components/EditPost';
import UserProfile from './components/UserProfile';
import Register from './components/Register';
import Login from './components/Login';

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#90caf9',
    },
    secondary: {
      main: '#f48fb1',
    },
  },
});

function PrivateRoute({ children }) {
  const userStr = localStorage.getItem('user');
  const user = JSON.parse(userStr || 'null');
  const isLoggedIn = user && user.id;

  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

function App() {
  return (
    <ThemeProvider theme={darkTheme}>
      <CssBaseline />
      <Router>
        <div className="App">
          <Navbar />
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/posts" element={<PostList />} />
            <Route 
              path="/posts/:postId" 
              element={<PostDetail />} 
              loader={({ params }) => {
                console.log('Route params:', params);
                return null;
              }}
            />
            <Route path="/edit-post/:id" element={<EditPost />} />
            <Route path="/login" element={<Login />} />
            <Route 
              path="/create-post" 
              element={
                <PrivateRoute>
                  <CreatePost />
                </PrivateRoute>
              } 
            />
            <Route path="/profile/:id" element={<UserProfile />} />
            <Route path="/register" element={<Register />} />
          </Routes>
        </div>
      </Router>
    </ThemeProvider>
  );
}

export default App;
