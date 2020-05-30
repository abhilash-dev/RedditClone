package com.abhilash.redditclone.repo;

import com.abhilash.redditclone.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubredditRepo extends JpaRepository<Subreddit, Long> {
}
