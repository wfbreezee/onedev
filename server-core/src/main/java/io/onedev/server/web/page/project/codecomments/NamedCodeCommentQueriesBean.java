package io.onedev.server.web.page.project.codecomments;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.NamedCodeCommentQuery;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.page.project.savedquery.NamedQueriesBean;

@Editable
public class NamedCodeCommentQueriesBean extends NamedQueriesBean<NamedCodeCommentQuery> {

	private static final long serialVersionUID = 1L;

	private List<NamedCodeCommentQuery> queries = new ArrayList<>();

	@Override
	@NotNull
	@Editable
	@OmitName
	public List<NamedCodeCommentQuery> getQueries() {
		return queries;
	}

	@Override
	public void setQueries(List<NamedCodeCommentQuery> queries) {
		this.queries = queries;
	}

}