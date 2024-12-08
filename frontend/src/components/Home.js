import React, { useState, useEffect } from 'react';
import { Container, Typography, Box } from '@mui/material';
import PostList from './PostList';
import SearchBar from './SearchBar';
import api from '../services/api';

function Home() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchPosts = async (searchParams = {}) => {
    try {
      setLoading(true);
      setError('');
      console.log('Fetching posts with params:', searchParams);
      
      const response = Object.keys(searchParams).length === 0 
        ? await api.getPosts()
        : await api.searchPosts(searchParams);

      console.log('Received response:', response);
      
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

  useEffect(() => {
    fetchPosts();
  }, []);

  const handleSearch = (searchParams) => {
    fetchPosts(searchParams);
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Blog Posts
        </Typography>
        <SearchBar onSearch={handleSearch} />
        <PostList 
          posts={posts} 
          loading={loading} 
          error={error} 
        />
      </Box>
    </Container>
  );
}

export default Home;
