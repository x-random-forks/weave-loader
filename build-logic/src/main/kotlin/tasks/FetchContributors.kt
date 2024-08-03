package tasks

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.net.URI

const val CONTRIBUTORS_URL = "https://api.github.com/repos/Weave-MC/Weave-Loader/contributors"

@Serializable
internal data class GitHubContributor(
    val login: String,
    val url: String,
)

open class FetchContributors : DefaultTask() {
    @get:Input
    val gitCommit: Property<String> =
        project.objects.property(String::class.java).convention(fetchGitCommit())

    @get:OutputFile
    val contributorsFile: RegularFileProperty =
        project.objects.fileProperty().convention(
            project.layout.buildDirectory.file("tmp/weave-contributor-names.json")
        )

    @TaskAction
    fun fetchContributors() {
        val contributors = buildList {
            URI.create(CONTRIBUTORS_URL).toURL().openStream().use {
                it.bufferedReader().useLines { lines ->
                    addAll(Json.decodeFromString<List<GitHubContributor>>(lines.joinToString("\n")))
                }
            }
        }

        if (contributors.isEmpty()) {
            logger.warn("No contributors found, skipping")
            return
        }

        val contributorsFile = contributorsFile.get().asFile
        if (contributorsFile.exists()) {
            // Check if file contents are the same
            val existingContributors = Json.decodeFromString<List<GitHubContributor>>(contributorsFile.readText())
            if (existingContributors == contributors) {
//              Contributors are the same, skipping
                return
            }
        }

        contributorsFile.writeText(Json.encodeToString(contributors))
    }

    private fun fetchGitCommit(): String {
        val stdout = ByteArrayOutputStream()
        project.exec {
            commandLine("git", "rev-parse", "HEAD")
            standardOutput = stdout
            isIgnoreExitValue = true
        }
        return stdout.toString(Charsets.UTF_8).trim()
    }
}