plugins {
    kotlin("jvm")
}

kotlinProject()

messagingLibs()

dependencies {
    implementation(project(":pleo-antaeus-data"))
    api(project(":pleo-antaeus-models"))

    implementation("io.github.resilience4j:resilience4j-retry:1.7.1")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}