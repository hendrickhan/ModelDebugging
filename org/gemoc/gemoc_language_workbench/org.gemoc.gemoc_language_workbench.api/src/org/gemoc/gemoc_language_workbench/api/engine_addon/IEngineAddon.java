package org.gemoc.gemoc_language_workbench.api.engine_addon;

import java.util.Collection;

import org.gemoc.execution.engine.trace.gemoc_execution_trace.LogicalStep;
import org.gemoc.execution.engine.trace.gemoc_execution_trace.MSEOccurrence;
import org.gemoc.gemoc_language_workbench.api.core.EngineStatus.RunStatus;
import org.gemoc.gemoc_language_workbench.api.core.IExecutionEngine;

public interface IEngineAddon {

	/**
	 * Operation called before the engine starts
	 */
	public void engineAboutToStart(IExecutionEngine engine);
	/**
	 * Operation called after the engine have started
	 */
	public void engineStarted(IExecutionEngine executionEngine);

	
	public void engineAboutToStop(IExecutionEngine engine);
	/**
	 * Operation called after the engine has been stopped
	 */
	public void engineStopped(IExecutionEngine engine);
	
	/**
	 * Operation before the engine has been disposed (and after the engine has been stopped)
	 */
	public void engineAboutToDispose(IExecutionEngine engine);
	
	
	/**
	 * Operation called before the LogicalStep has been chosen
	 */
	public void aboutToSelectLogicalStep(IExecutionEngine engine, Collection<LogicalStep> logicalSteps);
	
	public void proposedLogicalStepsChanged(IExecutionEngine engine, Collection<LogicalStep> logicalSteps);
	
	/**
	 * Operation called after the LogicalStep has been chosen
	 * It also returns the chosen LogicalStep
	 */
	public void logicalStepSelected(IExecutionEngine engine, LogicalStep selectedLogicalStep);
	

	public void aboutToExecuteLogicalStep(IExecutionEngine engine, LogicalStep logicalStepToExecute);
	public void logicalStepExecuted(IExecutionEngine engine, LogicalStep logicalStepExecuted);


	public void aboutToExecuteMSEOccurrence(IExecutionEngine engine, MSEOccurrence mseOccurrence);
	public void mseOccurrenceExecuted(IExecutionEngine engine, MSEOccurrence mseOccurrence);

	public void engineStatusChanged(IExecutionEngine engine, RunStatus newStatus);	
	
}
