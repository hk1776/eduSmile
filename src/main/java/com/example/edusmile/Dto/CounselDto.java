package com.example.edusmile.Dto;

import com.example.edusmile.Entity.CounselEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class CounselDto {


    private String title;
    private String counsel;

    private String student;


    private String classId;


    public CounselEntity toEntity() {
        return new CounselEntity(null,title,student,1,counsel,null,null,classId);
    }
}
