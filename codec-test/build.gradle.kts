plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jflac.codec)

    implementation(libs.log4j.api)
    implementation(libs.log4j.core)

    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}