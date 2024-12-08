import axios from 'axios';

const API_BASE_URL = 'http://localhost:9091';

// Create axios instance with default config
const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Add a response interceptor for logging
axiosInstance.interceptors.response.use(
    response => {
        return response.data;
    },
    error => {
        console.error('API Response Error:', error.response ? error.response.data : error.message);
        throw error;
    }
);

const api = {
    // User endpoints
    async login(username, password) {
        try {
            const formData = new URLSearchParams();
            formData.append('username', username);
            formData.append('password', password);

            const response = await axiosInstance.post('/api/login', formData, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });
            return response;
        } catch (error) {
            throw error;
        }
    },

    async register(username, password) {
        try {
            const response = await axiosInstance.post('/api/register', { username, password });
            return response;
        } catch (error) {
            throw error;
        }
    },

    // Post endpoints
    async getPosts() {
        try {
            const response = await axiosInstance.get('/api/posts');
            return response;
        } catch (error) {
            throw error;
        }
    },

    async getPostById(id) {
        try {
            if (!id) {
                throw new Error('Post ID is required');
            }
            const response = await axiosInstance.get(`/api/posts/${id}`);
            if (!response.success) {
                throw new Error(response.error || 'Failed to fetch post');
            }
            return response.post;
        } catch (error) {
            throw error;
        }
    },

    async createPost(postData) {
        try {
            const response = await axiosInstance.post('/api/posts', postData);
            return response;
        } catch (error) {
            throw error;
        }
    },

    async updatePost(id, postData) {
        try {
            if (!id) {
                throw new Error('Post ID is required');
            }
            const response = await axiosInstance.put(`/api/posts/${id}`, postData);
            if (!response.success) {
                throw new Error(response.error || 'Failed to update post');
            }
            return response;
        } catch (error) {
            throw error;
        }
    },

    async deletePost(id) {
        try {
            const response = await axiosInstance.delete(`/api/posts/${id}`);
            return response;
        } catch (error) {
            throw error;
        }
    },

    async searchPosts(searchParams) {
        try {
            const params = new URLSearchParams();
            if (searchParams.title) params.append('title', searchParams.title);
            if (searchParams.author) params.append('author', searchParams.author);
            if (searchParams.tags?.length > 0) params.append('tags', searchParams.tags.join(','));

            console.log('Sending search request with params:', params.toString());
            const response = await axiosInstance.get(`/api/posts/search?${params.toString()}`);
            console.log('Response:', response);

            return response;
        } catch (error) {
            console.error('Search error:', error);
            throw new Error('Failed to search posts');
        }
    },

    // Comment endpoints
    async createComment(postId, content) {
        try {
            const response = await axiosInstance.post(`/api/posts/${postId}/comments`, { content });
            return response;
        } catch (error) {
            throw error;
        }
    },
};

export default api;
