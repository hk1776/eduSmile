package com.example.edusmile.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Subject {
    @Id
    @Column(name = "subject_id")
    private String id;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Attend> attends = new HashSet<>();

    private String subject;
    private int grade;

    @Column(name = "CLASS")
    private String divClass;

    // 편의 메서드
    public void addAttend(Attend attend) {
        attends.add(attend);
        attend.setSubject(this);
    }

    public void removeAttend(Attend attend) {
        attends.remove(attend);
        attend.setSubject(null);
    }
}
