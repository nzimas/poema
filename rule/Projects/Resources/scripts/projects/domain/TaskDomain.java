package projects.domain;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import com.exponentus.common.model.ACL;
import com.exponentus.rest.outgoingdto.Outcome;

import administrator.model.User;
import helpdesk.model.Demand;
import projects.exception.TaskException;
import projects.model.Project;
import projects.model.Request;
import projects.model.Task;
import projects.model.constants.ResolutionType;
import projects.model.constants.TaskStatusType;
import reference.model.TaskType;

public class TaskDomain {

	public Task composeNew(User user, Project project, Task parentTask, Demand demand, TaskType taskType,
			boolean initiative, int dueDateRange) {
		Task task = new Task();

		task.setAuthor(user);
		task.setInitiative(initiative);
		task.setTaskType(taskType);
		task.setStatus(TaskStatusType.OPEN);
		task.setProject(project);
		if (demand != null) {
			task.setDemand(demand);
		} else if (task.getParent() != null) {
			//   task.setDemand(task.getParent().getDemand());
		}

		if (project == null && demand != null) {
			task.setProject(demand.getProject());
		}

		if (task.getProject() != null) {
			task.setAssignee(task.getProject().getProgrammer());
		}

		if (parentTask != null) {
			task.setParent(parentTask);
			task.setTitle(parentTask.getTitle());
			task.setPriority(parentTask.getPriority());
			task.setStartDate(parentTask.getStartDate());
			task.setDueDate(parentTask.getDueDate());
			task.setTags(parentTask.getTags());
			task.setObservers(parentTask.getObservers());
		} else {
			task.setStartDate(new Date());
			task.setDueDate(new LocalDate(task.getStartDate()).plusDays(dueDateRange).toDate());
			if (task.getProject() != null) {
				task.setObservers(task.getProject().getObservers());
			}
		}

		return task;
	}

	public void fillFromDto(Task task, Task dto) {
		if (task.isNew()) {
			task.setAuthor(dto.getAuthor());
			changeStatus(task, TaskStatusType.OPEN);
			task.setInitiative(dto.isInitiative());

			if (dto.getParent() != null) {
				task.setParent(dto.getParent());
				//	dto.setProject(dto.getParent().getProject());
				//	task.addReaders(dto.getParent().getReaders());
			}
		}

		task.setProject(dto.getProject());
		task.setDemand(dto.getDemand());

		String title = dto.getTitle();
		if (title == null || title.isEmpty()) {
			// TODO here it needed to vanish from markdown symbols
			title = StringUtils.abbreviate(dto.getBody(), 140);
		}
		task.setTitle(title);
		task.setTaskType(dto.getTaskType());
		task.setPriority(dto.getPriority());
		task.setBody(dto.getBody());
		task.setTags(dto.getTags());

		task.setStartDate(dto.getStartDate());
		task.setDueDate(dto.getDueDate());
		calculateStatus(task);

		User user = new User();
		user.setId(dto.getAssignee());
		changeAssignee(task, user);

		task.setCustomerObservation(dto.isCustomerObservation());
		task.setAttachments(dto.getAttachments());
		task.setObservers(dto.getObservers() != null ? dto.getObservers() : new ArrayList<>());

		calculateReaders(task);
	}

	public void changeStatus(Task task, TaskStatusType status) {
		task.setStatus(status);
		/*
		 * Если задача становится draft, то перестает быть видной для
		 * исполнителей и обсерверов. Т.е. Ситуация автор вдруг понял, что
		 * задание ещё не достаточно четко сформулировано или что-то изменилось
		 * в требованиях и т.д.
		 */
		if (status == TaskStatusType.DRAFT) {
			task.resetReadersEditors();
			task.addReaderEditor(task.getAuthor());
		} else {
			task.addReaderEditor(task.getAuthor());
		}
		task.setStatusDate(new Date());
	}

	public void calculateStatus(Task task) {
		if (task.getStartDate() == null) {
			changeStatus(task, TaskStatusType.DRAFT);
		} else {
			if (task.getStatus() == TaskStatusType.DRAFT || task.getStatus() == TaskStatusType.OPEN
					|| task.getStatus() == TaskStatusType.WAITING) {
				if (new Date().before(task.getStartDate())) {
					changeStatus(task, TaskStatusType.WAITING);
				} else {
					changeStatus(task, TaskStatusType.OPEN);
				}
			}
		}
	}

	public void changeAssignee(Task task, User newAssignee) {
		task.setAssignee(newAssignee.getId());

		// if (oldAssignee.longValue() != newAssignee.longValue()) {
		// // TODO notify about changes ?
		// }
	}

	public void calculateReaders(Task task) {
		if (task.getStatus() != TaskStatusType.DRAFT) {

			User assigneeUser = new User();
			assigneeUser.setId(task.getAssignee());

			task.addReader(assigneeUser);
			task.addReaders(task.getObservers());
		}

		if (task.getParent() != null) {
			//task.getParent().addReaders(task.getReaders());
		}
	}

	public void acknowledgedTask(Task task, User user) throws TaskException {
		if (!task.getAssignee().equals(user.getId())) {
			throw new TaskException("not_assignee_user");
		} else if (task.getStatus() != TaskStatusType.OPEN && task.getStatus() != TaskStatusType.WAITING) {
			throw new TaskException("task_status_is_not_open");
		}

		changeStatus(task, TaskStatusType.PROCESSING);
	}

	public void completeTask(Task task) throws TaskException {
		if (task.getStatus() == TaskStatusType.COMPLETED) {
			throw new TaskException("task already completed");
		}

		changeStatus(task, TaskStatusType.COMPLETED);
	}

	public void prolongTask(Task task, Date newDueDate) throws TaskException {
		if (newDueDate == null) {
			throw new TaskException("newDueDate is null");
		}

		if (task.getDueDate().after(newDueDate)) {
			throw new TaskException(
					"new due date '" + newDueDate + "' must be after current due date '" + task.getDueDate() + "'");
		}

		task.setDueDate(newDueDate);
		changeStatus(task, TaskStatusType.PROCESSING);
	}

	public void cancelTask(Task task, String comment) throws TaskException {
		if (task.getStatus() == TaskStatusType.CANCELLED) {
			throw new TaskException("task already cancelled");
		}

		changeStatus(task, TaskStatusType.CANCELLED);
		task.setCancellationComment(comment);
	}

	public void returnToProcessing(Task task) {
		changeStatus(task, TaskStatusType.PROCESSING);
	}

	public boolean taskIsEditable(Task task) {
		return task.isEditable();
	}

	public boolean taskCanBeDeleted(Task task) {
		return !task.isNew() && task.isEditable()
				&& (task.getStatus() == TaskStatusType.OPEN || task.getStatus() == TaskStatusType.DRAFT);
	}

	public boolean userCanDoAcknowledged(Task task, User user) {
		if (!task.isNew() && task.getAssignee().equals(user.getId())) {
			if (task.getStatus() == TaskStatusType.OPEN || task.getStatus() == TaskStatusType.WAITING) {
				return true;
			}
		}
		return false;
	}

	public boolean userCanDoRequest(Task task, User user) {
		if (!task.isNew() && task.getAssignee().equals(user.getId())) {
			if (task.getStatus() == TaskStatusType.PROCESSING) {
				return true;
			}
		}
		return false;
	}

	public boolean userCanDoResolution(Task task, User user) {
		if (!task.isNew()) {
			if (task.getAuthor().getId().equals(user.getId()) || user.isSuperUser()) {
				if (task.getStatus() != TaskStatusType.COMPLETED && task.getStatus() != TaskStatusType.CANCELLED) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean userCanAddSubTask(Task task, User user) {
		if (!task.isNew()) {
			if (task.getStatus() != TaskStatusType.COMPLETED && task.getStatus() != TaskStatusType.CANCELLED) {
				return true;
			}
		}
		return false;
	}

	public Outcome getOutcome(Task task) {
		Outcome outcome = new Outcome();

		if (task.isNew() && task.getParent() != null) {
			outcome.setTitle("new_subtask");
		} else if (task.isInitiative()) {
			outcome.setTitle("initiative_task");
		} else if (task.isNew()) {
			outcome.setTitle("new_task");
		} else if (task.getParent() != null) {
			outcome.setTitle("subtask");
		} else {
			outcome.setTitle("task");
		}

		outcome.addPayload(task);
		outcome.addPayload("parentTask", task.getParent());
		if (!task.isNew()) {
			outcome.addPayload(new ACL(task));
		}

		if (task.getRequests() != null && !task.getRequests().isEmpty()) {
			Request unresolvedRequest = task.getRequests().stream()
					.filter(it -> it.getResolution() != ResolutionType.ACCEPTED
							&& it.getResolution() != ResolutionType.DECLINED)
					.findFirst().orElse(null);
			if (unresolvedRequest != null) {
				outcome.addPayload("unresolvedRequest", unresolvedRequest);
			}
		}

		return outcome;
	}
}
