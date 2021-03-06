package io.onedev.server.search.commit;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.DateUtils;

public class AfterCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public AfterCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public void fill(Project project, RevListCommand command) {
		for (String value: values)
			command.after(value);
	}

	@Override
	public boolean matches(RefUpdated event, User user) {
		RevCommit commit = event.getProject().getRevCommit(event.getNewCommitId(), true);
		for (String value: values) {
			if (!commit.getCommitterIdent().getWhen().after(DateUtils.parseRelaxed(value)))
				return false;
		}
		return true;
	}

}
