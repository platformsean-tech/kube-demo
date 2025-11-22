package gusl.launcher;

public interface LauncherConstants {

    String JAR_MANIFEST_LOCATION = "META-INF/MANIFEST.MF";

    String BUILD_VERSION_KEY = "Build-Version";
    String BUILD_TIMESTAMP_KEY = "Build-Timestamp";

    // Manifest Attributes
    String APPLICATION_KEY = "Application";
    String WEBSOCKET_SUPPORT_KEY = "WebSocketSupport";
    String WEBSOCKET_SUPPORT_PACKAGE_KEY = "WebSocketPackage";

    // System Properties
    // Max size of an inbound entity
    String LAUNCHER_ENTITY_SIZE_KEY = "LauncherEntitySize";
    String LAUNCHER_LOGS_LOCATION_KEY = "ApplicationLogs";
    String LAUNCHER_PORT_KEY = "ApplicationPort";

}
