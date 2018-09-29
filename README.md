# Project2018Servers

<h1>배재대 연합소프트웨어 프로젝트 공지</h1>

1. 소스코드만 업로드 하세요!!!

2. 그리고 제발 GIT에서 바로 받아서 프로젝트진행하지 마시고 클론 만들어서 복사본으로 따로 진행하세요!!!

3. 개발하다가 원본을 바꾸거나 삭제해야할땐 각자 개인마다 branch 만들어서 관리하세요!!! (절때 master branch에 머지 하지 말것!!!!!)

*그레들에 대한 설명임 by 이찬호

-기본 명령어.
    gradle run (프로젝트를 컴파일하고 메인 클래스의 main 함수를 실행함)    
    gradle assemble (프로젝트를 jar파일로 생성, 디렉토리는 build/lib/{프로젝트명}.jar)
    gradle build (프로젝트를 빌드 (잘 안쓸듯?))

- 루트 프로젝트는 nodeControlServer 폴더로 돼있으니 gradle 명령어 날릴때 꼭 경로 이동해주셈

- 메인 프로젝트에 추가로 프로젝트 추가할거 있으면 일단 나한테 알려주셈 답장이 너무 늦으면 아래보고 잘 해보시구..
    1. nodeControlServer폴더 하위에 프로젝트 폴더 생성하고...
    2. 해당 경로로 들어가 gradle init -type java-application 쳐주면 프로젝트를 알아서 생성해줌
    3. src/main/java 폴더에 열심히 코딩하고
    4. 그 프로젝트의 build.gradle 파일 열어서 플러그인이랑 main클래스 지정해줌
    5. 다시 돌아와 메인 프로젝트의 settings.gradle 파일열어서 include '{플젝폴더 이름}' 라인 추가.
    
- 프로젝트 실행은 경로 잡아주고 gradle run 명령어 날리면 대충 뜰거임
