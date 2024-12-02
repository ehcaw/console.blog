import axios from 'axios';

const API_BASE_URL = 'http://localhost:8090/api';

// Create axios instance with default config
const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

const api = {
    // User endpoints
    createUser: async (userData) => {
        console.log('Sending registration request:', userData);
        const response = await axiosInstance.post('/users', userData);
        console.log('Registration response:', response);
        return response.data;
    },
    login: async (credentials) => {
        const response = await axiosInstance.post('/login', credentials);
        return response.data;
    },
    getUsers: () => axiosInstance.get('/users'),
    getUserById: (id) => axiosInstance.get(`/users/${id}`),

    // Post endpoints
    createPost: (postData) => axiosInstance.post('/posts', postData),
    getPosts: () => axiosInstance.get('/posts'),
    getPostById: (id) => axiosInstance.get(`/posts/${id}`),
    updatePost: (id, postData) => axiosInstance.put(`/posts/${id}`, postData),
    deletePost: (id) => axiosInstance.delete(`/posts/${id}`),
    getPostsByAuthor: (authorId) => axiosInstance.get(`/users/${authorId}/posts`),
};

export default api;
