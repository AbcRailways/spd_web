package importpackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@LocalBean
@Startup
public class InitImport {
	
	@EJB
	ImportProcessor importProc;
	
	public enum States {BEFORESTARTED, STARTED, PAUSED, SHUTTINGDOWN};

    private States state;

    @PostConstruct
    public void initialize() {

    	state = States.BEFORESTARTED;    
    	importProc.dirHandler();
    	state = States.STARTED;
    	System.out.println("Старт сканирования : "+state.toString());
    }

    @PreDestroy
    public void terminate() {    	
    	importProc.isCancelled=true;
    	state = States.SHUTTINGDOWN;
    	System.out.println("Shut down in progress");
    	Path sourceImportPath = Paths.get("C:\\Test\\import\\exit.txt");
    	try {
			Files.createFile(sourceImportPath);
			Files.deleteIfExists(sourceImportPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	    	
    }

    public States getState() {
    	return state;
    }

    public void setState(States state) {
    	this.state = state;
    }

}
