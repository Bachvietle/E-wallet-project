# Giải thích Multi-satge build:
# Ở Stage 1: chúng ta tải về toàn bộ JDK, Maven và thư viện ngoài để tạo ra các file .jar (run ở dòng 12)
# Ở Satge 3: Sau khi chúng ta đã có các file .jar, chúng ta chỉ cần chạy các file .jar này tên Java runtime enviroment (jre)
# và bỏ qua tất cả các thứ cồng kềnh trước đó


# Stage 1: Download the dependencies
FROM eclipse-temurin:21-jdk-jammy AS dependencies
WORKDIR /build
# Copy file pom và bộ wrapper của Maven
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Cấp quyền thực thi cho file script mvnw (Rất quan trọng trên môi trường Linux/Alpine)
RUN chmod +x ./mvnw

# Dùng mvnw thay vì mvn (Dấu ./ biểu thị chạy file ở thư mục hiện tại)
RUN ./mvnw dependency:go-offline # download all dependencies, go-offline có nghĩa là sau lệnh này, bạn có thể build ứng dụng mà không cần kết nối Internet nữa.

# Stage 2: Build the application
FROM dependencies AS builder
COPY src ./src
RUN ./mvnw clean package -Dmaven.test.skip=true # xóa thư mục target cũ sinh ra từ những lần build trước (nếu có) để tránh lỗi.

# Stage 3: Run the application
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# file jar được tạo trong folder target, copy toàn bộ file jar đó vào file app.jar
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# Bth khi chạy SpringBoot, ta chỉ cần viết câu lệnh như dưới vào terminal. Tuy nhiên Docker
# yêu cầu command là 1 array, mỗi phần là 1 ptu
# java -jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
