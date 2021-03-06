package io.onedev.server.web.page.project.setting.general;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.project.ConfirmDeleteProjectModal;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class GeneralSettingPage extends ProjectSettingPage {

	private String oldName;
	
	private BeanEditor editor;
	
	public GeneralSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		editor = BeanContext.editModel("editor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getProject();
			}

			@Override
			public void setObject(Serializable object) {
				// check contract of projectManager.save on why we assign oldName here
				oldName = getProject().getName();
				editor.getDescriptor().copyProperties(object, getProject());
			}
			
		}, Lists.newArrayList("name", "description", "defaultPrivilege"), false);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Project project = getProject();
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				Project projectWithSameName = projectManager.find(project.getName());
				if (projectWithSameName != null && !projectWithSameName.equals(project)) {
					String errorMessage = "This name has already been used by another project"; 
					editor.getErrorContext(new PathElement.Named("name")).addError(errorMessage);
				} else {
					projectManager.save(project, oldName);
					Session.get().success("General setting has been updated");
					setResponsePage(GeneralSettingPage.class, paramsOf(project));
				}
			}
			
		};
		form.add(editor);

		form.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ConfirmDeleteProjectModal(target) {
					
					@Override
					protected void onDeleted(AjaxRequestTarget target) {
						setResponsePage(ProjectListPage.class);						
					}
					
					@Override
					protected Project getProject() {
						return GeneralSettingPage.this.getProject();
					}

				};
			}
			
		});
		
		add(form);
	}

}
