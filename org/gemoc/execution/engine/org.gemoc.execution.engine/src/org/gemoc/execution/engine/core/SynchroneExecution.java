package org.gemoc.execution.engine.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.gemoc.execution.engine.Activator;
import org.gemoc.gemoc_language_workbench.api.core.IExecutionCheckpoint;
import org.gemoc.gemoc_language_workbench.api.core.IExecutionEngine;
import org.gemoc.gemoc_language_workbench.api.dsa.CodeExecutionException;
import org.gemoc.gemoc_language_workbench.api.engine_addon.IEngineAddon;

import fr.inria.aoste.timesquare.ecl.feedback.feedback.ActionCall;
import fr.inria.aoste.timesquare.ecl.feedback.feedback.FeedbackFactory;
import fr.inria.aoste.timesquare.ecl.feedback.feedback.ModelSpecificEvent;

public class SynchroneExecution extends OperationExecution 
{

	protected SynchroneExecution(ModelSpecificEvent mse, IExecutionEngine engine) 
	{
		super(mse, engine);
	}

	@Override
	public void run() 
	{
		for (IEngineAddon addon : getEngine().getExecutionContext().getExecutionPlatform().getEngineAddons()) 
		{
			addon.aboutToExecuteMSE(getEngine(), getMSE());
		}
		Object res = callExecutor();
		setResult(res);
		try {
			applyAnimationTime();
		} catch (InterruptedException e) {
			Activator.getDefault().error("Exception received " + e.getMessage(), e);
		}
		for (IEngineAddon addon : getEngine().getExecutionContext().getExecutionPlatform().getEngineAddons()) 
		{
			addon.mseExecuted(getEngine(), getMSE());
		}
	}
	
	/**
	 * Calls the {@link EventExecutor} for the given
	 * {@link EngineEventOccurence}.
	 * 
	 * @param mse
	 *            the {@link EngineEventOccurence} to execute
	 * @return the {@link FeedbackData} if any, <code>null</code> other wise
	 */
	private Object callExecutor() 
	{
		final TransactionalEditingDomain editingDomain = TransactionalEditingDomain.Factory.INSTANCE.getEditingDomain(getExecutionContext().getResourceModel().getResourceSet());
		Object res = null;
		final ActionCall call = FeedbackFactory.eINSTANCE.createActionCall();
		call.setTriggeringEvent(getMSE());
		if (editingDomain != null) {
			final RecordingCommand command = new RecordingCommand(editingDomain, "execute engine event occurence " + getMSE()) {
				private List<Object> result = new ArrayList<Object>();

				@Override
				protected void doExecute() {
					try {
						result.add(getExecutionContext().getExecutionPlatform().getCodeExecutor().execute(call));
					} catch (CodeExecutionException e) {
						Activator.getDefault().error("Exception received " + e.getMessage(), e);
					}
				}

				@Override
				public Collection<?> getResult() {
					return result;
				}
			};
			IExecutionCheckpoint checkpoint = IExecutionCheckpoint.CHECKPOINTS.get(editingDomain.getResourceSet());
			try {
				if (checkpoint != null) {
					checkpoint.allow(true);
				}
				editingDomain.getCommandStack().execute(command);
			} finally {
				if (checkpoint != null) {
					checkpoint.allow(false);
				}
			}
			res = (Object) command.getResult().iterator().next();
		} else {
			try {
				res = getExecutionContext().getExecutionPlatform().getCodeExecutor().execute(call);
			} catch (CodeExecutionException e) { 
				Activator.getDefault().error("Exception received " + e.getMessage(), e);
			}
		}
		return res;
	}
	
	private void applyAnimationTime() throws InterruptedException {
		int animationDelay = getEngine().getExecutionContext().getRunConfiguration().getAnimationDelay();								
		if (animationDelay != 0) 
		{
			Thread.sleep(animationDelay);
		}
	}

}
