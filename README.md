



## 📄 프로젝트 소개
프로젝트 "저기요"는 사용자와 사장님 모두를 위한 양방향 주문 관리 플랫폼입니다.<br>
사용자는 위치 기반으로 가까운 매장을 찾아 편리하게 주문하고, 사장님은 매장과 메뉴를 효율적으로 관리하며 주문을 실시간으로 처리할 수 있습니다.



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



## 📍 기능



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


## 📈 성능 최적화 전략

데이터베이스 최적화

- QueryDSL:
  - 동적 쿼리로 불필요한 조회 최소화
- Soft Delete:
  - 데이터 이력 보존
  - 복구 가능성 확보
  - 통계 데이터 유지

비동기 처리

- 이벤트 기반 평점 계산:
  - 리뷰 등록/수정/삭제 시 이벤트 발행
  - 별도 스레드에서 평점 재계산
  - 사용자 응답 속도 향상

API 안정성

- Fallback 전략: API 실패 시 기본값 제공
- 로깅: 문제 발생 시 빠른 추적


> 개발 기간 : 2025.10.29 ~ 2025.11.10

