package org.goald.osgi.intelligence.dev;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.tg.osgi.intelligence.obr.LocalRepoAdm;
import org.tg.osgi.intelligence.obr.RepositoryAdm;
import org.tg.osgi.intelligence.obr.RepositoryMock;
import org.tg.osgi.intelligence.plan.Planner;

import goald.model.Agent;
import goald.model.CtxEvaluator;
import goald.model.Dame;
import goald.model.DeploymentPlan;
import goald.model.Goal;
import goald.model.DeploymentPlan.Command;
import goald.planning.DamUpdater;
import goald.planning.DameRespository;
import goald.planning.DeploymentPlanner;

/*
import goald.model.Agent;
import goald.planning.DamUpdater;
import goald.planning.DameRespository;
import goalp.evaluation.ExperimentTimerImpl;
import goalp.evaluation.ExperimentTimerImpl.Split;
import goalp.evaluation.model.ExecResult;
import goalp.model.Artifact;
import goalp.systems.PlanSelectionException;
*/
public class Launcher {
	
	public static final int START = 0x00010;

	private Framework framework = null;
	private RepositoryAdm repoAdmin = null;
	private LocalRepoAdm localRepo = new LocalRepoAdm();
	private BundleContext context;
	private List<String> userBundles;
	private List<String> scenarioResources;
	//private ExperimentTimerImpl timer = new ExperimentTimerImpl();
	//Planner planner = new Planner();
	
	DamUpdater updater;
	DameRespository repo;
	Agent agent;

	Launcher() {
		
		userBundles = new ArrayList<String>();
		scenarioResources = new ArrayList<String>();
		
		//timer.begin();
		
		FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
		
		Map<String, String> config = new HashMap<String, String>();
		config.put("osgi.console", "");
		config.put("osgi.clean", "true");
		config.put("osgi.noShutdown", "true");
		config.put("eclipse.ignoreApp", "true");
		config.put("osgi.bundles.defaultStartLevel", "4");
		config.put("osgi.configuration.area", "./configuration");

		// automated bundles deployment
		config.put("felix.fileinstall.dir", "./dropins");
		config.put("felix.fileinstall.noInitialDelay", "true");
		config.put("felix.fileinstall.start.level", "4");
		
		//Criar o RepositoryAdm sem problemas
		config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "org.apache.felix.bundlerepository; version=2.0.10");
		//config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "SendSMS-impl");

		framework = frameworkFactory.newFramework(config);

		try {
			framework.start();
		} catch (BundleException e) {
			e.printStackTrace();
		}
		
		System.out.println("OSGi Framework successfully started!");
		
		context = framework.getBundleContext();
		
		// framework bundles
		start("org.eclipse.osgi");
		start("org.eclipse.osgi.services");
		//start("org.osgi.service.component");
		start("org.eclipse.osgi.util");
		//start("org.osgi.util.promise");
		start("org.eclipse.equinox.common");
		start("org.eclipse.equinox.registry");
		start("org.eclipse.equinox.preferences");
		start("org.eclipse.equinox.app");
		start("org.eclipse.core.jobs");
		start("org.eclipse.equinox.util");
		start("org.eclipse.equinox.ds");
		start("org.eclipse.core.contenttype");
		start("org.eclipse.core.runtime");
		start("org.eclipse.equinox.security");
		start("org.eclipse.equinox.event");
		start("org.apache.felix.bundlerepository");		//Adiciona o OBR
		
		// default shell
		start("org.apache.felix.gogo.runtime");	//runtime do felix
		start("org.apache.felix.gogo.command");
		start("org.apache.felix.gogo.shell");
		//start("org.apache.felix.scr");
		start("org.eclipse.equinox.console");
		
		
		
		repo = RepositoryMock.regRepo();
		//Repository 
		
		repoAdmin = new RepositoryAdm(context);
		//repoAdmin.addRepository("http://felix.apache.org/obr/releases.xml");		
		//repoAdmin.addRepository("file:/C:/Users/jpaul/Documents/Workspaces/localrepo/repository.xml");
		repoAdmin.addRepository("file:/C:/Users/jpaul/.m2/repository/repository.xml");
		//repoAdmin.addRepository("http://sling.apache.org/obr/sling.xml");
		
		
		//setListener();
		//System.out.println("Fim do construtor!");
		//timer.split("Setup environment");

	}
	
	RepositoryAdm getRepoAdm(){
		return this.repoAdmin;
	}
	/*
	public ExperimentTimerImpl getTimer () {
		return timer;
	}
	
	public void printTimerResults () {
		for (Split split : timer.result()) {
			System.out.println(split.toString());
		}
	}*/
	
	public void setListener() {
		
		
		context.addBundleListener(new BundleListener(){
			@Override
			public void bundleChanged(BundleEvent event) {
				switch (event.getType()) {
				case BundleEvent.RESOLVED:
	                break;
	            case BundleEvent.INSTALLED:
	            	System.out.println("LISTENTER: Bundle " + event.getBundle().getSymbolicName() + " installed!");
	            	if (!userBundles.contains(event.getBundle().getSymbolicName()))
	            		userBundles.add(event.getBundle().getSymbolicName());
	                break;
				case BundleEvent.STARTED:
					if (!userBundles.contains(event.getBundle().getSymbolicName()))
						userBundles.add(event.getBundle().getSymbolicName());
	                break;
	            case BundleEvent.UNINSTALLED:
	            	if (userBundles.contains(event.getBundle().getSymbolicName()))
	            		userBundles.remove(event.getBundle().getSymbolicName());
	            	System.out.println("LISTENER: Bundle " + event.getBundle().getSymbolicName() + " Uninstalled!");
	                break;
				case BundleEvent.UNRESOLVED:
					System.out.println("LISTENER: Bundle " + event.getBundle().getSymbolicName() + " Unresolved!");
	                break;
				case BundleEvent.STOPPED:
					System.out.println("LISTENER: Bundle " + event.getBundle().getSymbolicName() + " Stopped!");
	                break; 
				}
			}
		});
	}
	
	
	
	
	/*
	 * Adds a scenario resource to the context of the environment
	 */
	public void addScenarioResource (String resource) {
		if (!planner.getScenario().contains(resource))
			planner.getScenario().add(resource);
	}
	
	public List<String> artifactsToString (Set<Artifact> artifacts) {
		List<String> bundleNames = new ArrayList<String>();
		for (Artifact art : artifacts)
			bundleNames.add(art.getIdentification());
		return bundleNames;
	}
	
	/*
	 * Make a new plan to recover from a crash of a environment context
	 * */
	public void replan() {
		List<String> newPlan = null;
		List<String> newBundles = null;
		List<String> toRemove = null;
		
		//planner.getPlan();
		
		if (planner == null) {
			System.out.println("ERROR: Sorry, You don't have enough resources to replan");
			return;
		}/*
		for (Artifact art : planner.getResult().getResultPlan().plan.getSelectedArtifacts()) {
				newPlan.add(art.getIdentification());
		}*/
		
		newBundles = new ArrayList<String>(newPlan);
		toRemove = new ArrayList<String>(userBundles);
		
		newBundles.removeAll(userBundles);	// new bundles to install
		toRemove.removeAll(newPlan);		// old bundles to remove
		
		//for (String bundle : newBundles)
		//	executeGoal(bundle, newBundles);
		
		for (String bundle : toRemove)
			uninstall(bundle);
	}
	
	public void removeScenarioResource (String resource) {
		if (!planner.getScenario().contains(resource)) {
			System.out.println("ERROR: Resource not present in current scenario");
			return;
		}
		planner.getScenario().remove(resource);
		System.out.println("Resource " + resource + " not available anymore...");
		System.out.println("Replanning...");
		replan();
	}
	
	
	public BundleContext executeGoal(Agent agent, CtxEvaluator ctx, List<Goal> query) {
		updater = new DamUpdater(repo, agent);
		Dame rootDame = updater.resolveGoals(query).get(0);		
		agent.setRootDame(rootDame);
		
		DeploymentPlanner deploymentPlanner = new DeploymentPlanner(repo, agent);
		DeploymentPlan plan = deploymentPlanner.createPlan();
		if (plan.getCommands() == null) {
			System.out.println("The plan got no results..");
			return null;
		}
		for (Command command : plan.getCommands()) {
			System.out.println(command.toString());
			switch (command.getOp()) {
				case INSTALL:
					start(command.getBundle().getIdentification());
					break;
				case REFRESH:
					break;
				case RESOLVE:
					break;
				case START:
					break;
				case STOP:
					break;
				case UNINSTALL:
					break;
				case UPDATE:
					break;
				default:
					break;
			}
			
			
		}
		/*
		for (Command command1 : plan.getCommands()) {
			System.out.println(command1.toString());
		}
		*/
		//retorna null...
		return context;
	}
	/*
	public void executeGoal(String goal) {
		
		planner.setExpName(goal);
		planner.getPlan();
		if (planner == null) return;
		
		for (Artifact artifact  : planner.getResult().getResultPlan().plan.getSelectedArtifacts())
			start(artifact.getIdentification());
	}
	*/
	public void removeAllBundles () {
		for (int i = userBundles.size()-1; i >= 0; i--) {
			uninstall(userBundles.get(i));
		}
	}
	
	public void removeAllScenarioRes () {
		for (int i = planner.getScenario().size()-1; i >= 0; i--)
			planner.getScenario().remove(i);
	}
	
	public void cleanScenario () {
		removeAllBundles();
		removeAllScenarioRes();
	}
	
	public void printBundles () {
		System.out.println(Arrays.asList(userBundles));
	}

	protected Bundle search4BundleRemotelly(String name, int shouldStart) {
		String filterExpr = null;
		Boolean deployed = false;
		
		System.out.println("Looking for bundle " + name + " remotelly...");
		
		if (name.endsWith(".jar")) {
			System.out.println("ERROR: Bad Bundle Symbolic name format!");
			return null;
		}
		
		filterExpr = String.format("(symbolicname=" + name + ")");
		deployed = repoAdmin.deployResource(filterExpr, shouldStart);
		if (deployed) {		//Achou o bundle e instalou com sucesso!
			return getBundleBySymbolicName(name);
		}

		return null;
	}
	
	protected Bundle getBundleBySymbolicName(String name) {
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getSymbolicName().equals(name)) {
				return bundle;
			}
		}
		return null;
	}

	protected Bundle install(String name, int shouldStart) {
		Bundle newBundle = null;
		String jarPath = localRepo.search4BundleLocally(name);
		
		if (jarPath != null) {
			newBundle = localRepo.installLocalBundle(jarPath, context);
			if (newBundle == null) {
				System.out.println("ERROR: Couldn't install local bundle " + name);
				return null;
			}
			System.out.println("Local Bundle " + name +" installed successfully!");
			return newBundle;
		}
		else {	//Bundle not found locally
			newBundle = search4BundleRemotelly(name, shouldStart);
			if (newBundle == null) return null;
			return newBundle;
		}
	}
	
	protected Boolean startBundle(Bundle newBundle) {
		if (newBundle != null) {				//Bundle no framework
			if (localRepo.checkBundleActive(newBundle))	//Bundle ja esta ativo
				return true;
											//bundle instalado, mas nao ativo
			try {
				newBundle.start();
				System.out.println("Bundle " + newBundle.getSymbolicName() + " started successfully!");
				return true;
			} catch (BundleException e) {
				if (e.getType() == BundleException.RESOLVE_ERROR) {
					// Plan order of bundles didn't work
					System.out.println("ERROR: While starting remote bundle. Probably the plan order didn't work!");
				}
			}
		
		}
		return false;
	}
	
	protected Bundle start(String name) {
		Bundle newBundle = getBundleBySymbolicName(name);
		
		if (newBundle != null) {	//Bundle no framework
			if (startBundle(newBundle)){
				return newBundle;
			}
			System.out.println("ERROR: Couldn't start bundle " + name);
			return null;
		}
		
		//Bundle nao esta no framework
		newBundle = install(name, START);
		if (!localRepo.checkBundleActive(newBundle)) {
			if(startBundle(newBundle)){
				//bundleList.add(name);
				return newBundle;
			}
				
		}
		
		//bundleList.add(name);
		return newBundle;
	}
	
	protected void stop(String name) {
		Bundle newBundle = getBundleBySymbolicName(name);
		
		//verifica se esta ativo
		if (!localRepo.checkBundleActive(newBundle)) {
			System.out.println("Bundle " + name +" not ACTIVE!");
			return;
		}
		try {
			newBundle.stop();
			System.out.println("Bundle " + name + " stopped!");
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
	
	protected void uninstall(String name) {
		Bundle newBundle = getBundleBySymbolicName(name);
		
		if(newBundle == null) {
			System.out.println("ERROR: Bundle " + name  + " not found. Couldn't unninstall!");
		}
		
		//verifica se esta ativo
		if (!localRepo.checkBundleInstalled(newBundle)) {
			System.out.println("Bundle " + name +" not Installed!");
			return;
		}
		try {
			newBundle.uninstall();
			System.out.println("Bundle " + name + " uninstalled!");
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
	
	protected void shutdown() throws BundleException {
		try {
			framework.stop();
			System.out.println("OSGi Framework successfully stopped!");
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/*
	 * Adds a scenario resource to the environment and fetch related bundles from repository.
	 *//*
	public void addScenarioResourceG (String resource, String goalReached) {
		Planner tempPlan = new Planner(goalReached, Arrays.asList(resource)); 		
		tempPlan.getPlan();
		
		if (tempPlan.getResult() == null) {
			System.out.print("ERROR: Could not get partial plan");
		}
		
		for (Artifact artifact  : tempPlan.getResult().getResultPlan().plan.getSelectedArtifacts())
			start(artifact.getIdentification());
			
		if (!planner.getScenario().contains(resource))
			planner.getScenario().add(resource);
	}*/
}
