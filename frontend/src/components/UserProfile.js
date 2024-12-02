import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import {
  Container,
  Typography,
  Paper,
  Box,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Avatar,
} from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import api from '../services/api';

function UserProfile() {
  const { id } = useParams();
  const [user, setUser] = useState(null);
  const [userPosts, setUserPosts] = useState([]);

  useEffect(() => {
    const fetchUserAndPosts = async () => {
      try {
        const [userResponse, postsResponse] = await Promise.all([
          api.getUserById(id),
          api.getPostsByAuthor(id),
        ]);
        setUser(userResponse.data);
        setUserPosts(postsResponse.data);
      } catch (error) {
        console.error('Error fetching user data:', error);
      }
    };

    fetchUserAndPosts();
  }, [id]);

  if (!user) {
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
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
          <Avatar
            sx={{ width: 100, height: 100, mr: 3 }}
          >
            {user.username[0].toUpperCase()}
          </Avatar>
          <Box>
            <Typography variant="h4" gutterBottom>
              {user.username}
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Member since: {new Date(user.registrationDate).toLocaleDateString()}
            </Typography>
          </Box>
        </Box>

        <Typography variant="h5" gutterBottom sx={{ mt: 4 }}>
          Posts by {user.username}
        </Typography>
        <Grid container spacing={4}>
          {userPosts.map((post) => (
            <Grid item xs={12} md={6} key={post.postId}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {post.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {post.content.substring(0, 100)}...
                  </Typography>
                  <Typography variant="caption" display="block" sx={{ mt: 1 }}>
                    Posted on: {new Date(post.postDate).toLocaleDateString()}
                  </Typography>
                </CardContent>
                <CardActions>
                  <Button
                    size="small"
                    component={RouterLink}
                    to={`/posts/${post.postId}`}
                  >
                    Read More
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Paper>
    </Container>
  );
}

export default UserProfile;
