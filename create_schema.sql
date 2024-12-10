DROP TABLE IF EXISTS comments;

DROP TABLE IF EXISTS post_tags;

DROP TABLE IF EXISTS tags;

DROP TABLE IF EXISTS posts;

DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create posts table
CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(50),
    content VARCHAR(2500) NOT NULL,
    user_id INTEGER NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create tags table
CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Create post_tags junction table
CREATE TABLE post_tags (
    post_id INTEGER NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    tag_id INTEGER NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

-- Create comments table
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    content VARCHAR(250) NOT NULL,
    post_id INTEGER NOT NULL REFERENCES posts (id),
    user_id INTEGER NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample users (passwords are hashed version of 'password123')
INSERT INTO
    users (username, password)
VALUES
    (
        'demouser',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
    ),
    (
        'ryan1',
        '5c9626e9d4ad08cc8db460432728e63eb7193c5b3977fd1e9598a105ea788b76'
    ),
    (
        'ape',
        'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3'
    ),
    (
        'ape2',
        '173af653133d964edfc16cafe0aba33c8f500a07f3ba3f81943916910c257705'
    ),
    (
        'ape1',
        '173af653133d964edfc16cafe0aba33c8f500a07f3ba3f81943916910c257705'
    ),
    (
        'ape3',
        '173af653133d964edfc16cafe0aba33c8f500a07f3ba3f81943916910c257705'
    ),
    (
        'ape4',
        '173af653133d964edfc16cafe0aba33c8f500a07f3ba3f81943916910c257705'
    ),
    (
        'ape5',
        'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3'
    );

-- Insert sample posts
INSERT INTO
    posts (user_id, title, description, content)
VALUES
    (
        1,
        'Getting Started with Java',
        'A beginner''s guide to Java programming',
        'Java is a versatile programming language that has been around for decades...\n\nHere are some key concepts to get you started:\n1. Object-Oriented Programming\n2. Variables and Data Types\n3. Control Flow'
    ),
    (
        2,
        'Web Development Best Practices',
        'Essential tips for modern web development',
        'Modern web development requires attention to several key areas:\n\n- Performance\n- Security\n- Accessibility\n- Responsive Design'
    ),
    (
        3,
        'Database Design Tips',
        'How to structure your database effectively',
        'Good database design is crucial for application performance...\n\nKey principles include:\n- Normalization\n- Indexing\n- Relationship Planning'
    );

-- Insert sample tags
INSERT INTO
    tags (name)
VALUES
    ('java'),
    ('programming'),
    ('web-development'),
    ('databases'),
    ('tutorial'),
    ('best-practices');

-- Link tags to posts
INSERT INTO
    post_tags (post_id, tag_id)
VALUES
    (1, 1), -- Java post - java tag
    (1, 2), -- Java post - programming tag
    (1, 5), -- Java post - tutorial tag
    (2, 3), -- Web dev post - web-development tag
    (2, 6), -- Web dev post - best-practices tag
    (3, 4), -- Database post - databases tag
    (3, 6);

-- Database post - best-practices tag
-- Insert sample comments
INSERT INTO
    comments (post_id, user_id, content)
VALUES
    (
        1,
        2,
        'Great introduction to Java! Very helpful for beginners.'
    ),
    (
        1,
        3,
        'Could you add more examples about inheritance?'
    ),
    (2, 1, 'Security section was particularly useful.'),
    (2, 3, 'Don''t forget about HTTPS!'),
    (
        3,
        1,
        'Good overview of database design principles.'
    ),
    (
        3,
        2,
        'Would love to see more about indexing strategies.'
    );
