import React, { useState } from 'react';
import { Box, TextField, Button, Typography, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const Login = () => {
    const [formData, setFormData] = useState({
        username: '',
        password: ''
    });
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const response = await api.login(formData);
            console.log('Login response:', response);
            if (response && response.userId) {
                localStorage.setItem('userId', response.userId);
                localStorage.setItem('username', response.username);
                navigate('/create-post');
            } else {
                setError('Invalid credentials');
            }
        } catch (err) {
            console.error('Login error:', err);
            setError('Invalid username or password');
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

            <TextField
                required
                fullWidth
                label="Username"
                name="username"
                value={formData.username}
                onChange={handleChange}
            />

            <TextField
                required
                fullWidth
                label="Password"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleChange}
            />

            <Button
                type="submit"
                variant="contained"
                color="primary"
                size="large"
                fullWidth
            >
                Login
            </Button>
        </Box>
    );
};

export default Login;
