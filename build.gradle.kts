import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `java-library`

    alias(libs.plugins.pluginyml)
    alias(libs.plugins.shadow)
}

the<JavaPluginExtension>().toolchain {
    languageVersion.set(JavaLanguageVersion.of(16))
}

version = "6.2.1"

repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://repo.dmulloy2.net/nexus/repository/public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven {
        name = "IntellectualSites Snapshots"
        url = uri("https://mvn.intellectualsites.com/content/repositories/snapshots/")
        content {
            includeModule("com.intellectualsites", "Pipeline")
        }
    }
}

dependencies {
    compileOnly(libs.plotsquared)
    compileOnly(libs.paper)
    compileOnly(libs.holographicdisplays)
    compileOnly(libs.protocollib)
    compileOnly(libs.worldedit)
    implementation(libs.bstatsBukkit)
    implementation(libs.bstatsBase)
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
