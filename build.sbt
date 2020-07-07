import sbtcrossproject.CrossPlugin.autoImport.crossProject
import explicitdeps.ExplicitDepsPlugin.autoImport.moduleFilterRemoveValue
import BuildHelper._

inThisBuild(
  List(
    name := "zio-lexer",
    organization := "com.schuwalow",
    homepage := Some(url("https://github.com/mschuwalow/zio-lexer")),
    developers := List(
      Developer(
        "mschuwalow",
        "Maxim Schuwalow",
        "maxim.schuwalow@gmail.com",
        url("https://github.com/mschuwalow")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        homepage.value.get,
        "scm:git:git@github.com:mschuwalow/zio-lexer.git"
      )
    ),
    licenses := Seq(
      "Apache-2.0" -> url(
        s"${scmInfo.value.map(_.browseUrl).get}/blob/v${version.value}/LICENSE"
      )
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc")
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias(
  "check",
  "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)
addCommandAlias(
  "testJVM",
  ";coreExamplesJVM/compile;testExamplesJVM/compile;coreTestsJVM/test;testTestsJVM/test"
)
addCommandAlias(
  "testJS",
  ";coreExamplesJS/compile;testExamplesJS/compile;coreTestsJS/test;testTestsJS/test"
)
addCommandAlias("testRelease", ";set every isSnapshot := false;+clean;+compile")

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true,
    unusedCompileDependenciesFilter -= moduleFilter(
      "org.scala-js",
      "scalajs-library"
    )
  )
  .aggregate(
    regex.jvm,
    regex.js,
    regexTests.jvm,
    regexTests.js
  )
  .enablePlugins(ScalaJSPlugin)

lazy val regex = crossProject(JSPlatform, JVMPlatform)
  .in(file("regex"))
  .settings(stdSettings("zio-lexer-regex"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"         % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion
    )
  )

lazy val regexTests = crossProject(JSPlatform, JVMPlatform)
  .in(file("regex-tests"))
  .dependsOn(regex)
  .settings(stdSettings("zio-lexer-regex-tests"))
  .settings(testSettings)
