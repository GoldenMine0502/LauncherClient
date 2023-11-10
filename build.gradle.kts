import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
//    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.springframework.boot") version "2.7.3"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    kotlin("jvm") version "1.7.21"
    id("edu.sc.seis.launch4j") version ("2.5.4")
    application
}

group = "kr.goldenmine.inuminecraftlauncher.client"
version = "1.3.1-SNAPSHOT"

repositories {
    mavenCentral()

    maven(url="https://raw.githubusercontent.com/tomsik68/maven-repo/master/")

    maven(url="https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-parent:2.7.3")
    implementation(group="org.glassfish.jaxb", name="jaxb-runtime", version="2.3.2")
    implementation("org.springframework.boot:spring-boot-devtools")

//    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
//    implementation ("org.slf4j:slf4j-api:2.0.7")

//    // https://mvnrepository.com/artifact/org.apache.commons/commons-compress
//    implementation("org.apache.commons:commons-compress:1.23.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-gson
    implementation(group="com.squareup.retrofit2", name="converter-gson", version="2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java
    implementation("org.seleniumhq.selenium:selenium-java:4.4.0")
    // https://mvnrepository.com/artifact/io.github.bonigarcia/webdrivermanager
    implementation(group="io.github.bonigarcia", name="webdrivermanager", version="5.3.0")

    implementation("sk.tomsik68:mclauncher-api:0.3.1")

    implementation("org.apache.maven:maven-artifact:3.8.6")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.google.oauth-client:google-oauth-client:1.34.1")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.http-client:google-http-client:1.42.2")
    implementation("com.google.http-client:google-http-client-gson:1.42.2")
    implementation("joda-time:joda-time:2.10.14")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.9.1")

//    implementation 'org.to2mbn:jmccc'

//    implementation files("libs/HMCL-3.5.SNAPSHOT.jar")
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
//    implementation("libs/INUMinecraftLauncherCore-1.0-SNAPSHOT.jar")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("junit:junit:4.13.2")

    testImplementation(kotlin("test")) // The Kotlin test library

//    implementation(project(":Core"))
//    testImplementation(project(":Core"))

//    testImplementation(kotlin("test"))

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("com.google.oauth-client:google-oauth-client:1.34.1")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.http-client:google-http-client:1.42.2")
    implementation("com.google.http-client:google-http-client-gson:1.42.2")

}
/*
        <dependency>
            <groupId>com.google.oauth-client</groupId>
            <artifactId>google-oauth-client</artifactId>
            <version>1.34.1</version>
        </dependency>
        <dependency>
            <groupId>com.google.oauth-client</groupId>
            <artifactId>google-oauth-client-jetty</artifactId>
            <version>1.34.1</version>
        </dependency>
        <dependency>
            <groupId>com.google.http-client</groupId>
            <artifactId>google-http-client</artifactId>
            <version>1.42.2</version>
        </dependency>
        <dependency>
            <groupId>com.google.http-client</groupId>
            <artifactId>google-http-client-gson</artifactId>
            <version>1.42.2</version>
        </dependency>
 */

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("kr.goldenmine.inuminecraftlauncher.Main")
}

launch4j {
    mainClassName = "kr.goldenmine.inuminecraftlauncher.Main"
    icon = "${projectDir}/icons/inu.ico"
}

//compileJava.options.fork = true
//compileJava.options.forkOptions.executable = 'C:/Program Files/Java/jdk-11.0.6/bin/javac.exe'