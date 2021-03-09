package robocorp.robot.intellij;

import com.google.gson.*;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import robocorp.lsp.intellij.LanguageServerDefinition;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;


class RobotProjectState {
    
    public String robotPythonExecutable = "";
    public String robotPythonEnv = "";
    public String robotVariables = "";
    public String robotPythonpath = "";
    public String robotCompletionsSectionHeadersForm = "";
    public String robotWorkspaceSymbolsOnlyForOpenDocs = "";
}

// IMPORTANT: Autogenerated. Don't change manually. Run codegen.py to regenerate.
@State(name = "RobotProjectPreferences", storages = {@Storage("RobotProjectPreferences.xml")})
public class RobotProjectPreferences implements PersistentStateComponent<RobotState> {

    
    public static final String ROBOT_PYTHON_EXECUTABLE = "robot.python.executable";
    public static final String ROBOT_PYTHON_ENV = "robot.python.env";
    public static final String ROBOT_VARIABLES = "robot.variables";
    public static final String ROBOT_PYTHONPATH = "robot.pythonpath";
    public static final String ROBOT_COMPLETIONS_SECTION_HEADERS_FORM = "robot.completions.section_headers.form";
    public static final String ROBOT_WORKSPACE_SYMBOLS_ONLY_FOR_OPEN_DOCS = "robot.workspaceSymbolsOnlyForOpenDocs";
    
    private static final Logger LOG = Logger.getInstance(RobotProjectPreferences.class);

    // IMPORTANT: Autogenerated. Don't change manually. Run codegen.py to regenerate.
    @Nullable
    @Override
    public RobotState getState() {
        RobotState robotState = new RobotState();
        
        robotState.robotPythonExecutable = getRobotPythonExecutable();
        robotState.robotPythonEnv = getRobotPythonEnv();
        robotState.robotVariables = getRobotVariables();
        robotState.robotPythonpath = getRobotPythonpath();
        robotState.robotCompletionsSectionHeadersForm = getRobotCompletionsSectionHeadersForm();
        robotState.robotWorkspaceSymbolsOnlyForOpenDocs = getRobotWorkspaceSymbolsOnlyForOpenDocs();
        return robotState;
    }

    // IMPORTANT: Autogenerated. Don't change manually. Run codegen.py to regenerate.
    @Override
    public void loadState(@NotNull RobotState robotState) {
        
        setRobotPythonExecutable(robotState.robotPythonExecutable);
        setRobotPythonEnv(robotState.robotPythonEnv);
        setRobotVariables(robotState.robotVariables);
        setRobotPythonpath(robotState.robotPythonpath);
        setRobotCompletionsSectionHeadersForm(robotState.robotCompletionsSectionHeadersForm);
        setRobotWorkspaceSymbolsOnlyForOpenDocs(robotState.robotWorkspaceSymbolsOnlyForOpenDocs);
    }
    
    // IMPORTANT: Autogenerated. Don't change manually. Run codegen.py to regenerate.
    public JsonObject asJsonObject() {
        Gson g = new Gson();
        JsonObject jsonObject = new JsonObject();
        
        if(!robotPythonExecutable.isEmpty()){
            try {
                jsonObject.add(ROBOT_PYTHON_EXECUTABLE, new JsonPrimitive(robotPythonExecutable));
            } catch(Exception e) {
                LOG.error(e);
            }
        }
        
        if(!robotPythonEnv.isEmpty()){
            try {
                jsonObject.add(ROBOT_PYTHON_ENV, g.fromJson(robotPythonEnv, JsonObject.class));
            } catch(Exception e) {
                LOG.error(e);
            }
        }
        
        if(!robotVariables.isEmpty()){
            try {
                jsonObject.add(ROBOT_VARIABLES, g.fromJson(robotVariables, JsonObject.class));
            } catch(Exception e) {
                LOG.error(e);
            }
        }
        
        if(!robotPythonpath.isEmpty()){
            try {
                jsonObject.add(ROBOT_PYTHONPATH, g.fromJson(robotPythonpath, JsonArray.class));
            } catch(Exception e) {
                LOG.error(e);
            }
        }
        
        if(!robotCompletionsSectionHeadersForm.isEmpty()){
            try {
                jsonObject.add(ROBOT_COMPLETIONS_SECTION_HEADERS_FORM, new JsonPrimitive(robotCompletionsSectionHeadersForm));
            } catch(Exception e) {
                LOG.error(e);
            }
        }
        
        if(!robotWorkspaceSymbolsOnlyForOpenDocs.isEmpty()){
            try {
                jsonObject.add(ROBOT_WORKSPACE_SYMBOLS_ONLY_FOR_OPEN_DOCS, new JsonPrimitive(Boolean.parseBoolean(robotWorkspaceSymbolsOnlyForOpenDocs)));
            } catch(Exception e) {
                LOG.error(e);
            }
        }
        
        return jsonObject;
    }

    
    private String robotPythonExecutable = "";

    public @NotNull String getRobotPythonExecutable() {
        return robotPythonExecutable;
    }
    
    public @Nullable JsonPrimitive getRobotPythonExecutableAsJson() {
        if(robotPythonExecutable.isEmpty()){
            return null;
        }
        Gson g = new Gson();
        return new JsonPrimitive(robotPythonExecutable);
    }
    
    public @NotNull String validateRobotPythonExecutable(String robotPythonExecutable) {
        if(robotPythonExecutable.isEmpty()) {
            return "";
        }
        try {
            Gson g = new Gson();
            new JsonPrimitive(robotPythonExecutable);
            
            return "";
            
        } catch(Exception e) {
            return e.toString();
        }
    }

    public void setRobotPythonExecutable(String s) {
        if (s == null) {
            s = "";
        }
        if (s.equals(robotPythonExecutable)) {
            return;
        }
        String old = robotPythonExecutable;
        robotPythonExecutable = s;
        for (LanguageServerDefinition.IPreferencesListener listener : listeners) {
            listener.onChanged(ROBOT_PYTHON_EXECUTABLE, old, s);
        }
    }
    
    private String robotPythonEnv = "";

    public @NotNull String getRobotPythonEnv() {
        return robotPythonEnv;
    }
    
    public @Nullable JsonObject getRobotPythonEnvAsJson() {
        if(robotPythonEnv.isEmpty()){
            return null;
        }
        Gson g = new Gson();
        return g.fromJson(robotPythonEnv, JsonObject.class);
    }
    
    public @NotNull String validateRobotPythonEnv(String robotPythonEnv) {
        if(robotPythonEnv.isEmpty()) {
            return "";
        }
        try {
            Gson g = new Gson();
            g.fromJson(robotPythonEnv, JsonObject.class);
            
            return "";
            
        } catch(Exception e) {
            return e.toString();
        }
    }

    public void setRobotPythonEnv(String s) {
        if (s == null) {
            s = "";
        }
        if (s.equals(robotPythonEnv)) {
            return;
        }
        String old = robotPythonEnv;
        robotPythonEnv = s;
        for (LanguageServerDefinition.IPreferencesListener listener : listeners) {
            listener.onChanged(ROBOT_PYTHON_ENV, old, s);
        }
    }
    
    private String robotVariables = "";

    public @NotNull String getRobotVariables() {
        return robotVariables;
    }
    
    public @Nullable JsonObject getRobotVariablesAsJson() {
        if(robotVariables.isEmpty()){
            return null;
        }
        Gson g = new Gson();
        return g.fromJson(robotVariables, JsonObject.class);
    }
    
    public @NotNull String validateRobotVariables(String robotVariables) {
        if(robotVariables.isEmpty()) {
            return "";
        }
        try {
            Gson g = new Gson();
            g.fromJson(robotVariables, JsonObject.class);
            
            return "";
            
        } catch(Exception e) {
            return e.toString();
        }
    }

    public void setRobotVariables(String s) {
        if (s == null) {
            s = "";
        }
        if (s.equals(robotVariables)) {
            return;
        }
        String old = robotVariables;
        robotVariables = s;
        for (LanguageServerDefinition.IPreferencesListener listener : listeners) {
            listener.onChanged(ROBOT_VARIABLES, old, s);
        }
    }
    
    private String robotPythonpath = "";

    public @NotNull String getRobotPythonpath() {
        return robotPythonpath;
    }
    
    public @Nullable JsonArray getRobotPythonpathAsJson() {
        if(robotPythonpath.isEmpty()){
            return null;
        }
        Gson g = new Gson();
        return g.fromJson(robotPythonpath, JsonArray.class);
    }
    
    public @NotNull String validateRobotPythonpath(String robotPythonpath) {
        if(robotPythonpath.isEmpty()) {
            return "";
        }
        try {
            Gson g = new Gson();
            g.fromJson(robotPythonpath, JsonArray.class);
            
            return "";
            
        } catch(Exception e) {
            return e.toString();
        }
    }

    public void setRobotPythonpath(String s) {
        if (s == null) {
            s = "";
        }
        if (s.equals(robotPythonpath)) {
            return;
        }
        String old = robotPythonpath;
        robotPythonpath = s;
        for (LanguageServerDefinition.IPreferencesListener listener : listeners) {
            listener.onChanged(ROBOT_PYTHONPATH, old, s);
        }
    }
    
    private String robotCompletionsSectionHeadersForm = "";

    public @NotNull String getRobotCompletionsSectionHeadersForm() {
        return robotCompletionsSectionHeadersForm;
    }
    
    public @Nullable JsonPrimitive getRobotCompletionsSectionHeadersFormAsJson() {
        if(robotCompletionsSectionHeadersForm.isEmpty()){
            return null;
        }
        Gson g = new Gson();
        return new JsonPrimitive(robotCompletionsSectionHeadersForm);
    }
    
    public @NotNull String validateRobotCompletionsSectionHeadersForm(String robotCompletionsSectionHeadersForm) {
        if(robotCompletionsSectionHeadersForm.isEmpty()) {
            return "";
        }
        try {
            Gson g = new Gson();
            new JsonPrimitive(robotCompletionsSectionHeadersForm);
             
            if(robotCompletionsSectionHeadersForm.equals("plural")){
                return "";
            }
            if(robotCompletionsSectionHeadersForm.equals("singular")){
                return "";
            }
            if(robotCompletionsSectionHeadersForm.equals("both")){
                return "";
            }
            return "Unexpected value: " + robotCompletionsSectionHeadersForm;
            
        } catch(Exception e) {
            return e.toString();
        }
    }

    public void setRobotCompletionsSectionHeadersForm(String s) {
        if (s == null) {
            s = "";
        }
        if (s.equals(robotCompletionsSectionHeadersForm)) {
            return;
        }
        String old = robotCompletionsSectionHeadersForm;
        robotCompletionsSectionHeadersForm = s;
        for (LanguageServerDefinition.IPreferencesListener listener : listeners) {
            listener.onChanged(ROBOT_COMPLETIONS_SECTION_HEADERS_FORM, old, s);
        }
    }
    
    private String robotWorkspaceSymbolsOnlyForOpenDocs = "";

    public @NotNull String getRobotWorkspaceSymbolsOnlyForOpenDocs() {
        return robotWorkspaceSymbolsOnlyForOpenDocs;
    }
    
    public @Nullable JsonPrimitive getRobotWorkspaceSymbolsOnlyForOpenDocsAsJson() {
        if(robotWorkspaceSymbolsOnlyForOpenDocs.isEmpty()){
            return null;
        }
        Gson g = new Gson();
        return new JsonPrimitive(Boolean.parseBoolean(robotWorkspaceSymbolsOnlyForOpenDocs));
    }
    
    public @NotNull String validateRobotWorkspaceSymbolsOnlyForOpenDocs(String robotWorkspaceSymbolsOnlyForOpenDocs) {
        if(robotWorkspaceSymbolsOnlyForOpenDocs.isEmpty()) {
            return "";
        }
        try {
            Gson g = new Gson();
            new JsonPrimitive(Boolean.parseBoolean(robotWorkspaceSymbolsOnlyForOpenDocs));
            
            return "";
            
        } catch(Exception e) {
            return e.toString();
        }
    }

    public void setRobotWorkspaceSymbolsOnlyForOpenDocs(String s) {
        if (s == null) {
            s = "";
        }
        if (s.equals(robotWorkspaceSymbolsOnlyForOpenDocs)) {
            return;
        }
        String old = robotWorkspaceSymbolsOnlyForOpenDocs;
        robotWorkspaceSymbolsOnlyForOpenDocs = s;
        for (LanguageServerDefinition.IPreferencesListener listener : listeners) {
            listener.onChanged(ROBOT_WORKSPACE_SYMBOLS_ONLY_FOR_OPEN_DOCS, old, s);
        }
    }
    

    private Collection<LanguageServerDefinition.IPreferencesListener> listeners = new CopyOnWriteArraySet<>();

    public static @Nullable RobotProjectPreferences getInstance(Project project) {
        try {
            return ServiceManager.getService(project, RobotProjectPreferences.class);
        } catch (Exception e) {
            LOG.error("Error getting RobotProjectPreferences", e);
            return null;
        }
    }

    public void addListener(LanguageServerDefinition.IPreferencesListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(LanguageServerDefinition.IPreferencesListener listener) {
        listeners.remove(listener);
    }

}