CREATE TABLE Posts (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(255),
                       content TEXT,
                       created_at TIMESTAMP DEFAULT NOW(),
                       user_id INT NOT NULL,
                       like_count int DEFAULT 0,
                       is_trending BOOLEAN,
                       CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
