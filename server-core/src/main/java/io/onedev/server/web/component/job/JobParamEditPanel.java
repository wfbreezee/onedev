package io.onedev.server.web.component.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.base.Preconditions;

import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.model.Project;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;

@SuppressWarnings("serial")
abstract class JobParamEditPanel extends Panel implements InputContext {

	private Job job;
	
	public JobParamEditPanel(String id, @Nullable Job job) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		Serializable paramBean;
		try {
			paramBean = JobParam.defineBeanClass(job.getParamSpecs()).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> getInputNames() {
		if (job != null)
			return new ArrayList<>(job.getParamSpecMap().keySet());
		else
			return new ArrayList<>();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		if (job != null)
			return Preconditions.checkNotNull(job.getParamSpecMap().get(inputName));
		else
			return null;
	}

	@Override
	public void validateName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
	protected abstract Project getProject();

	protected abstract void onSave(Job job, Serializable paramBean);
	
}
