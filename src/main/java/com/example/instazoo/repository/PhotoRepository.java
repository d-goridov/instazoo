package com.example.instazoo.repository;

import com.example.instazoo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Optional<Photo> findByUserId(Long userId);
    Optional<Photo> findByPostId(Long postId);
}
