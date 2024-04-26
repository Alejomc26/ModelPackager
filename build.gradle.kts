plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
}

group = "io.github.alejomc26"
version = "1.0"

repositories {
    mavenCentral()
}

javafx {
    modules("javafx.controls", "javafx.fxml")
    version = "21"
}

application {
    mainClass = "io.github.alejomc.Main"
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.8")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}