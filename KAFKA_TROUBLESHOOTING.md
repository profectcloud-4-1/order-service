# Kafka 연결 문제 해결 가이드

## "node -1 disconnected" 에러 해결

### 원인
Kafka 브로커가 실행되지 않은 상태에서 Kafka Consumer가 연결을 시도할 때 발생합니다.

### 해결 방법

#### 방법 1: Kafka 실행 (권장)
```powershell
# Kafka 시작
docker-compose -f src/main/resources/docker-compose.yml up -d

# Kafka 상태 확인
docker ps | findstr kafka
```

#### 방법 2: Kafka 없이 애플리케이션 실행
Consumer가 조건부로 활성화되어 있어 Kafka가 없어도 애플리케이션은 시작됩니다.
단, Kafka 기능(비동기 통신)은 작동하지 않습니다.

**주의**: Producer는 여전히 Kafka 연결을 시도하므로, Kafka가 없으면 Producer 사용 시 에러가 발생할 수 있습니다.

### Consumer 조건부 활성화
모든 Consumer는 `@ConditionalOnProperty` 어노테이션으로 보호되어 있습니다:
- Kafka 설정이 있으면 Consumer 활성화
- Kafka 설정이 없으면 Consumer 비활성화

### Kafka 연결 확인
```powershell
# Kafka 컨테이너 확인
docker ps | findstr kafka

# Kafka 로그 확인
docker-compose -f src/main/resources/docker-compose.yml logs kafka
```

### 일반적인 문제 해결

1. **Kafka가 실행되지 않음**
   ```powershell
   docker-compose -f src/main/resources/docker-compose.yml up -d
   ```

2. **포트 충돌**
   - application.yml의 `bootstrap-servers: localhost:9092` 확인
   - 다른 서비스가 9092 포트를 사용 중인지 확인

3. **Kafka 컨테이너 재시작**
   ```powershell
   docker-compose -f src/main/resources/docker-compose.yml restart kafka
   ```

4. **완전히 재시작**
   ```powershell
   docker-compose -f src/main/resources/docker-compose.yml down
   docker-compose -f src/main/resources/docker-compose.yml up -d
   ```

### 개발 환경 권장 사항
- 로컬 개발 시: Kafka를 항상 실행하도록 Docker Compose 자동 시작 설정
- 테스트 시: Kafka 없이도 실행 가능하도록 Consumer는 조건부 활성화됨

