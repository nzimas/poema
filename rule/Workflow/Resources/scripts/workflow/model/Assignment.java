package workflow.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.exponentus.common.model.SecureHierarchicalEntity;
import com.exponentus.dataengine.jpadatabase.ftengine.FTSearchable;
import com.exponentus.runtimeobj.IAppEntity;
import com.exponentus.scripting._Session;
import com.exponentus.user.IUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

import reference.model.ControlType;
import reference.model.Tag;
import staff.model.Employee;
import staff.model.embedded.Observer;
import workflow.init.AppConst;
import workflow.model.constants.ControlStatusType;
import workflow.model.constants.converter.ControlStatusTypeConverter;
import workflow.model.embedded.AssigneeEntry;

@JsonRootName("assignment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "wf__assignments")
public class Assignment extends SecureHierarchicalEntity {
	@JsonIgnore
	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	@OrderBy("appliedRegDate")
	private List<Report> reports;

	@JoinColumn(name = "applied_author", nullable = false)
	private Employee appliedAuthor;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "applied_reg_date")
	private Date appliedRegDate;

	@ManyToOne(optional = false)
	private ActionableDocument parent;

	@FTSearchable
	@Column(columnDefinition = "TEXT")
	private String body;

	@ElementCollection
	@CollectionTable(name = "wf__assignment_observers", joinColumns = @JoinColumn(referencedColumnName = "id"))
	private List<Observer> observers = new ArrayList<Observer>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "wf__assignment_tags")
	private List<Tag> tags;

	@Transient
	private List<IAppEntity<UUID>> responses;

	@JoinColumn(name = "control_type", nullable = false)
	private ControlType controlType;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date")
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "due_date")
	private Date dueDate;

	@Convert(converter = ControlStatusTypeConverter.class)
	private ControlStatusType status = ControlStatusType.DRAFT;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "status_time")
	private Date statusTime;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "wf__assignee_entries", uniqueConstraints = {
			@UniqueConstraint(columnNames = { "assignment_id", "sort" }),
			@UniqueConstraint(columnNames = { "assignment_id", "assignee" }) })
	@OrderBy("sort")
	private List<AssigneeEntry> assigneeEntries;

	public Employee getAppliedAuthor() {
		return appliedAuthor;
	}

	public void setAppliedAuthor(Employee appliedAuthor) {
		this.appliedAuthor = appliedAuthor;
	}

	public Date getAppliedRegDate() {
		return appliedRegDate;
	}

	public void setAppliedRegDate(Date appliedRegDate) {
		this.appliedRegDate = appliedRegDate;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<Observer> getObservers() {
		return observers;
	}

	public void setObservers(List<Observer> observers) {
		this.observers = observers;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public List<Report> getReports() {
		return reports;
	}

	public ActionableDocument getParent() {
		return parent;
	}

	public void setParent(ActionableDocument parent) {
		this.parent = parent;
	}

	@Override
	public String getURL() {
		return AppConst.BASE_URL + "assignments/" + getIdentifier();
	}

	@Override
	public SecureHierarchicalEntity getParentEntity(_Session ses) {
		return parent;
	}

	public List<IAppEntity<UUID>> getResponses() {
		return responses;
	}

	public void setResponses(List<IAppEntity<UUID>> responses) {
		this.responses = responses;
	}

	public ControlType getControlType() {
		return controlType;
	}

	public void setControlType(ControlType controlType) {
		this.controlType = controlType;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public ControlStatusType getStatus() {
		return status;
	}

	public void setStatus(ControlStatusType status) {
		this.status = status;
	}

	public Date getStatusTime() {
		return statusTime;
	}

	public void setStatusTime(Date statusTime) {
		this.statusTime = statusTime;
	}

	public List<AssigneeEntry> getAssigneeEntries() {
		return assigneeEntries;
	}

	public void setAssigneeEntries(List<AssigneeEntry> assigneeEntries) {
		this.assigneeEntries = assigneeEntries;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	public boolean assigneesContainsUser(IUser<Long> user) {
		if (this.getAssigneeEntries() == null) {
			return false;
		}

		for (AssigneeEntry ae : this.getAssigneeEntries()) {
			if (ae.getAssignee().getUser().getId().equals(user.getId())) {
				return true;
			}
		}

		return false;
	}
}
