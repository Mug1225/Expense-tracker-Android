FROM docker.io/library/ubuntu:22.04

ENV ANDROID_HOME="/sdk"
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

# Install dependencies
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk wget unzip git && \
    rm -rf /var/lib/apt/lists/*

# Install Gradle 8.5
RUN wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip -O /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /opt && \
    rm /tmp/gradle.zip
ENV PATH="${PATH}:/opt/gradle-8.5/bin"

# Download Android SDK
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Accept licenses and install platform tools
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

WORKDIR /app

# Copy project files
COPY . .

# Grant execution permission to gradlew (if it exists) or we run gradle directly
# We assume the user might not have wrapper generated, so we might need a local gradle or generate wrapper.
# For now, let's just assume we can run ./gradlew if available, or just fail if not.
# Better yet, let's allow the container to just be an environment where we can mount the volume.
# But for a standalone build:
RUN chmod +x gradlew || true

CMD ["/bin/bash"]
