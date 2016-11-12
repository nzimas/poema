package workflow.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * @author Kayra created 07-04-2016
 */

import com.exponentus.common.model.Attachment;
import com.exponentus.dataengine.jpa.SecureAppEntity;
import com.exponentus.dataengine.jpadatabase.ftengine.FTSearchable;
import com.fasterxml.jackson.annotation.JsonRootName;

import workflow.model.embedded.Approval;

@JsonRootName("officeMemo")
@Entity
@Table(name = "office_memos")
@NamedQuery(name = "OfficeMemo.findAll", query = "SELECT m FROM OfficeMemo AS m ORDER BY m.regDate")
public class OfficeMemo extends SecureAppEntity<UUID> {
	
	@Column(name = "reg_number")
	private String regNumber;
	
	@Column(name = "applied_reg_date")
	private Date appliedRegDate;
	
	@Column(name = "applied_author", nullable = false, updatable = true)
	protected Long appliedAuthor;

	protected Long recipient;

	@Embedded
	private Approval approval;
	
	@FTSearchable
	@Column(length = 140)
	private String title;
	
	@FTSearchable
	@Column(columnDefinition = "TEXT")
	private String body;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable(name = "office_memo_attachments", joinColumns = {
			@JoinColumn(name = "office_memo_id") }, inverseJoinColumns = {
					@JoinColumn(name = "attachment_id") }, indexes = {
							@Index(columnList = "office_memo_id, attachment_id") }, uniqueConstraints = @UniqueConstraint(columnNames = {
									"office_memo_id", "attachment_id" }))
	@CascadeOnDelete
	private List<Attachment> attachments = new ArrayList<>();
	
	public String getRegNumber() {
		return regNumber;
	}
	
	public void setRegNumber(String regNumber) {
		this.regNumber = regNumber;
	}
	
	public Date getAppliedRegDate() {
		return appliedRegDate;
	}
	
	public void setAppliedRegDate(Date appliedRegDate) {
		this.appliedRegDate = appliedRegDate;
	}
	
	public Long getAppliedAuthor() {
		return appliedAuthor;
	}

	public void setAppliedAuthor(Long appliedAuthor) {
		this.appliedAuthor = appliedAuthor;
	}

	public long getRecipient() {
		return recipient;
	}
	
	public void setRecipient(long recipient) {
		this.recipient = recipient;
	}
	
	public Approval getApproval() {
		return approval;
	}
	
	public void setApproval(Approval approval) {
		this.approval = approval;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	@Override
	public List<Attachment> getAttachments() {
		return attachments;
	}
	
	@Override
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	
	@Override
	public String getURL() {
		return "office-memos/" + getIdentifier();
	}
}
