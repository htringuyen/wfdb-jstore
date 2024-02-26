plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.log4j.core)
    implementation(files("/usr/local/lib/wfdb.jar", "/usr/local/lib/JustFLAC-0.0.1-SNAPSHOT.jar"))
    //implementation(files("/usr/local/share/JustFLAC-0.0.1-SNAPSHOT.jar"))


    testImplementation(libs.spring.context)
    testImplementation(libs.spring.test)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-preview");
}