package com.example.instazoo.service;

import com.example.instazoo.dto.PostDTO;
import com.example.instazoo.entity.Photo;
import com.example.instazoo.entity.Post;
import com.example.instazoo.entity.User;
import com.example.instazoo.exceptions.PostNotFoundException;
import com.example.instazoo.repository.PhotoRepository;
import com.example.instazoo.repository.PostRepository;
import com.example.instazoo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    public static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, PhotoRepository photoRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
    }

    public Post createPost(PostDTO postDTO, Principal principal) {
        User user = getUserByPrincipal(principal);
        Post post = new Post();
        post.setUser(user);
        post.setTitle(postDTO.getTitle());
        post.setDescription(postDTO.getCaption());
        post.setLocation(postDTO.getLocation());
        post.setLikes(0);

        LOG.info("Saving Post for User: {}", user.getEmail());

        return postRepository.save(post);

    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedDateDesc();
    }

    public Post getPostById(Long postId, Principal principal) {
        User user = getUserByPrincipal(principal);
        return postRepository.findPostByIdAndUser(postId, user)
                .orElseThrow(() -> new PostNotFoundException("Post cannot find for user: " + user.getEmail()));
    }

    public List<Post> getPostsByUser(Principal principal) {
            User user = getUserByPrincipal(principal);
            return  postRepository.findAllByUserOrderByCreatedDateDesc(user);
    }

    public Post likePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post cannot be found"));

        Optional<String> userLiked = post.getLikeUsers()
                .stream()
                .filter(u -> u.equals(username))
                .findAny();

        if (userLiked.isPresent()) {
            post.setLikes(post.getLikes() - 1);
            post.getLikeUsers().remove(username);
        } else {
            post.setLikes(post.getLikes() + 1);
            post.getLikeUsers().add(username);
        }
        return postRepository.save(post);
    }

    public void deletePost(Long postId, Principal principal) {
        Post post = getPostById(postId, principal);
        Optional<Photo> photo = photoRepository.findByPostId(post.getId());
        postRepository.delete(post);
        photo.ifPresent(photoRepository::delete);
    }


    private User getUserByPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found with username " + username));
    }
}
