import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import api from '../services/api';
import { 
  Typography, 
  Container, 
  Paper, 
  Button, 
  TextField, 
  Box, 
  Chip,
  Card,
  CardContent,
  IconButton
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

function PostDetail() {
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [newCommentContent, setNewCommentContent] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { postId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  // Get current user from localStorage
  const currentUser = JSON.parse(localStorage.getItem('user'));

  useEffect(() => {
    const fetchPostDetails = async () => {
      try {
        if (!postId) {
          setError('Post ID is missing');
          setLoading(false);
          return;
        }

        let postData;
        if (location.state?.post && location.state.post.id === parseInt(postId)) {
          // Use the post data from navigation state if available
          postData = location.state.post;
        } else {
          // Otherwise fetch from API
          const response = await api.getPostById(parseInt(postId));
          if (!response) {
            throw new Error('Post not found');
          }
          postData = response;
        }

        setPost(postData);
        setComments(postData.comments || []);
        setError(null);
      } catch (error) {
        console.error('Error fetching post details:', error);
        setError(error.message || 'Failed to load post');
      } finally {
        setLoading(false);
      }
    };

    setLoading(true);
    fetchPostDetails();
  }, [postId, location.state]);

  const handleEdit = () => {
    navigate(`/edit-post/${postId}`);
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this post?')) {
      return;
    }

    try {
      const response = await api.deletePost(postId);
      if (response.success) {
        navigate('/');
      } else {
        throw new Error(response.error || 'Failed to delete post');
      }
    } catch (error) {
      console.error('Error deleting post:', error);
      alert(error.message || 'Failed to delete post');
    }
  };

  const handleAddComment = async () => {
    if (!newCommentContent.trim()) {
      return;
    }

    try {
      const response = await api.createComment(postId, newCommentContent.trim());
      if (response.success && response.comment) {
        setComments([...comments, response.comment]);
        setNewCommentContent('');
      } else {
        throw new Error(response.error || 'Failed to add comment');
      }
    } catch (error) {
      console.error('Error adding comment:', error);
      alert(error.message || 'Failed to add comment');
    }
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
        <Typography color="error">{error}</Typography>
      </Container>
    );
  }

  if (!post) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Typography>Post not found</Typography>
      </Container>
    );
  }

  const isAuthor = currentUser && post.author && post.author.id === currentUser.id;

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Typography variant="h4" component="h1">
            {post.title}
          </Typography>
          {isAuthor && (
            <Box>
              <IconButton onClick={handleEdit} color="primary">
                <EditIcon />
              </IconButton>
              <IconButton onClick={handleDelete} color="error">
                <DeleteIcon />
              </IconButton>
            </Box>
          )}
        </Box>
        <Typography variant="subtitle1" color="text.secondary" gutterBottom>
          By {post.author?.username} on {new Date(post.createdAt).toLocaleDateString()}
        </Typography>
        <Box sx={{ my: 2 }}>
          {post.tags && post.tags.map((tag, index) => (
            <Chip
              key={index}
              label={tag}
              size="small"
              sx={{ mr: 1, mb: 1 }}
            />
          ))}
        </Box>
        <Typography variant="body1" paragraph>
          {post.content}
        </Typography>
      </Paper>

      {/* Comments Section */}
      <Paper elevation={2} sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Comments
        </Typography>
        
        {/* Add Comment Form */}
        {currentUser && (
          <Box sx={{ mb: 3 }}>
            <TextField
              fullWidth
              multiline
              rows={3}
              variant="outlined"
              placeholder="Write a comment..."
              value={newCommentContent}
              onChange={(e) => setNewCommentContent(e.target.value)}
              sx={{ mb: 1 }}
            />
            <Button
              variant="contained"
              color="primary"
              onClick={handleAddComment}
              disabled={!newCommentContent.trim()}
            >
              Add Comment
            </Button>
          </Box>
        )}

        {/* Comments List */}
        {comments.map((comment) => (
          <Card key={comment.id} sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                {comment.author?.username} • {new Date(comment.createdAt).toLocaleDateString()}
              </Typography>
              <Typography variant="body2">
                {comment.content}
              </Typography>
            </CardContent>
          </Card>
        ))}

        {comments.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            No comments yet
          </Typography>
        )}
      </Paper>
    </Container>
  );
}

export default PostDetail;
