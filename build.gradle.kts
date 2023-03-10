import de.undercouch.gradle.tasks.download.Download

plugins {
    java
    id("org.springframework.boot") version "3.0.3"
    id("io.spring.dependency-management") version "1.1.0"
    id("de.undercouch.download") version "5.3.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}
val jepArchive by configurations.creating
val resolveJep: TaskProvider<Copy> = tasks.register<Copy>("resolveJep") {
    destinationDir = file("$buildDir/distros/jep-distro")
    from(tarTree(jepArchive.singleFile))
}

var downloadGpt = tasks.create<Download>("downloadGPT") {
    src("https://github.com/karpathy/nanoGPT/archive/refs/heads/master.zip")
    dest(File("$buildDir/python/master.zip"))

    overwrite(false)
}
tasks.create<Copy>("unzipGPT") {
    dependsOn(downloadGpt)

    from(zipTree(downloadGpt.dest))
    into("$buildDir/python/")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("black.ninia:jep:4.1.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    jepArchive("com.icemachined:jep-distro-cp3.10:4.1.1@tgz")
    runtimeOnly(fileTree("$buildDir/distros/jep-distro").apply {
        builtBy(resolveJep)
    })
}

tasks.withType<Test> {
    useJUnitPlatform()
}
