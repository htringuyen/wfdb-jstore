plugins {
    id("java")
    id("io.freefair.lombok") version "8.6"
}

repositories {
    mavenCentral()
}

dependencies {
    //implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(files("/usr/local/lib/wfdb.jar"))
    implementation(files("/usr/local/lib/JustFLAC-0.0.1-SNAPSHOT.jar"))

    testImplementation(libs.commons.io)
    testImplementation(libs.spring.context)
    testImplementation(libs.spring.test)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.test {
    jvmArgs("--enable-preview");
    systemProperty("java.library.path", "/usr/local/lib")
    useJUnitPlatform()
}


