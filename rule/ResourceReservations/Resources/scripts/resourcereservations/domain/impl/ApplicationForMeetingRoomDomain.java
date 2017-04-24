package resourcereservations.domain.impl;

import com.exponentus.common.model.ACL;
import com.exponentus.rest.outgoingdto.Outcome;
import com.exponentus.user.IUser;
import reference.model.constants.ApprovalType;
import resourcereservations.domain.IApplicationForMeetingRoomDomain;
import resourcereservations.model.ApplicationForMeetingRoom;
import staff.model.Employee;
import workflow.exception.ApprovalException;
import workflow.model.constants.ApprovalStatusType;
import workflow.model.embedded.Block;
import workflow.model.util.ApprovalLifecycle;

import java.util.ArrayList;
import java.util.Date;

public class ApplicationForMeetingRoomDomain implements IApplicationForMeetingRoomDomain {

    @Override
    public ApplicationForMeetingRoom composeNew(Employee author) {
        ApplicationForMeetingRoom entity = new ApplicationForMeetingRoom();

        entity.setAuthor(author.getUser());
        entity.setAppliedRegDate(new Date());
        entity.setAppliedAuthor(author);
        entity.setRecipient(author);

        ArrayList<Block> blocks = new ArrayList();
        Block block = new Block();
        block.setType(ApprovalType.SIGNING);
        block.setPosition(1);
        block.setStatus(ApprovalStatusType.DRAFT);
        block.setApprovers(new ArrayList<>());
        blocks.add(block);
        entity.setBlocks(blocks);

        return entity;
    }

    @Override
    public void fillFromDto(ApplicationForMeetingRoom entity, ApplicationForMeetingRoom dto, Employee author) {
        if (entity.isNew()) {
            entity.setAuthor(author.getUser());

            entity.addReaderEditor(entity.getAuthor());
            if (dto.getAppliedAuthor() != null) {
                entity.addReaderEditor(dto.getAppliedAuthor().getUser());
            }
        }

        entity.setTitle(dto.getTitle());
        entity.setBody(dto.getBody());
        entity.setAppliedAuthor(dto.getAppliedAuthor());
        entity.setAppliedRegDate(dto.getAppliedRegDate());
        entity.setRecipient(dto.getRecipient());
        //entity.setR(dto.getVehicle());
        entity.setUseFrom(dto.getUseFrom());
        entity.setUseTo(dto.getUseTo());
        entity.setTags(dto.getTags());
        entity.setAttachments(dto.getAttachments());
        entity.setBlocks(dto.getBlocks());
        entity.setSchema(dto.getSchema());
        entity.setObservers(dto.getObservers());

        if (entity.isNew()) {
            dto.setVersion(1);
        }
    }

    @Override
    public boolean approvalCanBeStarted(ApplicationForMeetingRoom entity) {
        return entity.getStatus() == ApprovalStatusType.DRAFT;
    }

    @Override
    public void startApproving(ApplicationForMeetingRoom entity) throws ApprovalException {
        ApprovalLifecycle lifecycle = new ApprovalLifecycle(entity);
        lifecycle.start();
    }

    @Override
    public boolean employeeCanDoDecisionApproval(ApplicationForMeetingRoom entity, Employee employee) {
        return entity.userCanDoDecision(employee);
    }

    @Override
    public void acceptApprovalBlock(ApplicationForMeetingRoom entity, IUser<Long> user) throws ApprovalException {
        ApprovalLifecycle lifecycle = new ApprovalLifecycle(entity);
        lifecycle.accept(user);
    }

    @Override
    public void declineApprovalBlock(ApplicationForMeetingRoom entity, IUser<Long> user, String decisionComment) throws ApprovalException {
        ApprovalLifecycle lifecycle = new ApprovalLifecycle(entity);
        lifecycle.decline(user, decisionComment);
    }

    @Override
    public Outcome getOutcome(ApplicationForMeetingRoom entity) {
        Outcome outcome = new Outcome();

        outcome.setTitle(entity.getTitle());
        outcome.addPayload(entity.getEntityKind(), entity);
        if (!entity.isNew()) {
            outcome.addPayload(new ACL(entity));
        }

        return outcome;
    }
}
