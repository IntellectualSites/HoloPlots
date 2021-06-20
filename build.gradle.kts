import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("java-library")
    id("net.minecrell.plugin-yml.bukkit") version "0.4.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

plugins.withId("java") {
    the<JavaPluginExtension>().toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

version = "6.0.0"

repositories {
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.dmulloy2.net/nexus/repository/public/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://mvn.intellectualsites.com/content/groups/public/") }
}

dependencies {
    compileOnly("com.plotsquared:PlotSquared-Bukkit:6.0.0-SNAPSHOT")
    compileOnly("com.plotsquared:PlotSquared-Core:6.0.0-SNAPSHOT")
    compileOnlyApi("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT")
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.7")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:2.2.1")
    implementation("org.bstats:bstats-base:2.2.1")
}

bukkit {
    name = "HoloPlots"
    main = "com.empcraft.holoplots.Main"
    authors = listOf("Empire92", "NotMyFault", "dordsor21")
    apiVersion = "1.13"
    description = "Holographic Plot signs"
    version = rootProject.version.toString()
    softDepend = listOf("HolographicDisplays", "PlotSquared", "ProtocolLib")
    website = "https://www.spigotmc.org/resources/4880/"
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set(null as String?)
    dependencies {
        relocate("org.bstats", "com.empcraft.holoplots.metrics") {
            include(dependency("org.bstats:bstats-base"))
            include(dependency("org.bstats:bstats-bukkit"))
        }
    }
    minimize()
}

tasks.named("build").configure {
    dependsOn("shadowJar")
}
