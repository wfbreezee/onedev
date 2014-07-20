package com.pmease.gitop.web.component.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.service.AvatarManager;

@SuppressWarnings("serial")
public class AvatarByUser extends WebComponent {

	private IModel<String> avatarUrlModel;
	
	/**
	 * Display avatar of specified user model.
	 * 
	 * @param id
	 * 			component id
	 * @param userModel
	 * 			model of the user to display avatar for. If <tt>userModel.getObject()</tt>
	 * 			returns <tt>null</tt>, avatar of unknown user will be displayed
	 */
	public AvatarByUser(String id, IModel<User> userModel) {
		super(id, userModel);
		
		avatarUrlModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return Gitop.getInstance(AvatarManager.class).getAvatarUrl((User) getDefaultModelObject());
			}
			
		};
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.put("src", avatarUrlModel.getObject());
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof AvatarChanged) {
			AjaxRequestTarget target = ((AvatarChanged) event.getPayload()).getTarget();
			target.add(this);
		}
	}

	@Override
	protected void onDetach() {
		avatarUrlModel.detach();
		
		super.onDetach();
	}

}