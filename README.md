



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
      • 리뷰 기능 개발 <br/>
      • Gemini API를 활용한 비속어 필터링 기능 구현<br/>
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
      • 결제 기능 개발 <br/>
      • toss payments API 연동<br/>
    </td>
  </tr>
</table>

## 📐 ERD



## 📍 기능
### 1. 🧑🏻‍💻 일반 사용자 기능 (Customer)

| 핵심 기능 | 상세 설명 및 특징 |
| :--- | :--- |
| **회원 관리** | 회원가입, 로그인, 마이페이지, 개인 정보 수정. |
| **정보 조회** | 가게 정보 및 음식 메뉴 상세 조회. |
| **주문/결제** | 장바구니 관리, 주문 생성 및 결제 처리. |
| **주문 관리** | 주문 내역 조회 및 주문 취소 (5분 내 제한). |
| **리뷰 작성** | 평점 포함 리뷰 작성 (CRUD) |
| **배송/시스템** | 배송은 결제 완료 후에만 시작 가능. |

### 2. 🏪 사장님 기능 (Owner)

| 핵심 기능 | 상세 설명 및 특징 |
| :--- | :--- |
| **회원 관리** | 회원가입, 로그인. |
| **가게/메뉴 관리** | 가게 등록 및 메뉴 등록/수정/삭제. |
| **주문 처리** | 들어온 주문 목록 조회 및 주문 접수/취소 처리. |
| **리뷰 조회** | 본인 가게에 달린 리뷰 목록 조회. |

### 3. ⚙️ 관리자 기능 (Admin)

| 핵심 기능 | 상세 설명 및 특징 |
| :--- | :--- |
| **가게 승인** | 신규 가게 등록 요청 승인 및 거절 처리. |
| **사용자 관리** | 블랙리스트 관리 (사용자 상태를 BLOCK으로 변경). |


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

