package io.onedev.server.web.component.job;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobManager;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.project.builds.detail.BuildLogPage;

@SuppressWarnings("serial")
public class RunJobLink extends AjaxLink<Void> {

	private final IModel<Project> projectModel;
	
	private final ObjectId commitId;
	
	private final String jobName;
	
	public RunJobLink(String componentId, Project project, ObjectId commitId, @Nullable String jobName) {
		super(componentId);
		
		this.projectModel = new EntityModel<Project>(project);
		this.commitId = commitId;
		this.jobName = jobName;
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		CISpec ciSpec = Preconditions.checkNotNull(getProject().getCISpec(commitId));
		Job job;
		if (jobName != null) 
			job = Preconditions.checkNotNull(ciSpec.getJobMap().get(jobName));
		else if (ciSpec.getJobs().size() == 1)
			job = ciSpec.getJobs().iterator().next();
		else
			job = null;
		if (job == null || !job.getParamSpecs().isEmpty()) {
			new ModalPanel(target) {

				@Override
				protected Component newContent(String id) {
					return new JobParamEditPanel(id, job) {

						@Override
						protected void onSave(Job job, Serializable paramBean) {
							Map<String, List<String>> paramMap = JobParam.getParamMap(
									job, paramBean, job.getParamSpecMap().keySet());
							Build build = OneDev.getInstance(JobManager.class).submit(getProject(), 
									commitId, job.getName(), paramMap, SecurityUtils.getUser());
							setResponsePage(BuildLogPage.class, BuildLogPage.paramsOf(build, null));
						}

						@Override
						protected Project getProject() {
							return RunJobLink.this.getProject();
						}
						
					};
				}
				
			};
		} else {
			Build build = OneDev.getInstance(JobManager.class).submit(getProject(), commitId, 
					job.getName(), new HashMap<>(), SecurityUtils.getUser());
			setResponsePage(BuildLogPage.class, BuildLogPage.paramsOf(build, null));
		}
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canWriteCode(getProject().getFacade()));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}
	
}
