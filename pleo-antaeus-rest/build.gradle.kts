plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-core"))
    implementation(project(":pleo-antaeus-models"))

    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.3")
    implementation("io.javalin:javalin:3.7.0")
}
