import com.google.protobuf.gradle.id

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id ("com.google.protobuf") version "0.9.4"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.64.0"
        }
        generateProtoTasks {
            all().forEach {
                it.builtins {
                    named("java") {
                        option("lite")
                    }
                }
                it.plugins {
                    create("grpc") {
                        option("lite")
                    }
                }
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // required to manipulate the environment vars in tests
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
}

dependencies {
    implementation("io.grpc:grpc-okhttp:1.64.0")
    implementation("io.grpc:grpc-protobuf-lite:1.64.0")
    implementation("io.grpc:grpc-stub:1.64.0")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
}
