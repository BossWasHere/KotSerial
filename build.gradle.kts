import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "com.backwardsnode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fazecast:jSerialComm:2.9.2")

    testImplementation(kotlin("test"))
}

tasks {

    test {
        useJUnitPlatform()
    }

    jar {
        manifest {
            attributes(
                "Main-Class" to "MainKt"
            )
        }

        from(configurations.runtimeClasspath.get().map { zipTree(it.absoluteFile) })

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

}

tasks.withType<JavaExec> {
    run {
        standardInput = System.`in`
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("MainKt")
}
