CREATE TABLE Likes (
                         id SERIAL PRIMARY KEY,
                         user_id INT NOT NULL,
                         post_id INT NOT NULL,
                         created_at TIMESTAMP DEFAULT NOW(),
                         CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT fk_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);
