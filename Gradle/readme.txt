*그레들에 대한 설명임

- 루트 프로젝트는 nodeControlServer 폴더로 돼있으니 gradle 명령어 날릴때 꼭 경로 이동해주셈

- 메인 프로젝트에 추가로 프로젝트 추가할거 있으면 일단 나한테 알려주셈 답장이 너무 늦으면 아래보고 잘 해보시구..
    1. nodeControlServer폴더 하위에 프로젝트 폴더 생성하고...
    2. 해당 경로로 들어가 gradle init -type java-application 쳐주면 프로젝트를 알아서 생성해줌
    3. src/main/java 폴더에 열심히 코딩하고
    4. 그 프로젝트의 build.gradle 파일 열어서 플러그인이랑 main클래스 지정해줌
    5. 다시 돌아와 메인 프로젝트의 settings.gradle 파일열어서 include '{플젝폴더 이름}' 라인 추가.
    
- 프로젝트 실행은 경로 잡아주고 gradle run 명령어 날리면 대충 뜰거임
