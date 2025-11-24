package gusl.launcher;


import gusl.launcher.model.StaticDetails;
import gusl.launcher.model.WarDetails;
import gusl.launcher.model.WebSite;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.apache.commons.cli.*;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static io.undertow.UndertowOptions.ENABLE_HTTP2;

/**
 * The main bootstrap class for launching Casanova nodes.
 *
 * @author dhudson
 */
public class Launcher {

    private static final String HELP_ARG = "h";
    private static final String LOCATION_ARG = "location";
    private static final String WAR_ARG = "war";
    private static final String DEBUG_ARG = "debug";
    private static final String PORT_ARG = "port";
    private static final String HOST_ARG = "host";
    private static final String SITE_ARG = "site";
    private static final String NO_EXPAND_ARG = "noexpand";
    private static final String ENTITY_SIZE = "entitysize";

    // Comma delimited list of headers
    private static final String CORS_HEADERS = "corsheaders";

    static boolean isDebug = false;

    Launcher() {
    }

    void bootstrap(String[] args) {

        System.out.println("launcher .... " + getReleaseInfoFromManifest());

        Options options = new Options();
        options.addOption(DEBUG_ARG, "Enable debugging");
        options.addOption(HELP_ARG, "Help");
        options.addOption(LOCATION_ARG, true, "Location to unpack the war files. [Optional]");
        options.addOption(PORT_ARG, true, "Port number. [Optional default 8080]");
        options.addOption(HOST_ARG, true, "Host to listen on. [Optional default localhost");
        options.addOption(SITE_ARG, true, "Site.json to Deploy [Wars will be ignored]");
        options.addOption(NO_EXPAND_ARG, "Don't extract the wars");
        options.addOption(ENTITY_SIZE, true, "Max Entity Size");
        options.addOption(CORS_HEADERS, true, "Comma delimited list of headers");

        // Enable -war file1.war file2.war
        options.addOption(Option.builder(WAR_ARG).desc("War File(s) to Deploy [must be last arg]").hasArgs().build());

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(options, args);

        } catch (ParseException ex) {
            System.err.println("Command Line Exception " + ex);
            printHelp(options);
            return;
        }

        if (args.length == 0 || commandLine.hasOption(HELP_ARG)) {
            System.err.println("No arguments supplied");
            printHelp(options);
            return;
        }

        if (commandLine.hasOption(DEBUG_ARG)) {
            isDebug = true;
        }

        File baseLocation;
        if (commandLine.hasOption(LOCATION_ARG)) {
            if (isDebug) {
                System.out.println("has Location arg");
            }
            baseLocation = new File(commandLine.getOptionValue(LOCATION_ARG));
        } else {
            if (isDebug) {
                System.out.println("Using current directory for location");
            }
            baseLocation = new File(".");
        }

        WebSite website = new WebSite();

        UndertowConfig config = new UndertowConfig();
        if (commandLine.hasOption(PORT_ARG)) {
            try {
                config.setPort(Integer.parseInt(commandLine.getOptionValue(PORT_ARG)));
            } catch (NumberFormatException ex) {
                System.err.println("Invalid port number " + ex);
                // Stop ...
                return;
            }
        }

        if (commandLine.hasOption(ENTITY_SIZE)) {
            try {
                config.setEntityMaxSize(Long.parseLong(commandLine.getOptionValue(ENTITY_SIZE)));
            } catch (NumberFormatException ex) {
                System.err.println("Can't convert entity size " + ex);
            }
        }

        if (commandLine.hasOption(CORS_HEADERS)) {
            config.setCorsHeaders(commandLine.getOptionValue(CORS_HEADERS));
        } else {
            String entitySize = System.getProperty(LauncherConstants.LAUNCHER_ENTITY_SIZE_KEY);
            if (entitySize != null) {
                try {
                    config.setEntityMaxSize(Long.parseLong(entitySize));
                } catch (NumberFormatException ex) {
                    System.err.println("Can't convert entity size " + ex);
                }
            }
        }

        // Set the System Property for the port
        System.setProperty(LauncherConstants.LAUNCHER_PORT_KEY, Integer.toString(config.getPort()));

        if (commandLine.hasOption(HOST_ARG)) {
            config.setHost(commandLine.getOptionValue(HOST_ARG));
        }

        website.setConfig(config);

//        if (commandLine.hasOption(SITE_ARG)) {
//
//            // We have a site file, so lets load and parse that
//            ObjectMapper mapper = ObjectMapperFactory.createObjectMapper(PropertyNamingStrategy.KEBAB_CASE);
//
//            try {
//                Site site = LauncherUtils.loadConfig(Site.class, commandLine.getOptionValue(SITE_ARG), mapper);
//                Map<String, String> systemProperties = site.getSystemProperties();
//                if (systemProperties == null) {
//                    systemProperties = new HashMap<>(0);
//                }
//
//                for (WebApp webApp : site.getWebApps()) {
//                    if (webApp.getSite() != null) {
//                        // Let's see if we can locate the site
//                        File file = resolveCoordinates(webApp.getSite(), systemProperties);
//                        System.out.println(" I have resolved a site to ...  " + file.getAbsoluteFile());
//                        File location = new File(baseLocation, "undertow" + File.separator + webApp.getPath());
//                        LauncherUtils.unzip(file.getAbsolutePath(), location.getAbsolutePath(), webApp.getZipRoot());
//
//                        System.out.println(file.getName() + " unpacked to " + location.getPath());
//
//                        StaticDetails details = new StaticDetails();
//                        details.setLocation(location);
//                        details.setContext(webApp.getContext());
//                        details.setName(webApp.getName());
//                        website.addStatic(details);
//                    } else {
//                        File file = resolveCoordinates(webApp.getWar(), systemProperties);
//                        System.out.println(" I have resolved a site to ...  " + file.getAbsoluteFile());
//                        boolean expand = !commandLine.hasOption(NO_EXPAND_ARG);
//                        WarDetails warDetail = LauncherUtils.extract(file.getAbsolutePath(), baseLocation, webApp.getPath(), expand);
//                        if (expand) {
//                            System.out.println(file.getName() + " unpacked to " + warDetail.getLocation());
//                        }
//
//                        // Let's see if there is a site file.
//                        warDetail.createClassLoader();
//                        website.addWar(warDetail);
//                    }
//                }
//            } catch (IOException ex) {
//                System.err.println("Error loading site file " + commandLine.getOptionValue(SITE_ARG));
//                ex.printStackTrace();
//                return;
//            }
//        }

        if (commandLine.hasOption(WAR_ARG)) {
            for (String war : commandLine.getOptionValues(WAR_ARG)) {
                try {
                    File warFile = new File(war);
                    WarDetails warDetail = LauncherUtils.extract(war, baseLocation, LauncherUtils.getFileBaseName(warFile), !commandLine.hasOption(NO_EXPAND_ARG));

                    warDetail.createClassLoader();
                    website.addWar(warDetail);
                    if (isDebug) {
                        System.out.println("War Details .. " + warDetail);
                    }

                } catch (IOException ex) {
                    System.err.println("Unable to extract war file " + ex);
                    // Should we stop if we can't extract them all?
                }
            }
        }

        try {
            startUndertow(website, config);
        } catch (Throwable ex) {
            System.err.println("Unable to start Application " + ex);
        }
    }

//    private File resolveCoordinates(String coords, Map<String, String> systemProperties) throws IOException {
//        String path = LauncherUtils.resolve(coords, systemProperties);
//        return MavenResolver.resolve(path);
//    }

    private String getReleaseInfoFromManifest() {
        StringBuilder builder = new StringBuilder();
        builder.append("Release ");

        builder.append(LauncherUtils.getVersion());
        builder.append(" - ");
        builder.append(LauncherUtils.getTimestamp());
        return builder.toString();
    }

    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("launcher", options);
    }

    private void startUndertow(WebSite website, UndertowConfig config) throws IOException {

        System.out.println("Using config " + config);

        Xnio xnio = Xnio.getInstance();

        int threads = Runtime.getRuntime().availableProcessors() * 3;

        XnioWorker worker = xnio.createWorker(OptionMap.builder()
                .set(org.xnio.Options.WORKER_IO_THREADS, threads)
                .set(org.xnio.Options.WORKER_TASK_CORE_THREADS, threads)
                .set(org.xnio.Options.WORKER_TASK_MAX_THREADS, threads)
                .set(org.xnio.Options.TCP_NODELAY, true)
                .set(org.xnio.Options.WORKER_NAME, "Launcher HTTP")
                .set(org.xnio.Options.REUSE_ADDRESSES, true)
                .getMap());

        ServletContainer container = Servlets.defaultContainer();

        // Add gzip and defalte
        final CORsHandler handler = new CORsHandler(config.getCorsHeaders());

        for (StaticDetails details : website.getStatics()) {
            System.out.println("--- Static " + details.getName() + " ------- with context of " + details.getContext() + " ----- " + details.getLocation().getAbsolutePath());
//            path.addExactPath(details.getContext(), Handlers.resource(PathResourceManager.builder()
//                    .setTransferMinSize(1024)
//                    .setBase(details.getLocation().toPath())
//                    .setETagFunction(new CasanovaEtagFunction())
//                    .setCaseSensitive(false)
//                    .setSafePaths(null)
//                    .build()));

            handler.getPath().addPrefixPath(details.getContext(),
                    Handlers.resource(new PathResourceManager(details.getLocation().toPath(), 10000L))
                            .setDirectoryListingEnabled(false)
                            .setWelcomeFiles("index.html"));
        }

        for (WarDetails details : website.getWars()) {
            System.out.println("--- Deploying " + details.getName() + " ------- with context of /" + details.getName());

            final DeploymentInfo servletBuilder = Servlets.deployment()
                    .setDeploymentName(details.getName())
                    .setContextPath("/" + details.getName())
                    .setClassLoader(details.getClassLoader())
                    .setResourceManager(PathResourceManager.builder()
                            .setTransferMinSize(1024)
                            .setBase(details.getLocation().toPath())
                            .setETagFunction(new GUSLEtagFunction())
                            .setCaseSensitive(false)
                            .setSafePaths(null)
                            .build());

            // Scan for and register @WebServlet annotated classes
            //Hack to get the servlets registered
            registerServlets(servletBuilder, details);

            addWebSockets(servletBuilder, details, worker);

            // create deployment manager
            DeploymentManager manager = container.addDeployment(servletBuilder);

            if (manager != null) {
                try {
                    // Run the startup in the new class loader
                    manager.deploy();
                    handler.getPath().addPrefixPath("/" + details.getName(), manager.start());
                } catch (Throwable t) {
                    System.err.println("ERR001 Unable to deploy Error: " + t.getMessage());
                    t.printStackTrace();
                }
            }
        }

        System.out.println("Starting Application " + config);

        Undertow server = Undertow.builder()
                .setServerOption(UndertowOptions.MAX_HEADER_SIZE, 20480)
                .setServerOption(UndertowOptions.MAX_HEADERS, 40)
                .setServerOption(UndertowOptions.MAX_PARAMETERS, 40)
                .setServerOption(UndertowOptions.MAX_COOKIES, 40)
                .setServerOption(UndertowOptions.MAX_ENTITY_SIZE, config.getEntityMaxSize())
                //.setServerOption(UndertowOptions.IDLE_TIMEOUT, 60 * 1000)
                .addHttpListener(config.getPort(), config.getHost())
                .setHandler(handler)
                .setSocketOption(org.xnio.Options.WORKER_IO_THREADS, threads)
                .setSocketOption(org.xnio.Options.TCP_NODELAY, true)
                .setSocketOption(org.xnio.Options.REUSE_ADDRESSES, true)
                .setServerOption(ENABLE_HTTP2, true)
                .setWorker(worker)
                .build();
        server.start();

        // Call the Jersey Application, and notify that the port is now listening
        for (WarDetails details : website.getWars()) {
            String applClass = details.getManifest().getProperty(LauncherConstants.APPLICATION_KEY);
            try {
                Class<?> clazz = details.getClassLoader().loadClass(applClass);
                Method startup = clazz.getMethod("startup");
                // I need to invoke the method using the ServletContext class loader.
                Thread.currentThread().setContextClassLoader(details.getClassLoader());

                startup.invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException ex) {
                System.err.println("Unable to notify Application of container startup" + ex);
            }
        }

    }

    private void registerServlets(final DeploymentInfo servletBuilder, WarDetails details) {
        try {
            // Scan for @WebServlet annotated classes in common packages
            String[] packages = {"", "engine", "wallet"};
            Set<Class<?>> servletClasses = new java.util.HashSet<>();
            
            for (String pkg : packages) {
                try {
                    Set<Class<?>> found = scanForClasses(details.getClassLoader(), pkg, WebServlet.class);
                    servletClasses.addAll(found);
                } catch (Exception e) {
                    // Package might not exist, continue
                }
            }
            
            // Register each servlet found
            for (Class<?> servletClass : servletClasses) {
                // Ensure it's actually a Servlet subclass
                if (!Servlet.class.isAssignableFrom(servletClass)) {
                    continue;
                }
                
                WebServlet annotation = servletClass.getAnnotation(WebServlet.class);
                if (annotation != null) {
                    String[] urlPatterns = annotation.urlPatterns();
                    if (urlPatterns.length == 0) {
                        urlPatterns = annotation.value();
                    }
                    if (urlPatterns.length > 0) {
                        String servletName = annotation.name();
                        if (servletName.isEmpty()) {
                            servletName = servletClass.getSimpleName();
                        }
                        
                        @SuppressWarnings("unchecked")
                        Class<? extends Servlet> servletType = (Class<? extends Servlet>) servletClass;
                        io.undertow.servlet.api.ServletInfo servletInfo = Servlets.servlet(servletName, servletType);
                        for (String pattern : urlPatterns) {
                            servletInfo.addMapping(pattern);
                        }
                        servletBuilder.addServlet(servletInfo);
                        System.out.println("Registered servlet: " + servletClass.getName() + " with patterns: " + java.util.Arrays.toString(urlPatterns));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error registering servlets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addWebSockets(final DeploymentInfo servletBuilder, WarDetails details, XnioWorker worker) {
        // Load the manifest to find the Jersey application to load.
        boolean addWebsockets = false;
        String packagePath;

        Properties manifest = details.getManifest();

        if (manifest.containsKey(LauncherConstants.WEBSOCKET_SUPPORT_KEY)) {
            if ((manifest.get(LauncherConstants.WEBSOCKET_SUPPORT_KEY)).equals("true")) {
                if (manifest.containsKey(LauncherConstants.WEBSOCKET_SUPPORT_PACKAGE_KEY)) {
                    packagePath = (String) manifest.get(LauncherConstants.WEBSOCKET_SUPPORT_PACKAGE_KEY);
                    WebSocketDeploymentInfo wsDeployInfo = getWebSocketDeploymentInfo(details.getClassLoader(), packagePath,
                            worker);
                    servletBuilder.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, wsDeployInfo);
                    addWebsockets = true;
                }
            }
        }

        if (addWebsockets) {
            System.out.println("Web Socket Support added");
        } else {
            System.out.println("Web Socket Support not enabled for " + details.getName());
        }
    }

    private WebSocketDeploymentInfo getWebSocketDeploymentInfo(WebAppClassLoader classLoader, String packagePath, XnioWorker worker) {

        // Adding web sockets
        WebSocketDeploymentInfo wsDeployInfo = new WebSocketDeploymentInfo();
        wsDeployInfo.setWorker(worker);
        wsDeployInfo.setBuffers(new DefaultByteBufferPool(true, 1000));

        Set<Class<?>> endpoints = scanForClasses(classLoader, packagePath, ServerEndpoint.class);
        if (isDebug) {
            System.out.println("WS endpoint scan, found: " + endpoints.size() + " for prefix " + packagePath);
        }

        for (Class<?> serverEndpoint : endpoints) {
            if (isDebug) {
                System.out.println("WS Adding: " + serverEndpoint.getCanonicalName());
            }
            wsDeployInfo.addEndpoint(serverEndpoint);
        }

        return wsDeployInfo;
    }

    private Set<Class<?>> scanForClasses(WebAppClassLoader classLoader, String packagePath, Class<? extends Annotation> klass) {

        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(classLoader);
        if (packagePath == null) {
            packagePath = "";
        }

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner(),
                new TypeAnnotationsScanner());
        FilterBuilder filterBuilder = new FilterBuilder();
        for (String pathPrefix : packagePath.split(",")) {
            builder.setUrls(ClasspathHelper.forPackage(pathPrefix));
            filterBuilder.includePackage(pathPrefix);
        }

        builder.setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[classLoadersList.size()])));
        builder.addClassLoaders(classLoadersList.toArray(new ClassLoader[classLoadersList.size()]));
        builder.filterInputsBy(filterBuilder);

        Reflections reflections = new Reflections(builder);

//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner(),
//                        new TypeAnnotationsScanner())
//                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[classLoadersList.size()])))
//                .addClassLoader(classLoader)
//                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packagePath))));

        Set<Class<?>> resourceClassSet = reflections.getTypesAnnotatedWith(klass, true);
        return resourceClassSet;
    }

    public static void main(String[] args) {

        // Let's create the Log Directory before we do anything
        String logDir = getLogBase();
        if (logDir != null) {
            File file = new File(logDir);
            file.mkdirs();
        } else {

            System.err.println("System Property " + LauncherConstants.LAUNCHER_LOGS_LOCATION_KEY + " not set");
        }

        Launcher launcher = new Launcher();
        launcher.bootstrap(args);
    }

    public static String getLogBase() {
        return System.getProperty(LauncherConstants.LAUNCHER_LOGS_LOCATION_KEY);
    }

}
