package project;

import base.config.SystemConfig;
import base.config.UserConfig;
import base.enums.ResultEnums;
import base.exception.AnalyzerException;
import base.utils.SerializationUtils;
import entity.Project;
import ui.frame.Console;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static base.utils.SerializationUtils.deserialize;

/**
 * organize the projects
 *
 * @Author: DoneEI
 * @Since: 2021/2/17 1:05 下午
 **/
public class ProjectService {

    private static LinkedList<Project> history;

    static {
        loadHistoryProjects();
    }

    public static Project createNewProject(String owner, String framework, String filePath) {
        Project project = new Project(framework, owner, filePath);

        try {
            // init
            SerializationUtils.serialize(project, filePath, ".scaPro");

            File f = new File(filePath);

            if (!f.exists()) {
                f.mkdirs();
            }

            f = new File(filePath + File.separator + "application.properties");

            if (!f.exists()) {
                f.createNewFile();
            }

            f = new File(filePath + File.separator + ".scaPro");

            if (!f.exists()) {
                f.createNewFile();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new AnalyzerException(ResultEnums.ERROR, ioe.getMessage());
        }

        return project;
    }

    public static Project openProject(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new AnalyzerException(ResultEnums.FILE_NOT_EXIST, "The path does not exist");
        }

        // check whether the project info exists
        File pro = new File(filePath + File.separator + ".scaPro");

        if (!pro.exists()) {
            throw new AnalyzerException(ResultEnums.FILE_NOT_EXIST, "The path does not contain a sca project");
        }

        try {
            Project project = deserialize(pro.getAbsolutePath());
            openProject(project);

            return project;
        } catch (Exception e) {
            throw new AnalyzerException(ResultEnums.ERROR, "can not parse the project info");
        }

    }

    public static void openProject(Project project) {
        // put the project into the first
        history.remove(project);

        history.add(0, project);

        UserConfig.init(project.getFilePath() + File.separator + "application.properties");
    }

    public static void closeProject(Project project) throws IOException {
        SerializationUtils.serialize(project, project.getFilePath(), ".scaPro");
    }

    public static void loadHistoryProjects() {
        File historyFile = new File(SystemConfig.BASE_APPLICATION_FILE_PATH + File.separator + ".sca_cache");

        if (historyFile.exists()) {
            try {
                Project[] projects = SerializationUtils.deserialize(historyFile.getAbsolutePath());

                if (projects != null) {
                    history = new LinkedList<>(Arrays.asList(projects));
                    return;
                }
            } catch (Exception e) {
                throw new AnalyzerException(ResultEnums.ERROR, "can not parse the application cached info");
            }
        }

        history = new LinkedList<>();
    }

    public static void saveHistoryProjects() throws IOException {
        Project[] projects = new Project[history.size()];

        for (int i = 0; i < projects.length; i++) {
            projects[i] = history.get(i);
        }

        SerializationUtils.serialize(projects, SystemConfig.BASE_APPLICATION_FILE_PATH, ".sca_cache");
    }

    public static LinkedList<Project> getHistory() {
        return history;
    }
}
