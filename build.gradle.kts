import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	kotlin("jvm") version "1.6.0"
	kotlin("plugin.serialization") version "1.5.31"
	id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.semiosbio"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
	all {
		exclude("log4j")
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
	implementation("software.amazon.awssdk:iotdataplane:2.17.100")
	implementation("org.eclipse.californium:californium-core:3.0.0")
	implementation("software.amazon.awssdk:iotdataplane:2.17.100")
	implementation("ch.qos.logback:logback-classic:1.2.6")
	implementation("com.google.code.gson:gson:2.8.9")
}


tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<ShadowJar> {
	manifest {
		attributes(Pair("Main-Class", "org.semiosbio.coap.CoAPServerKt"))
	}
}

