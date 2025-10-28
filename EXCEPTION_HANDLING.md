# 예외 처리(Exception Handling) 가이드

## 개요

프로젝트에서 모든 예외는 중앙화된 `GlobalExceptionHandler`에서 처리되며, 일관된 에러 응답 형식을 반환합니다.

---

## 에러 응답 형식

모든 에러 응답은 다음과 같은 형식을 따릅니다:

```json
{
  "status": 401,
  "message": "JWT 토큰이 만료되었습니다.",
  "code": "JWT_EXPIRED",
  "timestamp": "2025-10-28T10:30:45.123456",
  "path": "/api/protected-resource"
}
```

### 응답 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| status | int | HTTP 상태 코드 |
| message | String | 에러 메시지 |
| code | String | 에러 코드 (프로그래밍 방식의 식별자) |
| timestamp | String | 에러 발생 시간 (ISO 8601 형식) |
| path | String | 요청한 API 경로 |

---

## 인증/인가 관련 예외

### 1. JwtException - JWT 관련 기본 예외
**HTTP 상태:** 401 Unauthorized

```java
throw new JwtException("JWT 처리 중 오류가 발생했습니다.");
```

**응답 예시:**
```json
{
  "status": 401,
  "message": "JWT 처리 중 오류가 발생했습니다.",
  "code": "JWT_ERROR",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/auth/login"
}
```

---

### 2. JwtExpiredException - JWT 토큰 만료
**HTTP 상태:** 401 Unauthorized
**에러 코드:** JWT_EXPIRED

```java
throw new JwtExpiredException();
// 또는
throw new JwtExpiredException("JWT 토큰이 만료되었습니다.");
```

**응답 예시:**
```json
{
  "status": 401,
  "message": "JWT 토큰이 만료되었습니다.",
  "code": "JWT_EXPIRED",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/protected-resource"
}
```

**발생 시나리오:**
- 로그인 후 24시간 이상 경과한 토큰으로 API 호출
- Authorization 헤더의 만료된 토큰 사용

---

### 3. InvalidJwtException - 유효하지 않은 JWT 토큰
**HTTP 상태:** 401 Unauthorized
**에러 코드:** INVALID_JWT

```java
throw new InvalidJwtException();
// 또는
throw new InvalidJwtException("유효하지 않은 JWT 토큰입니다.");
```

**응답 예시:**
```json
{
  "status": 401,
  "message": "유효하지 않은 JWT 토큰입니다.",
  "code": "INVALID_JWT",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/protected-resource"
}
```

**발생 시나리오:**
- 변조된 토큰
- 잘못된 형식의 토큰
- 서명이 유효하지 않은 토큰
- 지원하지 않는 JWT 알고리즘

---

### 4. AuthenticationException - 인증 실패
**HTTP 상태:** 401 Unauthorized
**에러 코드:** AUTHENTICATION_FAILED

```java
throw new AuthenticationException("아이디 또는 비밀번호가 잘못되었습니다.");
```

**응답 예시:**
```json
{
  "status": 401,
  "message": "아이디 또는 비밀번호가 잘못되었습니다.",
  "code": "AUTHENTICATION_FAILED",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/auth/login"
}
```

**발생 시나리오:**
- 존재하지 않는 아이디로 로그인 시도
- 비밀번호 불일치
- 사용자 계정이 비활성화된 상태

---

### 5. AccessDeniedException - 접근 권한 거부
**HTTP 상태:** 403 Forbidden
**에러 코드:** ACCESS_DENIED

```java
throw new AccessDeniedException("이 리소스에 접근할 권한이 없습니다.");
```

**응답 예시:**
```json
{
  "status": 403,
  "message": "이 리소스에 접근할 권한이 없습니다.",
  "code": "ACCESS_DENIED",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/admin/users"
}
```

**발생 시나리오:**
- 관리자 권한이 필요한 API에 일반 사용자가 접근
- 특정 역할(Role)이 필요한 리소스에 권한이 없는 사용자가 접근

---

### 6. UnauthorizedException - 인증 필요
**HTTP 상태:** 401 Unauthorized
**에러 코드:** UNAUTHORIZED

```java
throw new UnauthorizedException("인증이 필요합니다.");
```

**응답 예시:**
```json
{
  "status": 401,
  "message": "인증이 필요합니다.",
  "code": "UNAUTHORIZED",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/protected-resource"
}
```

**발생 시나리오:**
- 토큰 없이 보호된 리소스 접근
- Authorization 헤더가 없는 상태로 API 호출

---

## 기타 비즈니스 예외

### 7. DuplicateResourceException - 중복된 리소스
**HTTP 상태:** 409 Conflict
**에러 코드:** DUPLICATE_RESOURCE

```java
throw new DuplicateResourceException("사용자", "username", "user123");
// 응답: "사용자가 이미 존재합니다. (username: user123)"
```

**응답 예시:**
```json
{
  "status": 409,
  "message": "사용자가 이미 존재합니다. (username: user123)",
  "code": "DUPLICATE_RESOURCE",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/auth/signup"
}
```

---

### 8. ResourceNotFoundException - 리소스를 찾을 수 없음
**HTTP 상태:** 404 Not Found
**에러 코드:** RESOURCE_NOT_FOUND

```java
throw new ResourceNotFoundException("사용자", "id", 123);
// 응답: "사용자를 찾을 수 없습니다. (id: 123)"
```

**응답 예시:**
```json
{
  "status": 404,
  "message": "사용자를 찾을 수 없습니다. (id: 123)",
  "code": "RESOURCE_NOT_FOUND",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/users/123"
}
```

---

### 9. BusinessException - 일반 비즈니스 로직 오류
**HTTP 상태:** 400 Bad Request
**에러 코드:** BUSINESS_ERROR

```java
throw new BusinessException("재고가 부족합니다.");
```

**응답 예시:**
```json
{
  "status": 400,
  "message": "재고가 부족합니다.",
  "code": "BUSINESS_ERROR",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/orders"
}
```

---

## 유효성 검사 예외

### 10. MethodArgumentNotValidException - 요청 데이터 검증 실패
**HTTP 상태:** 400 Bad Request
**에러 코드:** VALIDATION_ERROR

**응답 예시:**
```json
{
  "status": 400,
  "message": "요청 데이터 검증 실패: username: 필수 항목입니다, password: 필수 항목입니다",
  "code": "VALIDATION_ERROR",
  "timestamp": "2025-10-28T10:30:45",
  "path": "/api/auth/login"
}
```

**발생 시나리오:**
- 필수 필드 누락
- 데이터 타입 불일치
- 커스텀 유효성 검사 실패

---

## 예외 처리 흐름도

```
┌─────────────────────────────────────┐
│  Exception 발생                      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  GlobalExceptionHandler 캡처         │
└──────────────┬──────────────────────┘
               │
       ┌───────┴────────────────────────────┐
       │                                    │
       ▼                                    ▼
  Custom Exception             Standard Exception
  (명시적 처리)                  (일반 예외)
       │                                    │
       ▼                                    ▼
  ErrorResponse                   500 Internal Server Error
  (적절한 상태코드)                    (일반 에러 응답)
       │                                    │
       └────────────────┬───────────────────┘
                        │
                        ▼
          ┌──────────────────────────┐
          │  클라이언트에게 응답     │
          └──────────────────────────┘
```

---

## 개발자 가이드

### Exception 선택 기준

| 상황 | 사용할 Exception | HTTP 상태 |
|------|-----------------|----------|
| JWT 토큰이 만료됨 | JwtExpiredException | 401 |
| JWT 토큰이 유효하지 않음 | InvalidJwtException | 401 |
| 로그인 실패 (아이디/비밀번호 오류) | AuthenticationException | 401 |
| 접근 권한 없음 | AccessDeniedException | 403 |
| 인증 정보가 필요함 | UnauthorizedException | 401 |
| 중복된 데이터 | DuplicateResourceException | 409 |
| 리소스를 찾을 수 없음 | ResourceNotFoundException | 404 |
| 기타 비즈니스 오류 | BusinessException | 400 |

### 사용 예시

**회원가입 서비스:**
```java
@Service
public class AuthService {
    public void signUp(SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("사용자", "username", request.getUsername());
        }
        // ... 회원가입 로직
    }
}
```

**로그인 서비스:**
```java
@Service
public class AuthService {
    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByUsername(request.getUsername());

        if (user.isEmpty()) {
            throw new AuthenticationException("아이디 또는 비밀번호가 잘못되었습니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            throw new AuthenticationException("아이디 또는 비밀번호가 잘못되었습니다.");
        }

        // ... JWT 토큰 생성 로직
    }
}
```

**보호된 API:**
```java
@GetMapping("/api/protected")
public ResponseEntity<?> getProtectedResource() {
    // JwtAuthenticationFilter가 토큰을 검증하고
    // JwtExpiredException, InvalidJwtException을 throw하면
    // GlobalExceptionHandler가 처리함
    return ResponseEntity.ok("Protected data");
}
```

---

## 로깅

모든 Exception은 적절한 로그 레벨로 기록됩니다:

- **ERROR**: 예상치 못한 서버 에러 (Exception 타입)
- **WARN**: 비즈니스 로직 상의 예외 (Custom Exception)
- **DEBUG**: 정상적인 인증 처리 (JwtAuthenticationFilter)

로그를 통해 문제를 추적하고 모니터링할 수 있습니다.

---

## 참고 사항

1. **Exception은 Service 계층에서 throw**: Controller에서 직접 throw하지 않도록 주의
2. **구체적인 Exception 사용**: 일반적인 Exception보다는 구체적인 Custom Exception 사용
3. **의미 있는 메시지**: 사용자가 이해할 수 있는 명확한 에러 메시지 작성
4. **보안 고려**: 민감한 정보는 에러 메시지에 포함하지 않기
