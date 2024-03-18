## 🙌 Hello. Pyre is always with you!
<img src="https://cdn.discordapp.com/attachments/1214849763745202176/1214850895133679616/pyre.png?ex=65fa9d10&is=65e82810&hm=0824d809c6b9297212831b1bcac723e24bf93b2199ffbcb665e84092034a133d&" alt="drawing" width="400"/>

#### 현재 파이어는 미완성 프로젝트입니다.
#### [Github 조직](https://github.com/Pyre-org)

# PyreAuth
## 프로젝트 구조
<img src="https://cdn.discordapp.com/attachments/393025698907947009/1219254633839722546/image.png?ex=660aa25d&is=65f82d5d&hm=5a8e8dabb5bff05518f90b40ab7d84f57a7e55faf7f71f7053fb5bc4d485afcd&" alt="drawing" width="600"/>

## 기술 스택
- Java 21
- Spring Boot (3.2.2)
- Spring Web
- Spring Data JPA
- Spring Cloud eureka client
- Spring Cloud config
- Spring cloud Open Feign

- Spring Boot oauth2-client
- Jwt
- Spring Boot Security
- Redis

- Mysql
- AWS S3  

  
## 어스 서비스
- 유저의 회원가입, 로그인 등 인증 및 인가를 관리합니다.
- Oauth2도 함께 관리합니다.
- JWT (access token + refresh token) 발급 및 Redis를 통해 refresh를 저장합니다.
- 저장된 리프레시 토큰은 1번 사용되면 다시 재발급 + 재저장 됩니다.
- 이미지 업로드를 위한 S3 업로드 서비스가 포함되어 있습니다.
- 유저 프로필 관리 및 이메일 서비스도 함께 포함되어 있습니다.

## 구현 중 이슈
