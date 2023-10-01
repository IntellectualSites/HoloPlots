import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `java-library`

    alias(libs.plugins.pluginyml)
    alias(libs.plugins.shadow)
    alias(libs.plugins.minotaur)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.compileJava.configure {
    options.release.set(17)
}

configurations.all {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
}

version = "7.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://repo.dmulloy2.net/nexus/repository/public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    implementation(platform("com.intellectualsites.bom:bom-newest:1.38"))
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit")
    compileOnly("io.papermc.paper:paper-api")
    compileOnly(libs.holographicdisplays)
    compileOnly(libs.protocollib)
    compileOnly(libs.worldedit)
    implementation("org.bstats:bstats-bukkit")
    implementation("org.bstats:bstats-base")
}

bukkit {
    name = "HoloPlots"
    main = "com.plotsquared.holoplots.HoloPlotsPlugin"
    authors = listOf("Empire92", "NotMyFault", "dordsor21")
    apiVersion = "1.13"
    description = "Holographic Plot signs"
    version = rootProject.version.toString()
    depend = listOf("HolographicDisplays", "PlotSquared", "ProtocolLib")
    website = "https://www.spigotmc.org/resources/4880/"
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set(null as String?)
    dependencies {
        relocate("org.bstats", "com.plotsquared.holoplots.metrics")
        relocate("net.kyori.adventure", "com.plotsquared.core.configuration.adventure")
    }
    minimize()
}

tasks.named("build").configure {
    dependsOn("shadowJar")
}

val supportedVersions = listOf("1.16.5", "1.17.1", "1.18.2", "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4", "1.20", "1.20.1")

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("holoplots")
    versionName.set("${project.version}")
    versionNumber.set("${project.version}")
    versionType.set("release")
    uploadFile.set(file("build/libs/${rootProject.name}-${project.version}.jar"))
    gameVersions.addAll(supportedVersions)
    loaders.addAll(listOf("paper", "purpur", "spigot"))
    syncBodyFrom.set(rootProject.file("README.md").readText())
    changelog.set("The changelog is available on GitHub: https://github" +
            ".com/IntellectualSites/HoloPlots/releases/tag/${project.version}")
}
