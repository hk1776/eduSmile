package com.example.edusmile.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CounselEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String student;
    private int views;
    @Column(length = 10000)
    private String Counsel;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private String created;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private String updated;


    private String classId;

}
