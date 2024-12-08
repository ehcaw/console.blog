import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  TextField,
  Button,
  Box,
  Paper,
  Alert,
  Chip,
  IconButton
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import api from '../services/api';

function EditPost() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [description, setDescription] = useState('');
  const [tags, setTags] = useState([]);
  const [newTag, setNewTag] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [initialized, setInitialized] = useState(false);

  // Get current user from localStorage
  const currentUser = JSON.parse(localStorage.getItem('user'));

  useEffect(() => {
    const fetchPost = async () => {
      if (!initialized) {
        try {
          const postData = await api.getPostById(id);
          if (!postData) {
            setError('Post not found');
            setLoading(false);
            return;
          }

          // Check if the current user is the author
          if (!currentUser || postData.author.id !== currentUser.id) {
            setError('You do not have permission to edit this post');
            setLoading(false);
            navigate('/posts');
            return;
          }

          setPost(postData);
          setTitle(postData.title);
          setContent(postData.content);
          setDescription(postData.description || '');
          setTags(postData.tags || []);
          setInitialized(true);
          setLoading(false);
        } catch (error) {
          console.error('Error fetching post:', error);
          setError(error.message || 'Failed to load post');
          setLoading(false);
        }
      }
    };

    fetchPost();
  }, [id, navigate, currentUser, initialized]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      if (!title.trim()) {
        setError('Title is required');
        return;
      }
      if (!content.trim()) {
        setError('Content is required');
        return;
      }

      const response = await api.updatePost(id, {
        title: title.trim(),
        content: content.trim(),
        description: description.trim(),
        tags
      });

      // Navigate with the updated post data
      navigate(`/posts/${id}`, {
        state: { post: response.post }
      });
    } catch (error) {
      console.error('Error updating post:', error);
      setError(error.message || 'Failed to update post');
    }
  };

  const handleAddTag = (e) => {
    e.preventDefault();
    if (newTag.trim() && !tags.includes(newTag.trim())) {
      setTags([...tags, newTag.trim()]);
      setNewTag('');
    }
  };

  const handleRemoveTag = (tagToRemove) => {
    setTags(tags.filter(tag => tag !== tagToRemove));
  };

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Typography>Loading...</Typography>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Edit Post
        </Typography>

        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 2 }}>
          <TextField
            label="Title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            fullWidth
            required
            sx={{ mb: 2 }}
          />

          <TextField
            label="Description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            fullWidth
            multiline
            rows={2}
            sx={{ mb: 2 }}
          />

          <TextField
            label="Content"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            fullWidth
            required
            multiline
            rows={10}
            sx={{ mb: 2 }}
          />

          <Box sx={{ mb: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              Tags
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 1 }}>
              {tags.map((tag, index) => (
                <Chip
                  key={index}
                  label={tag}
                  onDelete={() => handleRemoveTag(tag)}
                  color="primary"
                  variant="outlined"
                />
              ))}
            </Box>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <TextField
                label="Add tag"
                value={newTag}
                onChange={(e) => setNewTag(e.target.value)}
                size="small"
              />
              <Button onClick={handleAddTag} variant="outlined">
                Add
              </Button>
            </Box>
          </Box>

          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button
              type="button"
              onClick={() => navigate(`/posts/${id}`)}
              variant="outlined"
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              color="primary"
            >
              Save Changes
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
}

export default EditPost;
