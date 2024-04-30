plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.0.1"
    application
}

group = "io.github.alejomc"
version = "1.0"

repositories {
    mavenCentral()
}

javafx {
    modules("javafx.controls", "javafx.fxml")
    version = "21"
}

jlink {
    addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        name = project.name
    }
    jpackage {
        installerType = "msi"
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            installerOptions.plusAssign(listOf("--win-per-user-install", "--win-dir-chooser", "--win-menu", "--win-shortcut"))
        }
    }
}

application {
    mainClass = "io.github.alejomc.Main"
    mainModule = "io.github.alejomc"
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.8")
}