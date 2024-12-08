import React, { useState } from 'react';
import { Box, Typography, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [usernameError, setUsernameError] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const navigate = useNavigate();

    const validateUsername = () => {
        if (!username) {
            setUsernameError('Username is required');
        } else {
            setUsernameError('');
        }
    };

    const validatePassword = () => {
        if (!password) {
            setPasswordError('Password is required');
        //} else if (password.length < 8) {
           // setPasswordError('Password must be at least 8 characters long');
        } else {
            setPasswordError('');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        validateUsername();
        validatePassword();

        if (usernameError || passwordError) {
            return;
        }

        console.log('Login Attempt:', { 
            username, 
            passwordLength: password.length 
        });

        try {
            const response = await api.login(username, password);
            console.log('Login response:', response);
            
            if (response.success) {
                localStorage.setItem('user', JSON.stringify(response.user));
                navigate('/');
            } else {
                setError(response.error || 'Login failed');
            }
        } catch (err) {
            console.error('Full login error:', err);
            
            if (err.response) {
                console.error('Error response data:', err.response.data);
                setError(err.response.data.error || 'Login failed');
            } else if (err.request) {
                console.error('No response received:', err.request);
                setError('No response from server');
            } else {
                console.error('Error setting up request:', err.message);
                setError('An unexpected error occurred');
            }
        }
    };

    return (
        <Box
            component="form"
            onSubmit={handleSubmit}
            sx={{
                display: 'flex',
                flexDirection: 'column',
                gap: 2,
                maxWidth: 400,
                mx: 'auto',
                mt: 4,
                p: 3,
                bgcolor: 'background.paper',
                borderRadius: 1,
            }}
        >
            <Typography variant="h4" component="h1" gutterBottom>
                Login
            </Typography>

            {error && <Alert severity="error">{error}</Alert>}

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                    <Typography>Username:</Typography>
                    <Box sx={{ width: '100%' }}>
                        <input 
                            type="text" 
                            name="username"
                            value={username} 
                            onChange={(e) => setUsername(e.target.value)} 
                            onBlur={validateUsername}
                            required 
                            style={{
                                width: '100%',
                                height: '40px',
                                padding: '10px',
                                fontSize: '16px',
                                border: '1px solid #ccc',
                                borderRadius: '5px',
                                backgroundColor: 'inherit',
                                color: 'inherit'
                            }}
                        />
                        {usernameError && <Typography color="error">{usernameError}</Typography>}
                    </Box>
                </Box>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                    <Typography>Password:</Typography>
                    <Box sx={{ width: '100%' }}>
                        <input 
                            type="password" 
                            name="password"
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)} 
                            onBlur={validatePassword}
                            required 
                            style={{
                                width: '100%',
                                height: '40px',
                                padding: '10px',
                                fontSize: '16px',
                                border: '1px solid #ccc',
                                borderRadius: '5px',
                                backgroundColor: 'inherit',
                                color: 'inherit'
                            }}
                        />
                        {passwordError && <Typography color="error">{passwordError}</Typography>}
                    </Box>
                </Box>
                <button
                    type="submit"
                    style={{
                        width: '100%',
                        padding: '10px',
                        fontSize: '16px',
                        backgroundColor: '#1976d2',
                        color: 'white',
                        border: 'none',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        marginTop: '20px'
                    }}
                >
                    Login
                </button>
            </Box>
        </Box>
    );
}

export default Login;
