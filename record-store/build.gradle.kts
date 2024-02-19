plugins {
    id("java")
    id("io.freefair.lombok") version "8.6"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jflac.codec)
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(files("/usr/local/lib/wfdb.jar"))


    testImplementation(libs.spring.context)
    testImplementation(libs.spring.test)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    systemProperty("java.library.path", "/usr/local/lib")
    useJUnitPlatform()
}


