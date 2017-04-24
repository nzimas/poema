package workflow.model;

import com.exponentus.common.model.Attachment;
import com.exponentus.common.model.SecureHierarchicalEntity;
import com.exponentus.dataengine.jpadatabase.ftengine.FTSearchable;
import com.exponentus.scripting._Session;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import staff.model.Employee;
import staff.model.embedded.Observer;
import workflow.init.AppConst;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonRootName("report")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "wf__reports")
public class Report extends SecureHierarchicalEntity {

    @JoinColumn(name = "applied_author", nullable = false)
    private Employee appliedAuthor;

    @Column(name = "applied_reg_date")
    private Date appliedRegDate;

    @JsonIgnore
    @NotNull
    @ManyToOne
    private Assignment parent;

    @FTSearchable
    @Column(columnDefinition = "TEXT")
    private String body;

    @ElementCollection
    @CollectionTable(name = "wf__report_observers", joinColumns = @JoinColumn(referencedColumnName = "id"))
    private List<Observer> observers;

    @JoinTable(name = "wf__report_attachments", joinColumns = {
            @JoinColumn(name = "report_id")}, inverseJoinColumns = {@JoinColumn(name = "attachment_id")}, indexes = {
            @Index(columnList = "report_id, attachment_id")}, uniqueConstraints = @UniqueConstraint(columnNames = {
            "report_id", "attachment_id"}))
    @CascadeOnDelete
    private List<Attachment> attachments = new ArrayList<>();

    public Report() {
    }

    // test
    public Report(UUID id, Date regDate, String title, String body, Employee appliedAuthor, Date appliedRegDate,
                  Long countAtt) {
        this.id = id;
        this.regDate = regDate;
        this.title = title;
        this.body = body;
        this.appliedAuthor = appliedAuthor;
        this.appliedRegDate = appliedRegDate;
        if (countAtt > 0) {
            this.attachments = new ArrayList<>();
            for (int i = 0; i < countAtt; i++) {
                this.attachments.add(new Attachment());
            }
        }
    }

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

    public Assignment getParent() {
        return parent;
    }

    @JsonProperty
    public void setParent(Assignment parent) {
        this.parent = parent;
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
        return AppConst.BASE_URL + "reports/" + getIdentifier();
    }

    @Override
    public SecureHierarchicalEntity getParentEntity(_Session ses) {
        return parent;
    }
}
