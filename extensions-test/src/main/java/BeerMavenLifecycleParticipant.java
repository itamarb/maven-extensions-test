import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: eli
 * Date: 9/15/13
 * Time: 9:32 PM
 * To change this template use File | Settings | File Templates.
 */

@Component( role = AbstractMavenLifecycleParticipant.class, hint = "beer")
public class BeerMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant{

    @Requirement
    private Logger mLog;
    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        ExecutionListener executionListener = session.getRequest().getExecutionListener();
        session.getRequest().setExecutionListener(new PropertiesResolver(executionListener, mLog));
    }

    @Override
    public void afterSessionStart( MavenSession session )
            throws MavenExecutionException
    {
        // start the beer machine
    }


}
