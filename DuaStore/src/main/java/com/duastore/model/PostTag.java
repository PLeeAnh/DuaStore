package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PostTags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100, unique = true)
    private String tenTag;

    @Column(length = 300)
    private String slug;
}
