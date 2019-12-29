# IoT 디바이스 클러스터 기반의 견고한 분산처리 플랫폼 구축

재난상황에서 중앙시스템의 통제를 받지 못하는 부분이 발생할 수 있습니다.

다수의 컴퓨팅 보드를 각 요소에 배치하고 이를 클러스터링 함으로서 중앙시스템을 대체, 제어 시스템의 분산도를 높여 통신이 단절돼도 부분적으로 관리 및 통제가 가능합니다.

이 저장소는 해당 프로젝트의 라즈베리 파이(즉, 컴퓨팅 보드)에 올라가는 분산 처리 환경 구축 시스템입니다.
 - 서버 없이 라즈베리파이끼리 통신하여 적절한 IP를 부여합니다.
 - 클러스터를 제어하는 마스터 노드를 자동으로 선출합니다.
 - 아파치 스파크를 이용하여 자동으로 분산 처리 환경을 구축합니다.
 - 주피터 노트북을 통해 각 노드의 상태 확인 및 파이썬 코딩을 할 수 있습니다.
 - 클러스터에 손상이 발생했을 때(즉, 네트워크가 단절되었을 때) 위의 과정을 반복하여 클러스터를 자동으로 재구성 합니다.
 - 두개 이상으로 분할된 네트워크가 다시 하나로 합쳐졌을 때도 위의 과정을 반복하여 하나의 클러스터로 재구성 합니다.

이미지는 Project2018Servers/mdImage/ 를 참조하세요


<h1>배재대 연합소프트웨어 프로젝트 공지</h1>

1. 소스코드만 업로드 하세요!!!

2. 그리고 제발 GIT에서 바로 받아서 프로젝트진행하지 마시고 클론 만들어서 복사본으로 따로 진행하세요!!!

3. 개발하다가 원본을 바꾸거나 삭제해야할땐 각자 개인마다 branch 만들어서 관리하세요!!! (절때 master branch에 머지 하지 말것!!!!!)

*개발장비 설정

    - 라즈베리파이는 raspbian-stretch-lite.img 이미지를 사용하여 USB-SEREAL 모듈로 개발할것
    - 라즈베리파이 기본 ID:pi PW:raspberry
    - 오랜지파이 armbian 기본 ID:orangepi PW:1234
    - 설치후 공통 비밀번호 설정: rhavkddl
    - 프로젝트 기본 계정(디렉토리): root(/root/Project2018Servers)
    1. apt-get install git, gradle, default-jdk
    2. 루트 홈으로 이동후 git clone https://github.com/dja12123/Project2018Servers.git
    3. /root/Project2018Servers 이동후 그래들 명령어를 사용할 수 있음
    4. 빌드한 프로젝트는 /root/Project2018Servers/builds/libs 에 있음
    * 패키지 설치가 불가능 할경우 apt-get update, apt-get upgrade
    
*개발장비 자동 설정 스크립트

    0. apt-get install git
    1. ~/Project2018Servers/nodeControlServer/extResource/Shscript/all_change_unix.sh
    2. ~/Project2018Servers/nodeControlServer/extResource/Shscript/init_rasp_pi.sh
    - 스크립트가 하는일 : 저장소 KAIST로 바꿔줌, apt-get update, upgrade 실행, gradle, default-jdk 설치
    
*완전 자동 설정 스크립트

    1. apt-get install git 후 프로젝트 받고 2번 진행 할 것.
    2. ~/Project2018Servers/nodeControlServer/extResource/Shscript/automatic_install_final.sh
    
    

*그래들 명령어

    - gradle clean build (프로젝트를 빌드 ./build/lib/{프로젝트 이름}.jar파일 생성)
    - gradle run (프로젝트를 컴파일하고 기본 클래스를 실행)
    - gradle buildtest -PmainClass={실행할클래스명}(ex. node.NodeControlCore) (테스트용jar 컴파일)
    - gradle moveres (nodeControlServers/extResources/의 모든 파일들을 jar파일이 있는 위치로 이동)
   
   
*ML관련 라이브러리 설치

    - ~/Project2018Servers/nodeControlServer/extResource/Shscript/ml_lib_install.sh
   
*Spark 설치

    1. ~/Project2018Servers/nodeControlServer/extResource/Shscript/all_change_unix.sh
    2. ~/Project2018Servers/nodeControlServer/extResource/Shscript/install_spark.sh /opt/
    3. 시스템 재부팅
    4. source /etc/bash.bashrc
    
    
*Spark 삭제
    
    1. sh ~/Project2018Servers/nodeControlServer/extResource/Shscript/uninstall_spark.sh /opt/
    2. 재부팅
    3. source /etc/bash.bashrc

※ Spark 관련 명령어 안될시 sh 를 맨앞에 붙여줄것!

프로젝트 자동빌드&실행 (git pull도 자동으로 해줌)

    sh ~/Project2018Servers/nodeControlServer/extResource/Shscript/auto_build_proj.sh 
    java -jar ~/Project2018Servers/build/libs/Project2018Servers.jar
