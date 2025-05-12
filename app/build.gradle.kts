import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException
import org.gradle.api.GradleException

import java.io.File
import java.io.FileInputStream
import java.io.BufferedInputStream
import java.security.MessageDigest

import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.file.DirectoryProperty

plugins {
    id("java")
    id("war")
    id("io.freefair.lombok") version "8.10"

}

tasks.war {
    webAppDirectory.set(file("src/main/webapp"))
    manifest {
        attributes(
            "Built-By" to System.getProperty("user.name"),
            "Application-Name" to project.name
        )
    }

    doFirst {
        val filesToHash = project.fileTree(".") {
            exclude("build/**", ".gradle/**", ".git/**", "*.iml", "*.ipr", "*.iws")
        }.files

        val md5Hashes = filesToHash.associate {
            it.relativeTo(project.projectDir).path to calculateHash(it, "MD5")
        }
        val sha1Hashes = filesToHash.associate {
            it.relativeTo(project.projectDir).path to calculateHash(it, "SHA-1")
        }

        val md5String = md5Hashes.entries.joinToString(";") { "${it.key}=${it.value}" }
        val sha1String = sha1Hashes.entries.joinToString(";") { "${it.key}=${it.value}" }

        manifest.attributes["MD5-Digests"] = md5String
        manifest.attributes["SHA-1-Digests"] = sha1String
    }
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



fun calculateHash(file: File, algorithm: String): String {
    val digest = MessageDigest.getInstance(algorithm)
    file.forEachBlock { buffer, bytesRead ->
        digest.update(buffer, 0, bytesRead)
    }
    return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
}

fun File.forEachBlock(blockSize: Int = 4096, action: (ByteArray, Int) -> Unit) {
    val inputStream = BufferedInputStream(FileInputStream(this))
    try {
        val buffer = ByteArray(blockSize)
        while (true) {
            val bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) break
            action(buffer, bytesRead)
        }
    } finally {
        inputStream.close()
    }
}

tasks.create<Zip>("doc") {
    group = "Documentation"
    description = "Generates ZIP with WAR and Javadoc."

    dependsOn(tasks["war"], tasks["javadoc"])

    val warTask = tasks["war"] as org.gradle.api.tasks.bundling.War
    val warFile = warTask.archiveFile.get().asFile

    from(warFile) {
        rename { "${project.name}-${project.version}.war" }
    }

    val javadocTask = tasks["javadoc"] as Javadoc
    val javadocDir = javadocTask.destinationDir!!

    from(javadocDir) {
        into("javadoc")
    }

    destinationDirectory.set(file("${buildDir}/distributions"))
    archiveFileName.set("${project.name}-${project.version}-doc.zip")
}
