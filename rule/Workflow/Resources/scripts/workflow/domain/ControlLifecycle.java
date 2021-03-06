package workflow.domain;

import java.util.Date;
import java.util.List;

import com.exponentus.env.EnvConst;

import reference.model.constants.ControlSchemaType;
import workflow.model.Assignment;
import workflow.model.Report;
import workflow.model.constants.ControlStatusType;
import workflow.model.embedded.AssigneeEntry;

public class ControlLifecycle {
	private Assignment entity;
	private Date current = new Date();

	public ControlLifecycle(Assignment approval) {
		this.entity = approval;
	}

	public void check() {
		if (entity.getControlType().getSchema() == ControlSchemaType.ALLOW_RESET_ON_BASIS_REPORT) {
			List<Report> reports = entity.getReports();
			List<AssigneeEntry> assigneeEntries = entity.getAssigneeEntries();
			int aeCount = assigneeEntries.size();
			int aCount = 0;
			for (AssigneeEntry assignee : assigneeEntries) {
				for (Report report : reports) {
					if (assignee.getAssignee().equals(report.getAppliedAuthor())
							&& assignee.getStatus() != ControlStatusType.COMPLETED) {
						assignee.setResetTime(current);
						assignee.setStatus(ControlStatusType.COMPLETED);
						assignee.setResetInfo(EnvConst.APP_ID);
						aCount++;
					} else if (assignee.getResetTime() != null) {
						aCount++;
					}
				}
			}
			if (aeCount == aCount) {
				entity.setStatus(ControlStatusType.COMPLETED);
			}
		} else if (entity.getControlType().getSchema() == ControlSchemaType.ALLOW_RESET_ON_BASIS_COORDINATOR_REPORT) {
			List<Report> reports = entity.getReports();
			AssigneeEntry coordinator = null;
			List<AssigneeEntry> assigneeEntries = entity.getAssigneeEntries();
			for (AssigneeEntry assignee : assigneeEntries) {
				if (assignee.isCoordinator()) {
					coordinator = assignee;
				}
			}
			if (coordinator != null) {
				for (Report report : reports) {
					if (coordinator.equals(report.getAppliedAuthor())) {
						assigneeEntries.stream().peek((element) -> {
							element.setResetTime(current);
							element.setStatus(ControlStatusType.COMPLETED);
							element.setResetInfo(EnvConst.APP_ID);
						});
						entity.setStatus(ControlStatusType.COMPLETED);
					}
				}
			}
		} else if (entity.getControlType().getSchema() == ControlSchemaType.RESET_ALL_MANULALLY) {
			List<Report> reports = entity.getReports();
			List<AssigneeEntry> assigneeEntries = entity.getAssigneeEntries();
			for (AssigneeEntry assignee : assigneeEntries) {
				for (Report report : reports) {
					if (assignee.getAssignee().equals(report.getAppliedAuthor())
							&& assignee.getStatus() != ControlStatusType.COMPLETED) {
						assignee.setResetTime(current);
						assignee.setStatus(ControlStatusType.PENDING);
						assignee.setResetInfo(EnvConst.APP_ID);
					}
				}
			}
		}
	}

}
