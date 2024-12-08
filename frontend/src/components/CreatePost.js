import React, { useState } from 'react';
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
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [content, setContent] = useState('');
  const [tags, setTags] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      // Convert tags string to array
      const tagsArray = tags
        .split(',')
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0);

      const response = await api.createPost({
        title,
        description,
        content,
        tags: tagsArray
      });

      if (response.success) {
        // Redirect to the list of posts or the newly created post
        navigate('/');
      } else {
        setError(response.error || 'Failed to create post');
      }
    } catch (err) {
      console.error('Post creation error:', err);
      setError(err.response?.data?.error || err.message || 'An error occurred while creating the post');
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
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            fullWidth
            required
            sx={{ mb: 2 }}
            error={!title}
            helperText={!title ? 'Title is required' : ''}
          />

          <TextField
            name="description"
            label="Description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            fullWidth
            required
            multiline
            rows={2}
            sx={{ mb: 2 }}
            error={!description}
            helperText={!description ? 'Description is required' : ''}
          />

          <TextField
            name="content"
            label="Content"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            fullWidth
            required
            multiline
            rows={8}
            sx={{ mb: 2 }}
            error={!content}
            helperText={!content ? 'Content is required' : ''}
          />

          <TextField
            name="tags"
            label="Tags (comma-separated)"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            fullWidth
            sx={{ mb: 2 }}
          />

          <Button
            type="submit"
            variant="contained"
            color="primary"
            size="large"
            disabled={!title || !description || !content}
          >
            Create Post
          </Button>
        </Box>
      </Paper>
    </Container>
  );
}

export default CreatePost;
