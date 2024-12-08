import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  Box,
  Chip,
  CircularProgress
} from '@mui/material';
import api from '../services/api';

function PostList({ posts: propPosts, loading: propLoading, error: propError }) {
  const navigate = useNavigate();
  const [posts, setPosts] = useState(propPosts || []);
  const [loading, setLoading] = useState(propLoading || false);
  const [error, setError] = useState(propError || '');

  useEffect(() => {
    // If props are provided, use them
    if (propPosts !== undefined) {
      setPosts(propPosts);
      setLoading(propLoading || false);
      setError(propError || '');
      return;
    }

    // Otherwise, fetch posts
    const fetchPosts = async () => {
      try {
        setLoading(true);
        setError('');
        const response = await api.getPosts();
        
        if (!response.success) {
          throw new Error(response.error || 'Failed to fetch posts');
        }
        
        if (!Array.isArray(response.posts)) {
          console.error('Posts is not an array:', response.posts);
          setPosts([]);
          return;
        }

        setPosts(response.posts);
      } catch (error) {
        console.error('Error fetching posts:', error);
        setError(error.message || 'Failed to load posts');
        setPosts([]);
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, [propPosts, propLoading, propError]);

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Box display="flex" justifyContent="center">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Box>
          <Typography color="error">{error}</Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Blog Posts
      </Typography>

      <Box sx={{ mt: 2 }}>
        <List>
          {posts.length === 0 ? (
            <ListItem>
              <ListItemText primary="No posts found." />
            </ListItem>
          ) : (
            posts.map((post) => (
              <Paper 
                elevation={3} 
                sx={{ mb: 2, width: '100%' }} 
                key={post.id}
              >
                <ListItem 
                  button
                  onClick={() => navigate(`/posts/${post.id}`, { state: { post } })}
                  sx={{ 
                    display: 'flex', 
                    flexDirection: 'column', 
                    alignItems: 'flex-start',
                    p: 3,
                    '&:hover': {
                      backgroundColor: 'rgba(0, 0, 0, 0.04)'
                    }
                  }}
                >
                  <ListItemText
                    primary={
                      <Typography variant="h6" gutterBottom>
                        {post.title}
                      </Typography>
                    }
                    secondary={
                      <>
                        <Typography variant="body2" color="text.secondary" paragraph>
                          By {post.author?.username} on {new Date(post.createdAt).toLocaleDateString()}
                        </Typography>
                        <Typography variant="body2" paragraph>
                          {post.description || (post.content?.length > 200 ? `${post.content.substring(0, 200)}...` : post.content)}
                        </Typography>
                        {post.tags && post.tags.length > 0 && (
                          <Box sx={{ mt: 1 }}>
                            {post.tags.map((tag, index) => (
                              <Chip
                                key={index}
                                label={tag}
                                size="small"
                                sx={{ mr: 1, mb: 1 }}
                              />
                            ))}
                          </Box>
                        )}
                      </>
                    }
                  />
                </ListItem>
              </Paper>
            ))
          )}
        </List>
      </Box>
    </Container>
  );
}

export default PostList;
