import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

tasks {
    val fetchContributors by creating(tasks.FetchContributors::class)
    val populateContributors by creating(tasks.PopulateContributors::class)

    val shadowJar by getting(ShadowJar::class) {
        from(populateContributors)
    }
}