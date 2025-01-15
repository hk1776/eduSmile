document.addEventListener("DOMContentLoaded", function () {
    // 데이터 정의
    const data = {
        findId: "아이디 찾기",
        findPw: "비밀번호 찾기",
        idLabel: "ID",
        pwLabel: "PW",
        studentSignup: "학생 회원가입",
        teacherSignup: "교사 회원가입",
        iconSrc: "_40.png",
        rectangle2Src: "rectangle-20.svg",
        rectangle5Src: "rectangle-50.svg",
        aiDescription: "AI 수업 보조 서비스",
        aiEdu: "에듀",
        aiSmile: "스마일",
        rectangle4Src: "rectangle-40.svg",
        loginText: "로그인",
        imageSrc: "image-10-removebg-preview-20.png"
    };

    // Mustache 템플릿 가져오기
    const template = document.getElementById("template").innerHTML;

    // Mustache로 HTML 렌더링
    const rendered = Mustache.render(template, data);

    // 결과를 DOM에 추가
    document.getElementById("app").innerHTML = rendered;
});