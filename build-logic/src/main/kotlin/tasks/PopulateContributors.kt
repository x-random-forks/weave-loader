package tasks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URI

@Serializable
data class GitHubUser(
    val login: String,
    val name: String,
    @SerialName("html_url")
    val htmlUrl: String,
)

open class PopulateContributors : DefaultTask() {
    @get:InputFile
    val contributorNames: RegularFileProperty =
        project.objects.fileProperty().convention(
            project.layout.buildDirectory.file("tmp/weave-contributor-names.json")
        )

    @get:OutputFile
    val contributorsFile: RegularFileProperty =
        project.objects.fileProperty().convention(
            project.layout.buildDirectory.file("tmp/weave-contributors.json")
        )

    @TaskAction
    fun populateContributors() {
        val contributors = contributorNames.get().asFile.readText().let { Json.decodeFromString<List<GitHubContributor>>(it) }

        val users = buildList {
            for (contributor in contributors) {
                URI.create(contributor.url).toURL().openStream().use {
                    it.bufferedReader().useLines { lines ->
                        this += Json.decodeFromString<GitHubUser>(lines.joinToString("\n"))
                    }
                }
            }
        }

        contributorsFile.get().asFile.writeText(Json.encodeToString(users))
    }
}