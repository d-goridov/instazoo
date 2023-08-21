package com.example.instazoo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String name;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photoBytes;
    @JsonIgnore
    private Long userId;
    @JsonIgnore
    private Long postId;
}
