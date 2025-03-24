ALTER TABLE Likes
    ADD CONSTRAINT uk_user_post UNIQUE (user_id, post_id);
