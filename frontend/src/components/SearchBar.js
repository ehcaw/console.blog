import React, { useState } from 'react';
import {
  Box,
  TextField,
  IconButton,
  Chip,
  Paper,
  InputAdornment,
  Stack
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';

function SearchBar({ onSearch }) {
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [tagInput, setTagInput] = useState('');
  const [tags, setTags] = useState([]);

  const handleSearch = () => {
    onSearch({
      title: title.trim(),
      author: author.trim(),
      tags: tags
    });
  };

  const handleClear = () => {
    setTitle('');
    setAuthor('');
    setTagInput('');
    setTags([]);
    onSearch({});
  };

  const handleAddTag = (e) => {
    if (e.key === 'Enter' && tagInput.trim()) {
      if (!tags.includes(tagInput.trim())) {
        setTags([...tags, tagInput.trim()]);
      }
      setTagInput('');
    }
  };

  const handleRemoveTag = (tagToRemove) => {
    setTags(tags.filter(tag => tag !== tagToRemove));
  };

  return (
    <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
      <Stack spacing={2}>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField
            fullWidth
            label="Search by title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            InputProps={{
              endAdornment: title && (
                <InputAdornment position="end">
                  <IconButton size="small" onClick={() => setTitle('')}>
                    <ClearIcon />
                  </IconButton>
                </InputAdornment>
              )
            }}
          />
          <TextField
            fullWidth
            label="Search by author"
            value={author}
            onChange={(e) => setAuthor(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            InputProps={{
              endAdornment: author && (
                <InputAdornment position="end">
                  <IconButton size="small" onClick={() => setAuthor('')}>
                    <ClearIcon />
                  </IconButton>
                </InputAdornment>
              )
            }}
          />
        </Box>

        <Box>
          <TextField
            fullWidth
            label="Add tags (press Enter)"
            value={tagInput}
            onChange={(e) => setTagInput(e.target.value)}
            onKeyPress={handleAddTag}
            InputProps={{
              endAdornment: tagInput && (
                <InputAdornment position="end">
                  <IconButton size="small" onClick={() => setTagInput('')}>
                    <ClearIcon />
                  </IconButton>
                </InputAdornment>
              )
            }}
          />
          <Box sx={{ mt: 1, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {tags.map((tag, index) => (
              <Chip
                key={index}
                label={tag}
                onDelete={() => handleRemoveTag(tag)}
              />
            ))}
          </Box>
        </Box>

        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
          <IconButton onClick={handleClear} color="error">
            <ClearIcon />
          </IconButton>
          <IconButton onClick={handleSearch} color="primary">
            <SearchIcon />
          </IconButton>
        </Box>
      </Stack>
    </Paper>
  );
}

export default SearchBar;
