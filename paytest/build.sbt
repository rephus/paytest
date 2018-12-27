organization  := "javier.rengel"

version       := "1"

scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
parallelExecution in Test := false

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "io.spray"            %%  "spray-json"     % sprayV,

    "org.flywaydb" % "flyway-core" % "4.2.0", // run migrations

    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "com.typesafe.slick"  %%  "slick"         % "3.1.1",
    "com.h2database"      %   "h2"            % "1.3.175" % "test",
    "postgresql"          % "postgresql"      % "9.1-901.jdbc4",

    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.slf4j" % "slf4j-api" % "1.7.2",
    "com.typesafe.akka" %% "akka-slf4j" % "2.3.15"


  )
}

Revolver.settings
