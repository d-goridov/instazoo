package com.example.instazoo.facade;

import com.example.instazoo.dto.PostDTO;
import com.example.instazoo.entity.Post;
import org.springframework.stereotype.Component;

@Component
public class PostFacade {
    public PostDTO postToPostDTO(Post post) {
        PostDTO postDTO = new PostDTO();
        postDTO.setUsername(post.getUser().getUsername());
        postDTO.setId(post.getId());
        postDTO.setTitle(post.getTitle());
        postDTO.setCaption(post.getDescription());
        postDTO.setLocation(post.getLocation());
        postDTO.setLikes(postDTO.getLikes());
        postDTO.setUsersLiked(post.getLikeUsers());

        return postDTO;
    }
}
