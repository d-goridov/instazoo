package com.example.instazoo.controller;

import com.example.instazoo.entity.Photo;
import com.example.instazoo.payload.response.MessageResponse;
import com.example.instazoo.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("api/photo")
@CrossOrigin
public class PhotoController {
    private final PhotoService photoService;

    @Autowired
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<MessageResponse> uploadPhotoToUser(@RequestParam("file")MultipartFile file,
                                                             Principal principal) throws IOException {
        photoService.uploadPhotoToUser(file, principal);
        return new ResponseEntity<>(new MessageResponse("Photo upload successfully"), HttpStatus.OK);
    }

    @PostMapping("/{postId}/upload")
    public ResponseEntity<MessageResponse> uploadPhotoForPost(@PathVariable("postId") String postId,
                                                              @RequestParam("file")MultipartFile file,
                                                              Principal principal) throws IOException {
        Photo photo = photoService.uploadPhotoToPost(Long.parseLong(postId), file, principal);
        return new ResponseEntity<>(new MessageResponse("Photo for post upload successfully"), HttpStatus.OK);
    }

    @GetMapping("/profilePhoto")
    public ResponseEntity<Photo> getPhotoForUser(Principal principal) {
        Photo photo = photoService.getPhotoToUser(principal);
        return new ResponseEntity<>(photo, HttpStatus.OK);
    }

    @GetMapping("/{postId}/photo")
    public ResponseEntity<Photo> getPhotoForPost(@PathVariable("postId") String postId) {
        Photo photo = photoService.getPhotoToPost(Long.parseLong(postId));
        return new ResponseEntity<>(photo, HttpStatus.OK);
    }
}
