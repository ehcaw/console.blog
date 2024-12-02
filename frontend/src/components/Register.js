import React, { useState } from 'react';
import { Box, TextField, Button, Typography, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const Register = () => {
    const [formData, setFormData] = useState({
        username: '',
        password: ''
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
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
        setSuccess('');

        try {
            const response = await api.createUser(formData);
            console.log('Registration response:', response);
            if (response && response.userId) {
                setSuccess('Registration successful! Redirecting to create post...');
                localStorage.setItem('userId', response.userId);
                setTimeout(() => {
                    navigate('/create-post');
                }, 2000);
            } else {
                setError('Invalid response from server');
            }
        } catch (err) {
            console.error('Registration error:', err);
            setError(err.response?.data || 'Error during registration');
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
                Register
            </Typography>

            {error && <Alert severity="error">{error}</Alert>}
            {success && <Alert severity="success">{success}</Alert>}

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
                Register
            </Button>
        </Box>
    );
};

export default Register;
