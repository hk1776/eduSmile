package com.example.edusmile.Dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter   //dto 맵핑 하려면 setter 필수
public class FindIdDto {

    private String name;
    private String phoneNumber;


}
