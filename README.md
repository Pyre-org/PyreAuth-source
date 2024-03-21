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
- Open API [링크](https://apis.pyre.live/auth-service/swagger-ui/index.html)

# 구현 중 이슈
## Jwt (refresh + access)에 관한 고민
### 액세스 토큰만을 사용할 경우
- 액세스 토큰은 stateless이기 때문에 탈취가 되면 서버 입장으로서 관리할 수 있는 방법이 없음 -> 액세스 토큰의 유효기간을 짧게 설정 -> 유저 입장에서는 토큰이 만료될 때마다 재 로그인하여 갱신해줘야 하는 번거로움 발생

- 유효 기간을 길게 가져가면 탈취 당했을 경우 유효 기간이 만료될 때까지 탈취한 공격자가 제약없이 사용할 수 있음

- DB에 유효 액세스 토큰을 저장 -> Jwt는 stateless로 정의 되었지만 DB에 저장하는 순간 그 목적이 깨지게 됨.

### Refresh 토큰 도입
- 리프레시 토큰은 액세스 토큰을 재 발급해주는 역할을 함.
- 리프레시 토큰은 액세스 토큰보다 비교적 긴 유효기간을 가짐, 액세스 토큰이 만료되더라도 비교적 긴 유효기간을 가진 리프레시 토큰을 통해 유저는 로그인 없이 액세스 토큰을 재발급 받을 수 있음.
- 비교적 짧은 액세스 토큰을 탈취 당하더라도, 유효 기간이 짧아서 비교적 괜찮음.

- 하지만 리프레시 토큰을 탈취 당하면 공격자는 무한하게 액세스 토큰을 발행할 수 있음.

- 리프레시 토큰을 DB에 저장하여 유저당 유효한 리프레시 토큰 1개 만을 사용할 수 있도록 구현함, 로그인 또는 액세스 토큰이 만료되어 재발급할 때 리프레시 토큰이 업데이트 되도록 구현함.

- 즉, 로그인, 액세스 토큰 재발행할 경우 유저의 고유한 리프레시 토큰이 생성됨.

### 도전과제
- 액세스 토큰의 유효 기간은 발급 후 2일로 정하고, 리프레시 토큰의 유효 기간은 발급 후 14일로 정하였음.
- 위 기간에 대한 정의 방법도 함께 공부해야할 과제로 남음.

- 한편으로는 유저의 MAC 이나 IP + 타임스탬프를 기반으로 Jwt를 생성하는 방법도 있었지만, Jwt를 택해서 사용하게 됨.
- Jwt보다 더 안전한 인증 및 인가에 대해 공부
- 비밀번호 방식(Know)의 로그인 방식을 사용하고 있지만, 타임스탬프 기반의 인증 코드 생성 방식(Google Authenticator, MS Authenticator 등)의 도입도 고민
