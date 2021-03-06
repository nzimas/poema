package workflow.model.embedded;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.exponentus.user.IUser;

import reference.model.constants.ApprovalSchemaType;
import staff.model.Employee;
import workflow.model.constants.ApprovalResultType;
import workflow.model.constants.ApprovalStatusType;

public interface IApproval {

	UUID getId();

	IUser<Long> getAuthor();

	String getURL();

	void addReader(IUser<Long> user);

	void addReaders(List<Long> readers);

	List<Block> getBlocks();

	Block getNextBlock();

	ApprovalResultType getResult();

	ApprovalSchemaType getSchema();

	ApprovalStatusType getStatus();

	List<Employee> getRecipients();

	boolean isVersionsSupport();

	void setVersionsSupport(boolean vs);

	int getVersion();

	void setBlocks(List<Block> blocks);

	void setEditors(Set<Long> set);

	void setResult(ApprovalResultType accepted);

	void setSchema(ApprovalSchemaType schema);

	void setStatus(ApprovalStatusType processing);

	void setVersion(int version);

	boolean userCanDoDecision(Employee emp);

}
