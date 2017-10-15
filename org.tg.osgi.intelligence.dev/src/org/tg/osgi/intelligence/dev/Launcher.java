package org.tg.osgi.intelligence.dev;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.tg.osgi.intelligence.obr.LocalRepoAdm;
import org.tg.osgi.intelligence.obr.RepositoryAdm;
import org.tg.osgi.intelligence.plan.Planner;

import goalp.model.Artifact;
import goalp.systems.PlanSelectionException;

//import org.apache.felix.bundlerepository.*;
import org.osgi.service.resolver.Resolver;


public class Launcher {
	
	public static final int START = 0x00010;
	
	
	//private static String[] libs = null;
	private Framework framework = null;
	private RepositoryAdm repoAdmin = null;
	private LocalRepoAdm localRepo = new LocalRepoAdm();
	private BundleContext context;
	private Planner planner;
	private List<String> configBundles;
	private List<String> userBundles;
	private List<String> scenarioResources;

	Launcher() {
		
		configBundles = new ArrayList<String>();
		userBundles = new ArrayList<String>();
		scenarioResources = new ArrayList<String>();
		
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

		framework = frameworkFactory.newFramework(config);

		try {
			framework.start();
		} catch (BundleException e) {
			e.printStackTrace();
		}
		
		System.out.println("OSGi Framework successfully started!");

		//context = framework.getBundleContext();
		setContext();
		
		// framework bundles
		start("org.eclipse.osgi.services", configBundles);
		start("org.eclipse.osgi.util", configBundles);
		start("org.eclipse.equinox.common", configBundles);
		start("org.eclipse.equinox.registry", configBundles);
		start("org.eclipse.equinox.preferences", configBundles);
		start("org.eclipse.equinox.app", configBundles);
		start("org.eclipse.core.jobs", configBundles);
		start("org.eclipse.equinox.util", configBundles);
		start("org.eclipse.equinox.ds", configBundles);
		start("org.eclipse.core.contenttype", configBundles);
		start("org.eclipse.core.runtime", configBundles);
		start("org.eclipse.equinox.security", configBundles);
		start("org.eclipse.equinox.event", configBundles);
		start("org.apache.felix.bundlerepository", configBundles);		//Adiciona o OBR
		
		// default shell
		start("org.apache.felix.gogo.runtime", configBundles);	//runtime do felix
		start("org.apache.felix.gogo.command", configBundles);
		start("org.apache.felix.gogo.shell", configBundles);
		start("org.eclipse.equinox.console", configBundles);
		
		
		
		//Repository 
		repoAdmin = new RepositoryAdm(context);
		//repoAdmin.addRepository("http://felix.apache.org/obr/releases.xml");		
		repoAdmin.addRepository("file:/C:/Users/jpaul/Documents/Workspaces/localrepo/repository.xml");
		repoAdmin.addRepository("file:/C:/Users/jpaul/.m2/repository/repository.xml");
		//repoAdmin.addRepository("http://sling.apache.org/obr/sling.xml");
		repoAdmin.printListResources(null);
		

		//System.out.println("Fim do construtor!");
	}
	
	RepositoryAdm getRepoAdm(){
		return this.repoAdmin;
	}
	
	public void setContext() {
		context = framework.getBundleContext();
		
		context.addBundleListener(new BundleListener(){
			@Override
			public void bundleChanged(BundleEvent event) {
				switch (event.getType()) {
				case BundleEvent.RESOLVED:
	                break;
	            case BundleEvent.INSTALLED:
	            	uninstall(event.getBundle().getSymbolicName());
	                break;
				case BundleEvent.STARTED:
	                break;
	            case BundleEvent.UNINSTALLED:
	            	System.out.println("LISTENER: Bundle " + event.getBundle().getSymbolicName() + " Uninstalled!");
	            	//Bundle b = start(event.getBundle().getSymbolicName());
	            	/*
	            	if (b ==null)
	            			String = planejador.replaneja(contexto);
	            			deployResources(String)
	            			*/
	                break;
				case BundleEvent.UNRESOLVED:
					System.out.println("LISTENER: Bundle " + event.getBundle().getSymbolicName() + " Unresolved!");
					//start(event.getBundle().getSymbolicName());
	                break;
				case BundleEvent.STOPPED:
					System.out.println("LISTENER: Bundle " + event.getBundle().getSymbolicName() + " Stopped!");
					//start(event.getBundle().getSymbolicName());
	                break; 
				}
			}
		});
	}
	
	public void addScenarioResource (String resource) {
		if (!scenarioResources.contains(resource))
			scenarioResources.add(resource);
	}
	
	public void removeScenarioResource (String resource) {
		if (!scenarioResources.contains(resource)) {
			System.out.println("ERROR: Resource not present in current scenario");
			return;
		}
		scenarioResources.remove(resource);
	}
	
	public void executeGoal(String goal) {
		planner = new Planner (goal, scenarioResources);
		try {
			planner.exec();
		} catch (PlanSelectionException e) {
			System.out.println("ERROR: Couldn't do the planning");
			e.printStackTrace();
		}
		if (planner.getResult() == null) {
			System.out.println("ERROR: Could not do the planning for " + goal);
			return;
		}
		
		for (Artifact artifact  : planner.getResult().getResultPlan().plan.getSelectedArtifacts())
			start(artifact.getIdentification(), userBundles);
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
	
	protected Bundle start(String name, List<String> bundleList) {
		BundleListener listener;
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
				bundleList.add(name);
				return newBundle;
			}
				
		}
		
		bundleList.add(name);
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
}
