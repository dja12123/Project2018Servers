# Project2018Servers

<h1>배재대 연합소프트웨어 프로젝트 공지</h1>

1. 소스코드만 업로드 하세요!!!

2. 그리고 제발 GIT에서 바로 받아서 프로젝트진행하지 마시고 클론 만들어서 복사본으로 따로 진행하세요!!!

3. 개발하다가 원본을 바꾸거나 삭제해야할땐 각자 개인마다 branch 만들어서 관리하세요!!! (절때 master branch에 머지 하지 말것!!!!!)

*개발장비 설정
    git clone 

*그래들 명령어

    gradle clean build (프로젝트를 빌드 ./build/libs/{프로젝트 이름}.jar파일 생성)
    gradle run (프로젝트를 컴파일하고 기본 클래스를 실행)
    gradle buildtest -PmainClass={실행할클래스명}(ex. node.NodeControlCore) (테스트용jar 컴파일)
    gradle moveres (nodeControlServers/extResources/의 모든 파일들을 jar파일이 있는 위치로 이동)
    
   
*Spark 설치

    1. extResource/Shscript/all_change_unix.sh 실행
    2. extResource/Shscript/install_spark.sh /opt/ 실행
    3. source /etc/bash.bashrc 실행 or 시스템 재부팅
    4. gradle moveres 후 빌드해서 프로젝트 실행
    
    
*Spark 삭제
    
    1. extResource/Shscript/uninstall_spark.sh /opt/ 실행
    2. 재부팅 
