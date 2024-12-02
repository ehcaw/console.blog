import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Paper,
  Box,
  Button,
  Divider,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import api from '../services/api';

function PostDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [author, setAuthor] = useState(null);

  useEffect(() => {
    const fetchPostAndAuthor = async () => {
      try {
        const postResponse = await api.getPostById(id);
        setPost(postResponse.data);

        const authorResponse = await api.getUserById(postResponse.data.authorId);
        setAuthor(authorResponse.data);
      } catch (error) {
        console.error('Error fetching post details:', error);
      }
    };

    fetchPostAndAuthor();
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete this post?')) {
      try {
        await api.deletePost(id);
        navigate('/posts');
      } catch (error) {
        console.error('Error deleting post:', error);
      }
    }
  };

  if (!post || !author) {
    return (
      <Container>
        <Box sx={{ my: 4 }}>
          <Typography>Loading...</Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container>
      <Paper sx={{ my: 4, p: 4 }}>
        <Typography variant="h3" gutterBottom>
          {post.title}
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" gutterBottom>
          By {author.username} • {new Date(post.postDate).toLocaleDateString()}
        </Typography>
        <Divider sx={{ my: 2 }} />
        <Typography variant="body1" paragraph>
          {post.content}
        </Typography>
        <Box sx={{ mt: 4, display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
            color="primary"
            startIcon={<EditIcon />}
            onClick={() => navigate(`/edit-post/${post.postId}`)}
          >
            Edit
          </Button>
          <Button
            variant="contained"
            color="error"
            startIcon={<DeleteIcon />}
            onClick={handleDelete}
          >
            Delete
          </Button>
        </Box>
      </Paper>
    </Container>
  );
}

export default PostDetail;
