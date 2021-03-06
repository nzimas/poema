package helpdesk.domain;

import administrator.model.User;
import com.exponentus.common.model.ACL;
import com.exponentus.rest.outgoingdto.Outcome;
import com.exponentus.rest.validation.exception.DTOException;
import helpdesk.model.Demand;
import helpdesk.model.constants.DemandStatusType;
import reference.model.DemandType;

import java.util.Date;

public class DemandDomain {

    public Demand composeNew(User user, DemandType demandType) {
        Demand demand = new Demand();

        demand.setAuthor(user);
        demand.setTitle("");
        demand.setBody("");
        demand.setStatus(DemandStatusType.DRAFT);
        demand.setDemandType(demandType);

        return demand;
    }

    public void fillFromDto(Demand demand, Demand dto, User user) throws DTOException {
        validate(dto);

        demand.setStatus(dto.getStatus());
        demand.setStatusDate(dto.getStatusDate());
        demand.setTitle(dto.getTitle());
        demand.setBody(dto.getBody());
        demand.setTags(dto.getTags());
        demand.setAttachments(dto.getAttachments());
        demand.setDemandType(dto.getDemandType());
        demand.setCustomer(dto.getCustomer());
        demand.setProject(dto.getProject());
        demand.setAttachments(dto.getAttachments());

        if (demand.isNew()) {
            demand.setAuthor(user);
            demand.addReaderEditor(demand.getAuthor());
        }
    }

    public void changeStatus(Demand demand, DemandStatusType status) {
        // DRAFT(566), PROCESSING(567), COMPLETED(568), CANCELLED(569),
        // OPEN(570);
        demand.setStatus(status);
        demand.setStatusDate(new Date());
    }

    public void registerTask(Demand demand) {

    }

    private void validate(Demand demand) throws DTOException {
        DTOException ve = new DTOException();

        if (demand.getTitle() == null || demand.getTitle().isEmpty()) {
            ve.addError("title", "required", "field_is_empty");
        }
        if (demand.getDemandType() == null) {
            ve.addError("demandType", "required", "field_is_empty");
        }

        if (ve.hasError()) {
            throw ve;
        }
    }

    public Outcome getOutcome(Demand demand) {
        Outcome outcome = new Outcome();

        outcome.setTitle(demand.getTitle());
        outcome.addPayload(demand);

        if (!demand.isNew()) {
            outcome.addPayload(new ACL(demand));
        }

        return outcome;
    }
}
