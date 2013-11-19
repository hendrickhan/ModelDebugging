package org.gemoc.execution.engine.core;

import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.gemoc.execution.engine.Activator;
import org.gemoc.gemoc_language_workbench.api.dsa.EventExecutor;
import org.gemoc.gemoc_language_workbench.api.dse.ModelSpecificEvent;
import org.gemoc.gemoc_language_workbench.api.feedback.FeedbackData;
import org.gemoc.gemoc_language_workbench.api.feedback.FeedbackPolicy;
import org.gemoc.gemoc_language_workbench.api.moc.Solver;
import org.gemoc.gemoc_language_workbench.api.utils.ModelLoader;

import fr.inria.aoste.trace.LogicalStep;

/**
 * Basic abstract implementation of the ExecutionEngine, independent from the
 * technologies used for the solver, the executor and the feedback protocol.
 * 
 * 
 * @author flatombe
 * 
 */
public abstract class BasicExecutionEngine implements ExecutionEngine {

	protected ModelLoader modelLoader = null;
	protected Solver solver = null;
	protected EventExecutor executor = null;
	protected FeedbackPolicy feedbackPolicy = null;
	protected Resource domainSpecificEventsResource = null;

	protected Resource modelResource = null;
	protected String modelStringURI = null;

	public BasicExecutionEngine(Resource domainSpecificEventsResource,
			Solver solver, EventExecutor executor, FeedbackPolicy feedbackPolicy) {

		// TODO : REMOVE ME
		if(domainSpecificEventsResource == null){
			ResourceSet resSet = new ResourceSetImpl();
			domainSpecificEventsResource = resSet.getResource(URI.createURI("platform:/resource/org.gemoc.sample.tfsm.ecldse/dse/TFSM.ecl"), true);
		}
		
		Activator.getMessagingSystem().info(
				"Verifying input before instanciating BasicExecutionEngine...",
				Activator.PLUGIN_ID);
		// The engine needs AT LEAST a domainSpecificEventsResource, a Solver,
		// an EventExecutor.
		if (domainSpecificEventsResource == null | solver == null
				| executor == null | feedbackPolicy == null) {
			String exceptionMessage = "";
			if (domainSpecificEventsResource == null) {
				exceptionMessage += "domainSpecificEventsResource is null, ";
			}
			if (solver == null) {
				exceptionMessage += "solver is null, ";
			}
			if (executor == null) {
				exceptionMessage += "eventExecutor is null, ";
			}
			if (exceptionMessage.endsWith(", ")) {
				exceptionMessage = exceptionMessage.substring(0,
						exceptionMessage.length() - 2);
			}
			Activator.getMessagingSystem().info(
					"...NOK. Throwing NullPointerException.",
					Activator.PLUGIN_ID);
			throw new NullPointerException(exceptionMessage);
		} else {
			Activator.getMessagingSystem().info(
					"...OK. Instantiating BasicExecutionEngine with...",
					Activator.PLUGIN_ID);
			Activator
					.getMessagingSystem()
					.info("\tDomainSpecificEventsResource="
							+ domainSpecificEventsResource, Activator.PLUGIN_ID);
			Activator.getMessagingSystem().info("\tSolver=" + solver,
					Activator.PLUGIN_ID);
			Activator.getMessagingSystem().info("\tExecutor=" + executor,
					Activator.PLUGIN_ID);

			if (feedbackPolicy == null) {
				String msg = "FeedbackPolicy is null";
				Activator.warn(msg, new NullPointerException(msg));
			} else {
				this.feedbackPolicy = feedbackPolicy;
				Activator.getMessagingSystem().info(
						"\tFeedbackPolicy=" + feedbackPolicy,
						Activator.PLUGIN_ID);
			}

			this.domainSpecificEventsResource = domainSpecificEventsResource;
			this.solver = solver;
			this.executor = executor;
			this.executor.initialize();
		}
	}

	/**
	 * Instantiates a list of Domain Specific Events depending on which event
	 * occurrences are in the Step returned by the Solver.
	 * 
	 * Depends on the implementation used for the Solver, Step and Domain
	 * Specific Event.
	 * 
	 * @param step
	 * @return
	 */
	protected abstract List<ModelSpecificEvent> match(LogicalStep step);

	@Override
	public void run() {
		Activator.getMessagingSystem().info("Starting running indefinitely",
				Activator.PLUGIN_ID);
		this.run(-1);
		Activator.getMessagingSystem().info("Stopped running indefinitely",
				Activator.PLUGIN_ID);
	}

	@Override
	public void run(int numberOfSteps) {
		Activator.getMessagingSystem().info(
				"Running " + numberOfSteps + " steps", Activator.PLUGIN_ID);
		for (int i = 0; i < numberOfSteps; i++) {
			this.runOneStep();
		}
	}

	@Override
	public void runOneStep() {
		ISafeRunnable runnable = new ISafeRunnable() {
			@Override
			public void handleException(Throwable e) {
				Activator.error(e.getMessage(), e);
			}

			@Override
			public void run() throws Exception {
				Activator.getMessagingSystem().info(">>Running one step",
						Activator.PLUGIN_ID);
				BasicExecutionEngine.this.doOneStep();
				Activator.getMessagingSystem().info("<<Step finished",
						Activator.PLUGIN_ID);
			}
		};
		SafeRunner.run(runnable);

	}

	private void doOneStep() {
		// Retrieve information from the solver.
		LogicalStep step = this.solver.getNextStep();
		Activator.getMessagingSystem().debug(
				"The solver has correctly returned a step to the engine",
				Activator.PLUGIN_ID);

		// Create the Domain Specific Events according to the information
		// returned to us by the solver.
		List<ModelSpecificEvent> events = this.match(step);
		Activator.getMessagingSystem().info(
				"Number of events matched : " + events.size(),
				Activator.PLUGIN_ID);

		// For each event, execute its action(s) and take into account the
		// feedback the Domain Specific Action returns.
		for (ModelSpecificEvent event : events) {
			Activator.getMessagingSystem().debug(
					"Executing the following event: " + event.toString(),
					Activator.PLUGIN_ID);
			List<FeedbackData> feedbacks = this.executor.execute(event);
			for (FeedbackData feedback : feedbacks) {
				Activator.getMessagingSystem().debug(
						"Feedback received: " + feedback.toString(),
						Activator.PLUGIN_ID);
				if (this.feedbackPolicy != null) {
					this.feedbackPolicy.processFeedback(feedback, solver);
				}
			}
		}
	}

	@Override
	public EventExecutor getExecutor() {
		return this.executor;
	}

	@Override
	public Solver getSolver() {
		return this.solver;
	}

	@Override
	public FeedbackPolicy getFeedbackPolicy() {
		return this.feedbackPolicy;
	}

	@Override
	public ModelLoader getModelLoader() {
		return this.modelLoader;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "@[Executor=" + this.executor
				+ " ; Solver=" + this.solver + " ; ModelResource="
				+ this.modelResource + "]";
	}
}
