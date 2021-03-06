package workflow.dao.filter;

import com.exponentus.scripting.WebFormData;
import reference.model.DocumentSubject;
import reference.model.DocumentType;
import staff.model.Organization;
import workflow.model.Outgoing;
import workflow.model.constants.ApprovalResultType;
import workflow.model.constants.ApprovalStatusType;

import java.util.UUID;

public class OutgoingFilter extends Outgoing {
    // ApprovalStatusType status
    // ApprovalResultType result
    // recipient
    // docType
    // docSubject

    public OutgoingFilter() {
    }

    public OutgoingFilter(WebFormData formData) {
        setStatus(null);
        setResult(null);

        if (formData.containsField("status")) {
            setStatus(ApprovalStatusType.valueOf(formData.getValueSilently("status")));
        }

        if (formData.containsField("result")) {
            setResult(ApprovalResultType.valueOf(formData.getValueSilently("result")));
        }

        if (formData.containsField("recipient")) {
            Organization recipient = new Organization();
            recipient.setId(UUID.fromString(formData.getValueSilently("recipient")));

            setRecipient(recipient);
        }

        if (formData.containsField("docType")) {
            DocumentType documentType = new DocumentType();
            documentType.setId(UUID.fromString(formData.getValueSilently("docType")));

            setDocType(documentType);
        }

        if (formData.containsField("docSubject")) {
            DocumentSubject documentSubject = new DocumentSubject();
            documentSubject.setId(UUID.fromString(formData.getValueSilently("docSubject")));

            setDocSubject(documentSubject);
        }
    }
}
