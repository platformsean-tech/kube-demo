package gusl.launcher;

/**
 * Use JBoss ShrinkWrap Resolver to find any maven artifacts and install them in
 * a local repository.
 * <p>
 * https://github.com/shrinkwrap/resolver https://maven.apache.org/pom.html
 *
 *
 * https://github.com/apache/maven-resolver/blob/master/maven-resolver-demos/maven-resolver-demo-snippets/src/main/java/org/apache/maven/resolver/examples/resolver/ResolverDemo.java
 * ^^ Use this instead ..
 *
 * @author dhudson
 */
public class MavenResolver {

    public static final String ALL_RELEASE_RANGE = "[0.0.0,)";
    public static final String LATEST_RELREASE_SYMBOL = "+";

    private MavenResolver() {
    }

//    public static File resolve(String artifactCoords) throws IOException {
//        MavenResolvedArtifact artifact = resolveArtifact(artifactCoords);
//        return artifact.asFile();
//    }
//
//    public static MavenResolvedArtifact resolveArtifact(String artifactCoords) throws IOException {
//        // Lets see if the version is a + for latest.
//        MavenCoordinate coordinate = MavenCoordinates.createCoordinate(artifactCoords);
//        if (coordinate.getVersion().equals(LATEST_RELREASE_SYMBOL)) {
//            coordinate = getLatestVersion(artifactCoords.replace(LATEST_RELREASE_SYMBOL, ALL_RELEASE_RANGE));
//        }
//
//        MavenResolvedArtifact artifact = Maven.configureResolver()
//                .fromClassloaderResource("settings.xml", MavenResolver.class.getClassLoader())
//                .resolve(coordinate.toCanonicalForm())
//                .withoutTransitivity()
//                .asSingle(MavenResolvedArtifact.class);
//        return artifact;
//    }
//
//    public static MavenVersionRangeResult resolveVersions(String artifactCoords) throws IOException {
//        return Maven.configureResolver()
//                .fromClassloaderResource("settings.xml", MavenResolver.class.getClassLoader())
//                .resolveVersionRange(artifactCoords);
//    }
//
//    public static MavenCoordinate getLatestVersion(String artifactCoords) throws IOException {
//        return resolveVersions(artifactCoords).getHighestVersion();
//    }

}
