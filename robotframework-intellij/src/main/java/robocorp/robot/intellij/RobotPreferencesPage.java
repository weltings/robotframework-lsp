package robocorp.robot.intellij;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


class RobotPreferencesComponent {

    private final JPanel panel;
    
    private final JBTextField robotLanguageServerPython = new JBTextField();
    private final JBTextField robotLanguageServerArgs = new JBTextField();
    private final JBTextField robotLanguageServerTcpPort = new JBTextField();
    private final JBTextField robotPythonExecutable = new JBTextField();
    private final JBTextField robotPythonEnv = new JBTextField();
    private final JBTextField robotVariables = new JBTextField();
    private final JBTextField robotPythonpath = new JBTextField();
    private final JBTextField robotCompletionsSectionHeadersForm = new JBTextField();

    public RobotPreferencesComponent() {
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Robot Language Server Python"), robotLanguageServerPython, 1, false)
                .addComponent(createJTextArea("Specifies the path to the python executable to be used for the Robot Framework Language Server (the\ndefault is searching python on the PATH).\nRequires a restart to take effect.\n\n"))
                .addLabeledComponent(new JBLabel("Robot Language Server Args"), robotLanguageServerArgs, 1, false)
                .addComponent(createJTextArea("Specifies the arguments to be passed to the robotframework language server (i.e.: [\"-vv\", \"--log-\nfile=~/robotframework_ls.log\"]).\nRequires a restart to take effect.\nNote: expected as JSON Array\n\n"))
                .addLabeledComponent(new JBLabel("Robot Language Server Tcp Port"), robotLanguageServerTcpPort, 1, false)
                .addComponent(createJTextArea("If the port is specified, connect to the language server previously started at the given port.\nRequires a restart to take effect.\n\n"))
                .addLabeledComponent(new JBLabel("Robot Python Executable"), robotPythonExecutable, 1, false)
                .addComponent(createJTextArea("Specifies the path to the python executable to be used to load `robotframework` code and dependent\nlibraries (the default is using the same python used for the language server).\n\n"))
                .addLabeledComponent(new JBLabel("Robot Python Env"), robotPythonEnv, 1, false)
                .addComponent(createJTextArea("Specifies the environment to be used when loading `robotframework` code and dependent libraries.\ni.e.: {\"MY_ENV_VAR\": \"some_value\"}\nNote: expected as JSON Object\n\n"))
                .addLabeledComponent(new JBLabel("Robot Variables"), robotVariables, 1, false)
                .addComponent(createJTextArea("Specifies custom variables to be considered by `robotframework` (used when resolving variables and\nautomatically passed to the launch config as --variable entries).\ni.e.: {\"RESOURCES\": \"c:/temp/resources\"}\nNote: expected as JSON Object\n\n"))
                .addLabeledComponent(new JBLabel("Robot Pythonpath"), robotPythonpath, 1, false)
                .addComponent(createJTextArea("Specifies the entries to be added to the PYTHONPATH (used when resolving resources and imports and\nautomatically passed to the launch config as --pythonpath entries).\ni.e.: [\"</my/path_entry>\"]\nNote: expected as JSON Array\n\n"))
                .addLabeledComponent(new JBLabel("Robot Completions Section Headers Form"), robotCompletionsSectionHeadersForm, 1, false)
                .addComponent(createJTextArea("Defines how completions should be shown for section headers (i.e.: *** Setting(s) ***).\nOne of: plural, singular, both.\n\n"))
                
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private JBTextArea createJTextArea(String text) {
        JBTextArea f = new JBTextArea();
        f.setText(text);
        f.setEditable(false);
        f.setBackground(null);
        f.setBorder(null);
        f.setFont(UIManager.getFont("Label.font"));
        return f;
    }

    public JPanel getPanel() {
        return panel;
    }

    public JComponent getPreferredFocusedComponent() {
        return robotLanguageServerPython;
    }

    
    @NotNull
    public String getRobotLanguageServerPython() {
        return robotLanguageServerPython.getText();
    }

    public void setRobotLanguageServerPython (@NotNull String newText) {
        robotLanguageServerPython.setText(newText);
    }
    
    @NotNull
    public String getRobotLanguageServerArgs() {
        return robotLanguageServerArgs.getText();
    }

    public void setRobotLanguageServerArgs (@NotNull String newText) {
        robotLanguageServerArgs.setText(newText);
    }
    
    @NotNull
    public String getRobotLanguageServerTcpPort() {
        return robotLanguageServerTcpPort.getText();
    }

    public void setRobotLanguageServerTcpPort (@NotNull String newText) {
        robotLanguageServerTcpPort.setText(newText);
    }
    
    @NotNull
    public String getRobotPythonExecutable() {
        return robotPythonExecutable.getText();
    }

    public void setRobotPythonExecutable (@NotNull String newText) {
        robotPythonExecutable.setText(newText);
    }
    
    @NotNull
    public String getRobotPythonEnv() {
        return robotPythonEnv.getText();
    }

    public void setRobotPythonEnv (@NotNull String newText) {
        robotPythonEnv.setText(newText);
    }
    
    @NotNull
    public String getRobotVariables() {
        return robotVariables.getText();
    }

    public void setRobotVariables (@NotNull String newText) {
        robotVariables.setText(newText);
    }
    
    @NotNull
    public String getRobotPythonpath() {
        return robotPythonpath.getText();
    }

    public void setRobotPythonpath (@NotNull String newText) {
        robotPythonpath.setText(newText);
    }
    
    @NotNull
    public String getRobotCompletionsSectionHeadersForm() {
        return robotCompletionsSectionHeadersForm.getText();
    }

    public void setRobotCompletionsSectionHeadersForm (@NotNull String newText) {
        robotCompletionsSectionHeadersForm.setText(newText);
    }
    

}

public class RobotPreferencesPage implements Configurable {

    private RobotPreferencesComponent component;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Robot Framework Language Server";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getPreferredFocusedComponent();
    }

    @Override
    public @Nullable JComponent createComponent() {
        component = new RobotPreferencesComponent();
        return component.getPanel();
    }

    @Override
    public boolean isModified() {
        RobotPreferences settings = RobotPreferences.getInstance();
        
        if(!settings.getRobotLanguageServerPython().equals(component.getRobotLanguageServerPython())){
            return true;
        }
        
        if(!settings.getRobotLanguageServerArgs().equals(component.getRobotLanguageServerArgs())){
            return true;
        }
        
        if(!settings.getRobotLanguageServerTcpPort().equals(component.getRobotLanguageServerTcpPort())){
            return true;
        }
        
        if(!settings.getRobotPythonExecutable().equals(component.getRobotPythonExecutable())){
            return true;
        }
        
        if(!settings.getRobotPythonEnv().equals(component.getRobotPythonEnv())){
            return true;
        }
        
        if(!settings.getRobotVariables().equals(component.getRobotVariables())){
            return true;
        }
        
        if(!settings.getRobotPythonpath().equals(component.getRobotPythonpath())){
            return true;
        }
        
        if(!settings.getRobotCompletionsSectionHeadersForm().equals(component.getRobotCompletionsSectionHeadersForm())){
            return true;
        }
        
        return false;
    }

    @Override
    public void reset() {
        RobotPreferences settings = RobotPreferences.getInstance();
        
        component.setRobotLanguageServerPython(settings.getRobotLanguageServerPython());
        component.setRobotLanguageServerArgs(settings.getRobotLanguageServerArgs());
        component.setRobotLanguageServerTcpPort(settings.getRobotLanguageServerTcpPort());
        component.setRobotPythonExecutable(settings.getRobotPythonExecutable());
        component.setRobotPythonEnv(settings.getRobotPythonEnv());
        component.setRobotVariables(settings.getRobotVariables());
        component.setRobotPythonpath(settings.getRobotPythonpath());
        component.setRobotCompletionsSectionHeadersForm(settings.getRobotCompletionsSectionHeadersForm());
    }

    @Override
    public void apply() throws ConfigurationException {
        RobotPreferences settings = RobotPreferences.getInstance();
        
        settings.setRobotLanguageServerPython(component.getRobotLanguageServerPython());
        settings.setRobotLanguageServerArgs(component.getRobotLanguageServerArgs());
        settings.setRobotLanguageServerTcpPort(component.getRobotLanguageServerTcpPort());
        settings.setRobotPythonExecutable(component.getRobotPythonExecutable());
        settings.setRobotPythonEnv(component.getRobotPythonEnv());
        settings.setRobotVariables(component.getRobotVariables());
        settings.setRobotPythonpath(component.getRobotPythonpath());
        settings.setRobotCompletionsSectionHeadersForm(component.getRobotCompletionsSectionHeadersForm());
    }
}