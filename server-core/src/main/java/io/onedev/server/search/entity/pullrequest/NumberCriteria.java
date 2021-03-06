package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.PullRequestConstants;

public class NumberCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final long value;
	
	public NumberCriteria(long value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<Long> attribute = root.get(PullRequestConstants.ATTR_NUMBER);
		if (operator == PullRequestQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else
			return builder.lessThan(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		if (operator == PullRequestQueryLexer.Is)
			return request.getNumber() == value;
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return request.getNumber() > value;
		else
			return request.getNumber() < value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestConstants.FIELD_NUMBER) + " " + PullRequestQuery.getRuleName(operator) + " " + PullRequestQuery.quote(String.valueOf(value));
	}

}
