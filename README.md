## 📚 목차

1. [프로젝트 소개](#📄-프로젝트-소개)
2. [팀원](#🙋🏻-팀원)
3. [ERD](#📁-ERD)
4. [아키텍쳐](#📊-아키텍쳐)
5. [기능](#📍-기능)
6. [기술 스택](#🛠-기술-스택)

## 📄 프로젝트 소개

"저기요"는 기존 배달 플랫폼의 핵심 기능을 벤치마킹하여 개발된 백엔드 중심의 주문 관리 서비스입니다.<br>
사용자가 위치 기반으로 가까운 매장을 탐색하고 편리하게 주문하는 경험과, 사장님이 매장 및 메뉴 관리는 물론 실시간으로 주문을 효율적으로 처리할 수 있는 양방향 시스템 구축에 중점을 두었습니다.

## 🙋🏻 팀원

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/SeongJJun">
        <img width="80" height="150" src="https://github.com/SeongJJun.png" alt="박성준"/>
        <br/><strong>박성준</strong><br/>
        <a>(LEADER)</a>
      </a>
    </td>
    <td align="left">
      • 주문 기능 개발<br/>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/sojsnake">
        <img width="80" height="150" src="https://github.com/sojsnake.png" alt="박소정"/>
        <br/><strong>박소정</strong><br/>
        <a>(MEMBER)</a>
      </a>
    </td>
    <td align="left">
      • 리뷰 기능 개발<br/>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/hoonssac">
        <img width="80" height="150" src="https://github.com/hoonssac.png" alt="서상훈"/>
        <br/><strong>서상훈</strong><br/>
        <a>(MEMBER)</a>
      </a>
    </td>
    <td align="left">
      • JWT 기반 인증/인가 구현<br/>
      • Gemini API를 활용한 메뉴 설명 생성 기능 개발<br/>
      • Naver Geocoding API를 활용한 위치 기반 정렬 기능 구현<br/>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/vsuminv">
        <img width="80" height="150" src="https://github.com/vsuminv.png" alt="이수민"/>
        <br/><strong>이수민</strong><br/>
        <a>(MEMBER)</a>
      </a>
    </td>
    <td align="left">
      • 결제 관리<br/>
    </td>
  </tr>
</table>

## 📁 ERD

<img width="1020" height="664" alt="Image" src="https://github.com/user-attachments/assets/ab66d20f-9742-4105-86c4-92821ae2bb3f" />

## 📊 아키텍쳐

<img width="750" height="357" alt="Image" src="https://github.com/user-attachments/assets/3122e5b6-b880-4fac-b8af-b954f33372c5" />

## 📍 기능

### 1️⃣ 사용자 (USER)

1. 위치 기반 매장 검색
    - 주소 기반 좌표 변환 : 사용자가 입력한 주소를 자동으로 위도/경도로 변환
    - 거리순 정렬 : 사용자 위치에서 가까운 매장부터 표시
    - 평점순 정렬 : 높은 평점 매장 우선 표시
    - 실시간 거리 표시 : 각 매장까지의 실제 거리(km) 표시


2. 주문 관리
    - 주문 생성
    - 주문 취소 : 경우에 따른 취소 기능
        - 주문 후 5분 이내 - 환불 없이 취소 가능
        - 결제 후 취소 - 환불 필요
    - 주문 내역 조회 : 최신순 전체 조회, 상세 조회, 주문 상태별 조회
        - 주문 상태 : 접수, 결제 완료, 조리중, 조리 완료, 배달중, 배달완료, 주문 완료, 주문 취소, 환불


3. 결제 관리
    - 결제 요청
    - 결제 취소: 주문 후 5분 이내 취소 시 결제까지 취소
    - 결제 상태 구분 : 성공, 실패


4. 리뷰 관리
    - 리뷰 등록/수정/삭제
    - AI 비속어 필터링 : Gemini API 활용하여 리뷰 등록, 수정 시 비속어 존재 여부 확인 후 숨김처리
    - 본인의 리뷰 조회 : 상태(숨김, 삭제)별, 평점순, 최신순, 전체, 상세 조회
    - 매장별 리뷰 조회 : 평점 높은순, 평점 낮은 순, 최신순 조회

### 2️⃣ 가게 주인 (OWNER)

1. 매장 관리
    - 매장 등록 : 사업자번호, 주소, 카테고리 등록
    - 영업 정보 관리 : 매장명, 설명, 카테고리 수정
    - 매장 중복 등록 방지


2. 메뉴 관리
    - 메뉴 등록/수정/삭제 : CRUD 기능
    - AI 메뉴 설명 사동 생성 : Gemini API 활용, 메뉴명만 입력하면 매력적인 설명 자동 생성


3. 주문 관리
    - 주문 목록 조회 : 본인 가게의 주문 전체 조회, 상세 조회
    - 주문 상태 변경 : 주문 진행 상황에 따른 상태 변경
        - 주문수락 또는 주문거절 > 조리완료 > 배달중 > 배달완료 + 주문완료


4. 결제 관리
    - 주문 거절 : 사장님이 주문 거절 시 결제 취소


5. 리뷰 관리
    - 리뷰 조회 : 본인의 매장에 등록된 리뷰 조회
    - 정렬 : 평점 높은 순, 평점 낮은 순, 최신순 조회

### 3️⃣ 관리자 (MANAGER)

1. 사용자 관리
    - 전체 회원 조회 : 페이징 처리된 회원 목록
    - 권한 변경 : USER <-> OWNER <-> ADMIN <-> BLOCK
    - 계정 정지 : 부적절한 사용자 차단


2. 매장 관리
    - 전체 매장 조회 : 삭제된 매장 포함 조회 가능
    - 매장 삭제 : 부적절한 매장 제재


3. 주문 관리
    - 전체 주문 조회 : 주문 완료 상태인 주문 전체 목록 조회, 상세 조회


4. 리뷰 관리
    - 리뷰 조회 : 상태(차단, 숨김, 삭제)별 리뷰 조회, 전체 조회, 상세 조회
    - 리뷰 삭제, 숨김 처리

## 🛠 기술 스택

Backend Core

- Framework: Spring Boot 3.x
- 언어: Java 17
- ORM: JPA/Hibernate
- 쿼리: QueryDSL (복잡한 검색 쿼리 최적화)
- DB: MySQL (Production), H2 (Test)
- 보안: Spring Security + JWT

External Integration

- Naver Maps Geocoding API: 주소 ↔ 좌표 변환
- Google Gemini API:
    - AI 메뉴 설명 생성
    - 리뷰 비속어 필터링
- TossPayments API: 안전한 결제 처리

DevOps & Tools

- 빌드: Gradle
- 테스트: JUnit 5, MockMvc
- API 문서: Swagger/OpenAPI 3.0
- 컨테이너: Docker
- CI/CD: GitHub Actions
- 로깅: SLF4J + Logback

> 개발 기간 : 2025.10.29 ~ 2025.11.10

