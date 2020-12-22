import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("java-library")
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility
}

version = "5.1.1"

repositories {
    jcenter()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.dmulloy2.net/nexus/repository/public/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://mvn.intellectualsites.com/content/groups/public/") }
}

dependencies {
    compileOnly("com.plotsquared:PlotSquared-Core:5.13.0")
    compileOnlyApi("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.4-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.0")
}

bukkit {
    name = "HoloPlots"
    main = "com.empcraft.holoplots.Main"
    authors = listOf("Empire92", "NotMyFault")
    apiVersion = "1.13"
    description = "Holographic Plot signs"
    version = rootProject.version.toString()
    softDepend = listOf("HolographicDisplays", "PlotSquared", "ProtocolLib")
    website = "https://www.spigotmc.org/resources/4880/"
}
