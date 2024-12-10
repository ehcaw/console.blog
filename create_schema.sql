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
    ),
    (
        'techie99',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
    ),
    (
        'coder42',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
    ),
    (
        'webdev21',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
    ),
    (
        'pythonista',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
    ),
    (
        'debugger',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
    ),
    (
        'devops_pro',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
    ),
    (
        'algo_master',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
    ),
    (
        'code_ninja',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
    ),
    (
        'bug_hunter',
        '5487cf596c53bf12e05cec7d9e2b719478cba212eb9e146e927900b48825f872'
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
    ),
    (
        4,
        'Python for Data Science',
        'Introduction to data analysis with Python',
        'Learn how to use Python for data analysis...'
    ),
    (
        5,
        'React Hooks Explained',
        'Modern React development',
        'Understanding React Hooks and their usage...'
    ),
    (
        6,
        'Docker Basics',
        'Container basics for beginners',
        'Getting started with Docker containerization...'
    ),
    (
        7,
        'GraphQL vs REST',
        'API design comparison',
        'Comparing GraphQL and REST APIs...'
    ),
    (
        8,
        'CSS Grid Layout',
        'Modern CSS layouts',
        'Master CSS Grid for responsive designs...'
    ),
    (
        9,
        'TypeScript Benefits',
        'Why use TypeScript',
        'Advantages of TypeScript in large projects...'
    ),
    (
        1,
        'Git Workflow',
        'Version control best practices',
        'Professional Git workflow strategies...'
    ),
    (
        2,
        'MongoDB Basics',
        'NoSQL database intro',
        'Getting started with MongoDB...'
    ),
    (
        3,
        'AWS Services',
        'Cloud computing overview',
        'Introduction to key AWS services...'
    ),
    (
        4,
        'Vue.js Components',
        'Component architecture',
        'Building reusable Vue.js components...'
    ),
    (
        5,
        'Node.js Async',
        'Asynchronous JavaScript',
        'Managing async operations in Node.js...'
    ),
    (
        6,
        'Redis Caching',
        'Implementation guide',
        'Using Redis for application caching...'
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
    ('best-practices'),
    ('python'),
    ('data-science'),
    ('react'),
    ('docker'),
    ('graphql'),
    ('css'),
    ('typescript'),
    ('git'),
    ('mongodb'),
    ('aws'),
    ('vue'),
    ('nodejs'),
    ('redis'),
    ('frontend'),
    ('backend');

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
    (3, 6),
    (4, 7), -- Python post - python tag
    (4, 8), -- Python post - data-science tag
    (5, 9), -- React post - react tag
    (5, 14), -- React post - frontend tag
    (6, 10), -- Docker post - docker tag
    (7, 11), -- GraphQL post - graphql tag
    (8, 12), -- CSS post - css tag
    (8, 14), -- CSS post - frontend tag
    (9, 13), -- TypeScript post - typescript tag
    (10, 14), -- Git post - git tag
    (11, 15), -- MongoDB post - mongodb tag
    (12, 16), -- AWS post - aws tag
    (13, 17), -- Vue post - vue tag
    (14, 18), -- Node.js post - nodejs tag
    (15, 19);

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
    ),
    (
        4,
        5,
        'Great Python tutorial! Very comprehensive.'
    ),
    (4, 6, 'Could you cover pandas library next?'),
    (5, 7, 'React Hooks really simplified my code.'),
    (5, 8, 'Excellent explanation of useEffect!'),
    (6, 9, 'Docker makes deployment so much easier.'),
    (6, 1, 'Would love to see Kubernetes next.'),
    (
        7,
        2,
        'GraphQL has been a game-changer for our API.'
    ),
    (7, 3, 'Nice comparison with REST.'),
    (8, 4, 'CSS Grid is so powerful for layouts.'),
    (
        8,
        5,
        'Better than using flexbox for grid layouts.'
    ),
    (
        9,
        6,
        'TypeScript caught so many bugs in our project.'
    ),
    (10, 7, 'Git workflow explanation was very clear.'),
    (
        11,
        8,
        'MongoDB vs PostgreSQL comparison would be nice.'
    ),
    (12, 9, 'AWS services overview was helpful.'),
    (13, 1, 'Vue.js components are so intuitive.');
