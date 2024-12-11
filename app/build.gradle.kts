plugins {
    id("java")
    id("war")
    id("io.freefair.lombok") version "8.10"

}

tasks.war {
    webAppDirectory.set(file("src/main/webapp"))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    implementation("com.google.code.gson:gson:2.11.0");

    testImplementation(platform("org.junit:junit-bom:5.11.3"))
	testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.14.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
		events("passed", "skipped", "failed");
	}
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.create("deploy_local") {

    dependsOn("war")

    doLast {
        exec {
            workingDir("..")
            commandLine("cp", "app/build/libs/*.war", "docker/deployments")
        }

        exec {
            workingDir("docker")
            commandLine("docker", "compose", "up")
        }
    }
}

// tasks.create("deploy_helios") {

//     dependsOn("war")

//     doLast {
//         exec {
//             workingDir(".")
//             commandLine("bash", "scripts/deploy_helios.sh")
//         }
//     }
// }
