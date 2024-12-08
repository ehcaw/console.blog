import React from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Container,
  Box
} from '@mui/material';
import CreateIcon from '@mui/icons-material/Create';

function Navbar() {
  const navigate = useNavigate();
  const userStr = localStorage.getItem('user');
  const user = JSON.parse(userStr || 'null');
  const isLoggedIn = user && user.id;

  console.log('Navbar - User string:', userStr);
  console.log('Navbar - Parsed user:', user);
  console.log('Navbar - Is logged in:', isLoggedIn);

  const handleLogout = () => {
    localStorage.removeItem('user');
    navigate('/');
    window.location.reload();
  };

  return (
    <AppBar position="static">
      <Container>
        <Toolbar>
          <Typography
            variant="h6"
            component={RouterLink}
            to="/"
            sx={{
              flexGrow: 1,
              textDecoration: 'none',
              color: 'inherit',
              fontWeight: 700,
            }}
          >
            Console.Blog
          </Typography>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Button
              color="inherit"
              component={RouterLink}
              to="/posts"
              sx={{ mx: 1 }}
            >
              POSTS
            </Button>
            {isLoggedIn && (
              <Button
                color="inherit"
                component={RouterLink}
                to="/create-post"
                sx={{ mx: 1 }}
                startIcon={<CreateIcon />}
              >
                CREATE
              </Button>
            )}
            {!isLoggedIn ? (
              <>
                <Button
                  color="inherit"
                  component={RouterLink}
                  to="/login"
                >
                  LOGIN
                </Button>
                <Button
                  color="inherit"
                  component={RouterLink}
                  to="/register"
                >
                  REGISTER
                </Button>
              </>
            ) : (
              <Button
                color="inherit"
                onClick={handleLogout}
              >
                LOGOUT
              </Button>
            )}
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
}

export default Navbar;
