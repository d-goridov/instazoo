package com.example.instazoo.service;

import com.example.instazoo.entity.Photo;
import com.example.instazoo.entity.Post;
import com.example.instazoo.entity.User;
import com.example.instazoo.exceptions.PhotoNotFoundException;
import com.example.instazoo.repository.PhotoRepository;
import com.example.instazoo.repository.PostRepository;
import com.example.instazoo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
public class PhotoService {

    public static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PhotoRepository photoRepository;

    public PhotoService(UserRepository userRepository, PostRepository postRepository, PhotoRepository photoRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.photoRepository = photoRepository;
    }


    public Photo uploadPhotoToUser(MultipartFile file, Principal principal) throws IOException {
        User user = getUserByPrincipal(principal);
        LOG.info("Uploading photo profile to User {}", user.getUsername());

        Photo profilePhoto = photoRepository.findByUserId(user.getId()).orElse(null);
        if (!ObjectUtils.isEmpty(profilePhoto)) {
            photoRepository.delete(profilePhoto);
        }

        Photo photo = new Photo();
        photo.setUserId(user.getId());
        photo.setPhotoBytes(compressBytes(file.getBytes()));
        photo.setName(file.getOriginalFilename());

        return photoRepository.save(photo);
    }

    public Photo uploadPhotoToPost(Long postId, MultipartFile file, Principal principal) throws IOException  {
        User user = getUserByPrincipal(principal);
        Post post = user.getPosts()
                .stream()
                .filter(p -> p.getId().equals(postId))
                .collect(toSinglePostCollector());

        Photo photo = new Photo();
        photo.setPostId(post.getId());
        photo.setPhotoBytes(file.getBytes());
        photo.setPhotoBytes(compressBytes(file.getBytes()));
        photo.setName(file.getOriginalFilename());
        LOG.info("Uploading photo to Post {}", post.getId());

        return photoRepository.save(photo);
    }

    public Photo getPhotoToUser(Principal principal) {
        User user = getUserByPrincipal(principal);
        Photo photo = photoRepository.findByUserId(user.getId()).orElse(null);
        if (!ObjectUtils.isEmpty(photo)) {
            photo.setPhotoBytes(decompressBytes(photo.getPhotoBytes()));
        }
        return photo;
    }

    public Photo getPhotoToPost(Long postId) {
        Photo photo = photoRepository.findByPostId(postId)
                .orElseThrow(() -> new PhotoNotFoundException("Cannot find image to Post: " + postId));
        if (!ObjectUtils.isEmpty(photo)) {
            photo.setPhotoBytes(decompressBytes(photo.getPhotoBytes()));
        }

        return photo;
    }



    private byte[] compressBytes(byte[] data) {
        Deflater compresser = new Deflater();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];

        while (!compresser.finished()) {
            int count = compresser.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            LOG.error("Cannot compress bytes");
        }

        System.out.println("Compressed photo byte size " + outputStream.toByteArray().length);
        return outputStream.toByteArray();
    }

    private byte[] decompressBytes(byte[] data) {
        Inflater decompresser = new Inflater();
        decompresser.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];

        try {
            while (!decompresser.finished()) {
                int count = decompresser.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException | DataFormatException df) {
            LOG.error("Cannot decompress bytes");
        }
        return outputStream.toByteArray();
    }


    /**
     * Метод возвращает один пост для user
     */
    private <T> Collector<T, ?, T> toSinglePostCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }


    private User getUserByPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found with username " + username));
    }
}
