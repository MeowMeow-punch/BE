# ========== 빌드 ========== 
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace/app

# 빌드에 필요한 파일 복사
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# 실행 권한 부여 및 줄바꿈 문자 수정 
# why? => 윈도우에서 git clone 시 gradlew가 CRLF로 저장될 수 있어 변환 필요함다 
RUN sed -i 's/\r$//' gradlew
RUN chmod +x gradlew
RUN ./gradlew bootJar -x test

# ========== 실행 ========== 
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일만 복사 (builder 별칭 사용)
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# [Data] Importer용 CSV 파일 복사
# src/main/resources/data 폴더의 내용을 컨테이너의 /app/data로 복사합니다.
COPY src/main/resources/data /app/data

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
