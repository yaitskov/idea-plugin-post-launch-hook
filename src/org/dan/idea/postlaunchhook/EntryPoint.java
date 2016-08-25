package org.dan.idea.postlaunchhook;

import static java.lang.Long.parseLong;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EntryPoint extends AnAction {
    private static final Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    static {
        com.intellij.openapi.diagnostic.Logger.getInstance(EntryPoint.class).info("Init logging");
        logger.info("Init Post Launch Hook");
        init();
    }

    private static void init() {
        final String processName = getRuntimeMXBean().getName();
        final long pid = parseLong(processName.split("@")[0]);
        logger.info("Idea PID is {}", pid);
        final String userName = System.getProperty("user.name");
        final Path hooks = Paths.get("/home").resolve(userName).resolve(".idea-post-launch-hooks");
        final Runtime runtime = Runtime.getRuntime();
        if (!Files.isDirectory(hooks)) {
            logger.info("Folder {} doesn't exist.", hooks);
            return;
        }
        listHookFiles(hooks)
                .forEachOrdered(p -> {
                    logger.info("Execute script [{}].", p);
                    try {
                        runtime.exec(new String[]{p.toString(), String.valueOf(pid)});
                    } catch (IOException e) {
                        logger.error("Failed to run " + p, e);
                    }
                });
    }

    private static Stream<Path> listHookFiles(Path hooks) {
        try {
            return Files.list(hooks).filter(Files::isExecutable)
                .filter(Files::isRegularFile)
                .sorted();
        } catch (IOException e) {
            logger.error("Failed to list dir [" + hooks + "]", e);
            return new ArrayList<Path>().stream();
        }
    }


    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        // is not called
    }

    @Override
    public void update(final AnActionEvent anActionEvent) {
        // is not called
    }
}
