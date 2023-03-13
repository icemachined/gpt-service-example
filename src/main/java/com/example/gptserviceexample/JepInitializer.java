package com.example.gptserviceexample;

import jep.Jep;
import jep.JepConfig;
import jep.MainInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.IllegalStateException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Configuring Jep depending on environment.
 */
class JepInitializer {
    public static JepConfig config = new JepConfig();

    private static Logger LOGGER = LoggerFactory.getLogger(JepInitializer.class);

    static {
        LOGGER.info("Initializing JEP");

        String packageName = "ExampleModule";
        URL packageInitFile = JepInitializer.class.getClassLoader().getResource("initgpt.py");

        config.redirectStdErr(System.err);
        config.redirectStdout(System.out);

        if (packageInitFile.getProtocol() == "file") {
            LOGGER.debug(
                "Found the $packageName module using a \"file\" resource. Using python code directly."
            );
            // we can point JEP to the folder and get better debug messages with python source code
            // locations

            // we want to have the parent folder of "CPGPython" so that we can do "import CPGPython"
            // in python
            try {
                config.addIncludePaths(Paths.get(packageInitFile.toURI()).getParent().toString());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
            Path targetFolder = null;
            try {
                targetFolder = Files.createTempDirectory("jep_python_example");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            config.addIncludePaths(targetFolder.toString());

            try {
                extractPythonPackage(packageInitFile, packageName, targetFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File jepLibrary = null;
        if (System.getenv("JEP_LIBRARY_PATH") != null) {
            jepLibrary = new File(System.getenv("JEP_LIBRARY_PATH"));
        } else {
            Path jepRoot = null;
            try {
                jepRoot = Paths.get(packageInitFile.toURI()).getParent().getParent().getParent().resolve("distros").resolve("jep-distro").resolve("jep");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            jepLibrary = deriveLepLibraryFile(jepRoot);
        }
        if (!jepLibrary.exists()) {
            throw new IllegalStateException(String.format("Can't find jep library. File %s does not exist.", jepLibrary));
        }
        MainInterpreter.setJepLibraryPath(jepLibrary.getPath());
        config.addIncludePaths(
            jepLibrary.toPath().getParent().getParent().toString()
        );
    }

    private static File deriveLepLibraryFile(Path jepRoot) {
        switch (getOS()) {
            case WINDOWS: return new File(jepRoot.resolve("jep.dll").toString());
            case LINUX: return new File(jepRoot.resolve("libjep.so").toString());
            case MAC: return new File(jepRoot.resolve("libjep.jnilib").toString());
        }
        return null;
    }

    private static void extractPythonPackage(URL pyInitFile, String packageName, Path targetFolder) throws IOException {
        // otherwise, we are probably running inside a JAR, so we try to extract our files
        // out of the jar into a temporary folder
        JarURLConnection jarURL = (JarURLConnection) pyInitFile.openConnection();
        JarFile jar = jarURL.getJarFile();

        if (jar == null) {
            LOGGER.error(
                "Could not extract $packageName package out of the jar."
            );
        } else {
            LOGGER.info(
                "Using JAR connection to {} to extract files into {}",
                jar.getName(),
                targetFolder
            );

            Stream<JarEntry> entries = Collections.list(jar.entries()).stream().filter(it -> it.getName().contains(packageName));

            entries.forEach ( entry -> {
                        LOGGER.debug("Extracting entry: {}", entry.getName());

                        // resolve target files relatively to our target folder. They are already
                        // prefixed with CPGPython/
                        File targetFile = targetFolder.resolve(entry.getName()).toFile();

                        // make sure to create directories along the way
                        if (entry.isDirectory()) {
                            targetFile.mkdirs();
                        } else {
                            // copy the contents into the temp folder
                            try {
                                try ( InputStream jis = jar.getInputStream(entry)) {
                                        try(FileOutputStream output = new FileOutputStream(targetFile)) {
                                            jis.transferTo(output);
                                        }
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
            );
        }
    }

    static OS getOS() {
        String os = System.getProperty("os.name").toString().toLowerCase();
        if (os.contains("win")) {
            return OS.WINDOWS;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return OS.LINUX;
        } else if (os.contains("mac")) {
            return OS.MAC;
        }
        return null;
    }

    public static JepConfig getConfig() {
        return config;
    }

    enum OS {
        WINDOWS, LINUX, MAC
    }

    public static void setupDebugger(Jep jep) throws IOException {
        String debugEgg = System.getenv("DEBUG_PYTHON_EGG");
        String debugHost = System.getenv("DEBUG_PYTHON_HOST");
        if(debugHost == null)
            debugHost = "localhost";
        String debugPortStr = System.getenv("DEBUG_PYTHON_PORT");
        Integer debugPort = debugPortStr == null ? 52225 : Integer.valueOf(debugPortStr);
        if (debugEgg != null) {
            jep.runScript(new File(JepInitializer.class.getResource("/setup_debug.py").getFile()).getCanonicalPath());
            jep.invoke("enable_debugger", debugEgg, debugHost, debugPort);
        }
    }
}
