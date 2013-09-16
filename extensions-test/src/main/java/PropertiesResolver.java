import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: eli
 * Date: 9/15/13
 * Time: 9:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertiesResolver extends AbstractExecutionListener {
    private final Logger mLog;
    private Namespace mNamespace;
    private Namespace mXsiNamespace;
    private final ExecutionListener delegate;

    public PropertiesResolver(ExecutionListener executionListener, Logger log) {
        delegate = executionListener;
        mLog = log;
        mNamespace = Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");
        mXsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }


    @Override
    public void mojoStarted(ExecutionEvent event) {
        delegate.mojoStarted(event);
        String lifecyclePhase = event.getMojoExecution().getLifecyclePhase();
        if (lifecyclePhase != null && lifecyclePhase.equals("install")) {
            MavenProject project = event.getProject();
            if (!project.getPackaging().equals("pom")) {
                generateResolvedPom(project);
            }
        }
    }

    @Override
    public void mojoSucceeded(ExecutionEvent event){
        delegate.mojoSucceeded( event );
    }

    private void generateResolvedPom(MavenProject project) {
        mLog.error("**** Resolving properties in pom for project: " + project.getName() + " ("
                + project.getGroupId() + ":" + project.getArtifactId() + ")");

        String buildDir = project.getBuild().getDirectory();
        String resolvedPomFileName = buildDir + File.separator + "resolved-pom.xml";
        Element projectElement = new Element("project");
        projectElement.addNamespaceDeclaration(mXsiNamespace);
        projectElement.setAttribute("schemaLocation",
                "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd",
                mXsiNamespace);
        Document doc = new Document(projectElement);

        addContent(doc, "modelVersion", "4.0.0");
        addContent(doc, "name", project.getName());
        addContent(doc, "groupId", project.getGroupId());
        addContent(doc, "artifactId", project.getArtifactId());
        addContent(doc, "version", project.getVersion());
        addContent(doc, "packaging", project.getPackaging());
        addContent(doc, "description", project.getDescription());

        Element dependenciesElement = new Element("dependencies");
        List<Dependency> dependencies = project.getDependencies();
        for (Dependency dependency : dependencies) {
            Element dependencyElement = createDependencyElement(dependency);
            dependenciesElement.addContent(dependencyElement);
        }
        doc.getRootElement().addContent(dependenciesElement);

// save the file
        Format format = Format.getPrettyFormat();
        format.setLineSeparator(System.getProperty("line.separator"));
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(format);
        File file = new File(resolvedPomFileName);
        FileOutputStream fileOutputStream;
        try {
            file.getParentFile().mkdirs();
            fileOutputStream = new FileOutputStream(file);
            outputter.output(doc, fileOutputStream);
            mLog.debug("Writing file: " + file.getAbsolutePath());
            fileOutputStream.flush();
            fileOutputStream.close();
            project.setFile(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Element createDependencyElement(Dependency dependency) {
        Element dep = new Element("dependency");
        dep.addContent(new Element("groupId").setText(dependency.getGroupId()));
        dep.addContent(new Element("artifactId").setText(dependency.getArtifactId()));
        dep.addContent(new Element("version").setText(dependency.getVersion()));
        dep.addContent(new Element("classifier").setText(dependency.getClassifier()));
        dep.addContent(new Element("type").setText(dependency.getType()));
        return dep;
    }

    private void addContent(Document doc, String elementName, String value) {
        doc.getRootElement().addContent(new Element(elementName).setText(value));
    }
}
