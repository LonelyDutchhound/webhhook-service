name := "webhook-service-server"

version := "0.1"

scalaVersion := "2.13.5"

idePackagePrefix := Some("com.LonelyDutchhound")

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val root = (project in file("."))
  .in(file("."))
  .settings(
    publishArtifact in(Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    sources in(Compile, doc) := Seq.empty,
    libraryDependencies ++= Dependencies.globalProjectDeps
  )