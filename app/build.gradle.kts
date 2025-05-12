import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException
import org.gradle.api.GradleException

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
    implementation("com.google.code.gson:gson:2.11.0");

    compileOnly("javax:javaee-api:8.0")
    compileOnly("javax.faces:javax.faces-api:2.3")
    implementation("org.hibernate.orm:hibernate-core:6.6.1.Final")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.hibernate:hibernate-core:5.6.14.Final")
    implementation("org.hibernate:hibernate-entitymanager:5.6.14.Final")


    // https://mvnrepository.com/artifact/org.primefaces/primefaces
    implementation("org.primefaces:primefaces:14.0.9")

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

tasks.register("copyWar", Copy::class) {
    dependsOn("war")

    from("build/libs/") {
        include("*.war")
    }
    into("docker/deoyments")
}

tasks.create("deploy_local") {

    dependsOn("copyWar")

    doLast {
        exec {
            workingDir("../docker")
            commandLine("docker", "compose", "up")
        }
    }
}

tasks.create("deploy_helios") {

    dependsOn("war")

    doLast {
        exec {
            workingDir("scripts")
            commandLine("sh", "deploy_helios.sh")
        }
    }
}

tasks.register("verifyXml") {
    group = "Verification"
    description = "Verifies XML files for well-formedness."

    val xmlDir = file("src/main/")
    inputs.dir(xmlDir)
    outputs.dir(layout.buildDirectory.dir("xmlVerification"))

    doLast {
        val xmlFiles = project.fileTree(xmlDir) { include("**/*.xml") }
        val errors = mutableListOf<String>() // Collect errors

        xmlFiles.forEach { file ->
            try {
                val factory = DocumentBuilderFactory.newInstance()
                factory.isValidating = false
                val builder = factory.newDocumentBuilder()

                builder.setErrorHandler(object : ErrorHandler {
                    override fun warning(exception: SAXParseException) {
                        logger.warn("⚠️ Warning in ${file.relativeTo(projectDir)}: ${exception.message}")
                    }

                    override fun error(exception: SAXParseException) {
                        errors.add("❌ Error in ${file.relativeTo(projectDir)} (Line ${exception.lineNumber}): ${exception.message}")
                    }

                    override fun fatalError(exception: SAXParseException) {
                        errors.add("💥 Fatal Error in ${file.relativeTo(projectDir)}: ${exception.message}")
                    }
                })

                builder.parse(file)
                logger.lifecycle("✅ Valid XML: ${file.relativeTo(projectDir)}")

            } catch (e: Exception) {
                errors.add("❗ Exception in ${file.relativeTo(projectDir)}: ${e.message}")
            }
        }

        // Fail the build if there are errors
        if (errors.isNotEmpty()) {
            throw GradleException("XML validation failed:\n${errors.joinToString("\n")}")
        }
    }
}


tasks.named("check") {
    dependsOn("verifyXml")
}
