plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "net.easecation"
version = "0.1.0"

val paperApiVersion: String by project

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}


dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("com.github.luben:zstd-jni:1.5.6-9")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
