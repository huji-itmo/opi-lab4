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

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("war")
    id("io.freefair.lombok") version "8.10"
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

tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
		events("passed", "skipped", "failed");
        outputs.upToDateWhen { false }
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

tasks.create("scp") {
    dependsOn("deploy_helios")
}

//====================================================

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

//====================================================


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

//====================================================


tasks.register("report") {
    dependsOn("test")
    group = "Verification"
    description = "Adds test reports to Git and commits them if tests passed"

    onlyIf {
        tasks.test.get().state.failure == null
    }

    doLast {
        val testResultsDir = "test-results"

        // Step 1: Clean the test-results directory
        delete(testResultsDir)
        mkdir(testResultsDir)

        // Step 2: Copy build/test-results to test-results
        copy {
            from("build/test-results")
            into(testResultsDir)
        }

        // Step 3: Check if there are any changes in test-results
        val diffResult = exec {
            commandLine("git", "diff", "--quiet", "--exit-code", "--", testResultsDir)
            isIgnoreExitValue = true
        }

        if (diffResult.exitValue == 0) {
            logger.lifecycle("No changes in test reports. Skipping commit.")
        } else {
            // Step 4: Add and commit the test reports
            exec {
                commandLine("git", "add", testResultsDir)
            }

            val commitMessage = "Update test reports: ${LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)}"
            exec {
                commandLine("git", "commit", "-m", commitMessage)
            }

            logger.lifecycle("Test reports committed with message: $commitMessage")
        }
    }
}

//====================================================


val altSrcDir = "$buildDir/alt-src"
val classesAltDir = "$buildDir/classes-alt"
val altJarFile = "$buildDir/libs/alternative.jar"

sourceSets {
    create("alt") {
        java.srcDir(altSrcDir)
        compileClasspath = sourceSets.main.get().compileClasspath
    }
}

tasks.register<Copy>("prepareAltSources") {
    from(sourceSets.main.get().java)
    into(altSrcDir)
    include("**/*.java")
    filter { line -> line.replace("GraphStateBean", "GraphMinisterStateBean") }

    eachFile {
        if (name == "GraphStateBean.java") {
            name = "GraphMinisterStateBean.java"
        }
    }
    includeEmptyDirs = false
}

tasks.register<JavaCompile>("compileAlt") {
    source = sourceSets.getByName("alt").java
    destinationDirectory.set(file(classesAltDir))
    classpath = sourceSets.getByName("alt").compileClasspath
    options.compilerArgs.add("-Xlint:all")

    dependsOn("prepareAltSources")
}

tasks.register<Jar>("altJar") {
    archiveFileName.set(file(altJarFile).name)
    destinationDirectory.set(file(file(altJarFile).parent))

    from({
        tasks.named<JavaCompile>("compileAlt").get().destinationDirectory.get().asFile
    })

    doLast {
        logger.lifecycle("Alternative jar: ${archiveFile.get()}")
    }
}

tasks.register("alt") {
    dependsOn("altJar")
    group = "build"
    description = "Build alternative JAR with modified sources"
}
tasks.register("cleanAlt") {
    doLast {
        delete(altSrcDir, classesAltDir, altJarFile)
    }
}

//====================================================


tasks.register<Copy>("native2ascii") {
    group = "Localization"
    description = "Converts localized files from UTF-8 to ASCII with Unicode escapes"

    from("src/main/resources") {
        include("**/*.properties")
    }
    into(layout.buildDirectory.dir("native2ascii"))

    filteringCharset = "UTF-8"

    filter { line ->
        line.map { char ->
            if (char.code > 127) {
                "\\u%04X".format(char.code)
            } else {
                char
            }
        }.joinToString("")
    }
}


//====================================================


tasks.register("team") {
    group = "team"
    description = "Builds two previous Git revisions and packages WARs into ZIP"

    doLast {
        val commits = getPreviousGitCommits()
        require(commits.size >= 2) { "Requires at least two previous commits" }

        val buildDir = layout.buildDirectory.get().asFile
        val worktreesDir = File(buildDir, "team/worktrees")
        val warsDir = File(buildDir, "team/wars")

        worktreesDir.deleteRecursively()
        warsDir.deleteRecursively()
        warsDir.mkdirs()

        commits.take(2).forEachIndexed { index, commit ->
            val worktreeDir = File(worktreesDir, "v${index + 1}-${commit.take(7)}")
            var warsFound = false

            try {
                // Create worktree
                project.exec {
                    commandLine("git", "worktree", "add", "--detach", worktreeDir, commit)
                        .assertNormalExitValue()
                }

                // Build with wrapper
                project.exec {
                    workingDir(worktreeDir)
                    commandLine("${project.rootDir}/gradlew", "build")
                        .assertNormalExitValue()
                }

                // Find WAR files
                val warFiles = fileTree(worktreeDir) {
                    include("**/build/libs/*.war")
                }

                if (warFiles.isEmpty()) {
                    throw GradleException("No WAR files found for commit $commit")
                }

                // Copy WARs with logging
                warFiles.forEach { war ->
                    val targetFile = File(warsDir, "v${index + 1}-${war.name}")
                    println("Copying: ${war.path} -> ${targetFile.path}")
                    war.copyTo(targetFile, overwrite = true)
                    warsFound = true
                }

            } finally {
                // Cleanup worktree
                project.exec {
                    commandLine("git", "worktree", "remove", "--force", worktreeDir.absolutePath)
                        .assertNormalExitValue()
                }
            }

            if (!warsFound) {
                throw GradleException("Failed to collect WARs for commit $commit")
            }
        }

        // Create ZIP only if wars exist
        val warFiles = fileTree(warsDir)
        if (warFiles.isEmpty()) {
            throw GradleException("No WAR files collected - cannot create ZIP archive")
        }

        ant.withGroovyBuilder {
            "zip"("destfile" to "$buildDir/team/previous-builds.zip") {
                "fileset"("dir" to warsDir)
            }
        }

        println("Successfully created archive with ${warFiles.count()} files: $buildDir/team/previous-builds.zip")
    }
}

fun getPreviousGitCommits(): List<String> {
    val output = ByteArrayOutputStream()
    project.exec {
        commandLine("git", "log", "--pretty=format:%H", "-n", "3")
        standardOutput = output
    }
    return output.toString()
        .trim()
        .lines()
        .drop(1)
        .take(2)
}

fun ExecSpec.assertNormalExitValue() = apply {
    isIgnoreExitValue = false
}

//==================================
tasks.register("history") {
    group = "history"
    description = "Finds last working commit and generates diff with first broken"

    doLast {
        val buildDir = layout.buildDirectory.get().asFile
        val worktreesDir = File(buildDir, "history/worktrees")
        worktreesDir.deleteRecursively()

        val allCommits = getGitCommitList()
        var workingCommit: String? = null

        for (commit in allCommits) {
            val worktreeDir = File(worktreesDir, commit.take(7))
            try {
                createGitWorktree(commit, worktreeDir)
                println("Testing commit: ${commit.take(7)}")

                val result = project.exec {
                    workingDir = worktreeDir
                    commandLine = listOf("./gradlew", "build", "--no-daemon", "--stacktrace")
                    isIgnoreExitValue = true
                }

                if (result.exitValue == 0) {
                    workingCommit = commit
                    break
                }
            } finally {
                removeGitWorktree(worktreeDir) // Uncomment to clean up worktrees
            }
        }

        if (workingCommit != null) {
            val brokenCommit = getNextCommitAfter(allCommits, workingCommit)
            if (brokenCommit != null) {
                createDiffFile(workingCommit, brokenCommit, buildDir)
            } else {
                println("No subsequent commits found")
            }
        } else {
            println("No working commits found in history")
        }
    }
}

fun getGitCommitList(): List<String> {
    val output = ByteArrayOutputStream()
    project.exec {
        commandLine = listOf("git", "rev-list", "--first-parent", "HEAD")
        standardOutput = output
        assertNormalExitValue()
    }
    return output.toString().trim().split("\n").filter { it.isNotBlank() }
}

fun createGitWorktree(commit: String, dir: File) {
    dir.parentFile.mkdirs()
    project.exec {
        commandLine = listOf("git", "worktree", "add", "--detach", dir.absolutePath, commit)
        assertNormalExitValue()
    }
}

fun removeGitWorktree(dir: File) {
    project.exec {
        commandLine = listOf("git", "worktree", "remove", "--force", dir.absolutePath)
        isIgnoreExitValue = true // Tolerate failure if already removed
    }
}

fun getNextCommitAfter(commits: List<String>, workingCommit: String): String? {
    val index = commits.indexOf(workingCommit)
    return if (index > 0) commits[index - 1] else null
}

fun createDiffFile(fromCommit: String, toCommit: String, buildDir: File) {
    val diffFile = File(buildDir, "diff_${fromCommit.take(7)}_${toCommit.take(7)}.diff")
    println("Generating diff between $fromCommit (working) and $toCommit (broken)...")
    val result = project.exec {
        commandLine = listOf("git", "diff", "$fromCommit..$toCommit")
        standardOutput = diffFile.outputStream()
        isIgnoreExitValue = true
    }
    if (result.exitValue == 0) {
        println("Diff saved to: ${diffFile.absolutePath}")
    } else {
        diffFile.delete()
        println("Error generating diff. Git exit code: ${result.exitValue}")
    }
}


tasks.named("clean") {
    group = "lab"
}

tasks.named("build") {
    group = "lab"
}

tasks.named("compileJava") {
    group = "lab"
}

tasks.named("test") {
    group = "lab"
}

tasks.named("verifyXml") {
    group = "lab"
}

tasks.named("doc") {
    group = "lab"
}

tasks.named("scp") {
    group = "lab"
}

tasks.named("native2ascii") {
    group = "lab"
}

tasks.named("team") {
    group = "lab"
}

tasks.named("history") {
    group = "lab"
}

tasks.named("report") {
    group = "lab"
}

tasks.named("alt") {
    group = "lab"
}
