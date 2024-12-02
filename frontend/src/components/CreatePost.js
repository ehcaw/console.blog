import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Paper,
  TextField,
  Button,
  Box,
  Alert,
} from '@mui/material';
import api from '../services/api';

function CreatePost() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    authorId: null
  });
  const [error, setError] = useState('');

  useEffect(() => {
    const userId = localStorage.getItem('userId');
    if (!userId) {
      navigate('/login');
      return;
    }
    setFormData(prev => ({
      ...prev,
      authorId: parseInt(userId)
    }));
  }, [navigate]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.authorId) {
      setError('You must be logged in to create a post');
      navigate('/login');
      return;
    }

    try {
      console.log('Submitting post:', formData);
      const response = await api.createPost(formData);
      console.log('Post created:', response);
      navigate(`/posts/${response.postId}`);
    } catch (error) {
      console.error('Error creating post:', error);
      setError(
        error.response?.data || 
        error.message || 
        'Error creating post. Please try again.'
      );
    }
  };

  return (
    <Container maxWidth="md">
      <Paper sx={{ my: 4, p: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Create New Post
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            name="title"
            label="Title"
            value={formData.title}
            onChange={handleChange}
            fullWidth
            required
            sx={{ mb: 2 }}
            error={!formData.title}
            helperText={!formData.title ? 'Title is required' : ''}
          />

          <TextField
            name="content"
            label="Content"
            value={formData.content}
            onChange={handleChange}
            fullWidth
            required
            multiline
            rows={8}
            sx={{ mb: 2 }}
            error={!formData.content}
            helperText={!formData.content ? 'Content is required' : ''}
          />

          <Button
            type="submit"
            variant="contained"
            color="primary"
            size="large"
            disabled={!formData.title || !formData.content}
          >
            Create Post
          </Button>
        </Box>
      </Paper>
    </Container>
  );
}

export default CreatePost;
