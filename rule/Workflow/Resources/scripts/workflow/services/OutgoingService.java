package workflow.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.exponentus.common.domain.IValidation;
import com.exponentus.dataengine.exception.DAOException;
import com.exponentus.dataengine.jpa.ViewPage;
import com.exponentus.env.EnvConst;
import com.exponentus.exception.SecureException;
import com.exponentus.rest.RestProvider;
import com.exponentus.rest.outgoingdto.Outcome;
import com.exponentus.rest.validation.exception.DTOException;
import com.exponentus.runtimeobj.RegNum;
import com.exponentus.scripting.SortParams;
import com.exponentus.scripting._Session;
import com.exponentus.scripting.actions._ActionBar;

import administrator.model.User;
import reference.model.constants.ApprovalType;
import staff.dao.EmployeeDAO;
import staff.model.Employee;
import workflow.dao.OutgoingDAO;
import workflow.dao.filter.OutgoingFilter;
import workflow.domain.ApprovalLifecycle;
import workflow.domain.OutgoingDomain;
import workflow.domain.exception.ApprovalException;
import workflow.dto.action.DeclineApprovalBlockAction;
import workflow.model.Outgoing;
import workflow.model.embedded.Approver;
import workflow.model.embedded.Block;
import workflow.other.Messages;
import workflow.ui.ActionFactory;

@Path("outgoings")
@Produces(MediaType.APPLICATION_JSON)
public class OutgoingService extends RestProvider {

	private ActionFactory action = new ActionFactory();

	@GET
	public Response getView() {
		_Session session = getSession();
		int pageSize = session.getPageSize();
		SortParams sortParams = getWebFormData().getSortParams(SortParams.desc("regDate"));
		OutgoingFilter filter = new OutgoingFilter(getWebFormData());

		try {
			OutgoingDAO dao = new OutgoingDAO(session);
			ViewPage<Outgoing> vp = dao.findViewPage(filter, sortParams, getWebFormData().getPage(), pageSize);

			_ActionBar actionBar = new _ActionBar(session);
			actionBar.addAction(action.newOutgoing);
			actionBar.addAction(action.refreshVew);

			Outcome outcome = new Outcome();
			outcome.setId("outgoings");
			outcome.setTitle("outgoing_documents");
			outcome.addPayload(actionBar);
			outcome.addPayload(vp);

			return Response.ok(outcome).build();
		} catch (DAOException e) {
			return responseException(e);
		}
	}

	@GET
	@Path("{id}")
	public Response getById(@PathParam("id") String id) {
		_Session ses = getSession();
		Outgoing entity;
		OutgoingDomain outDomain = new OutgoingDomain(ses);
		try {
			boolean isNew = "new".equals(id);
			if (isNew) {
				entity = outDomain.composeNew((User) ses.getUser());
			} else {
				OutgoingDAO outgoingDAO = new OutgoingDAO(ses);
				entity = outgoingDAO.findByIdentefier(id);
			}

			EmployeeDAO empDao = new EmployeeDAO(ses);
			Map<Long, Employee> emps = empDao.findAll(false).getResult().stream()
					.collect(Collectors.toMap(Employee::getUserID, Function.identity(), (e1, e2) -> e1));

			Outcome outcome = outDomain.getOutcome(entity);
			outcome.addPayload("employees", emps);
			outcome.addPayload(getActionBar(ses, entity, outDomain));
			outcome.addPayload(EnvConst.FSID_FIELD_NAME, getWebFormData().getFormSesId());

			return Response.ok(outcome).build();
		} catch (DAOException e) {
			return responseException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response add(Outgoing dto) {
		dto.setId(null);
		return saveRequest(dto);
	}

	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") String id, Outgoing dto) {
		dto.setId(UUID.fromString(id));
		return saveRequest(dto);
	}

	private Response saveRequest(Outgoing dto) {
		try {
			OutgoingDomain domain = new OutgoingDomain(getSession());
			Outcome outcome = domain.getOutcome(save(dto, new ValidationToSaveAsDraft()));

			return Response.ok(outcome).build();
		} catch (DTOException e) {
			return responseValidationError(e);
		} catch (DAOException | SecureException e) {
			return responseException(e);
		}
	}

	private Outgoing save(Outgoing dto, IValidation<Outgoing> validation)
			throws SecureException, DAOException, DTOException {
		_Session ses = getSession();

		OutgoingDAO dao = new OutgoingDAO(ses);
		Outgoing entity;

		if (dto.isNew()) {
			entity = new Outgoing();
		} else {
			entity = dao.findById(dto.getId());
		}

		new OutgoingDomain(getSession()).fillFromDto(entity, dto, validation, getWebFormData().getFormSesId());

		if (dto.isNew()) {
			RegNum rn = new RegNum();
			entity.setRegNumber(Integer.toString(rn.getRegNumber(entity.getDefaultFormName())));
			entity = dao.add(entity, rn);
		} else {
			entity = dao.update(entity);
		}

		return entity;
	}

	@DELETE
	@Path("{id}")
	public Response delete(@PathParam("id") String id) {
		_Session ses = getSession();
		try {
			OutgoingDAO dao = new OutgoingDAO(ses);
			Outgoing entity = dao.findByIdentefier(id);
			if (entity != null) {
				dao.delete(entity);
			}
			return Response.noContent().build();
		} catch (DAOException | SecureException e) {
			return responseException(e);
		}
	}

	@Override
	@GET
	@Path("{id}/attachments/{attachId}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getAttachment(@PathParam("id") String id, @PathParam("attachId") String attachId) {
		try {
			OutgoingDAO dao = new OutgoingDAO(getSession());
			Outgoing entity = dao.findByIdentefier(id);

			return getAttachment(entity, attachId);
		} catch (DAOException e) {
			return responseException(e);
		}
	}

	@GET
	@Path("{id}/attachments/{attachId}/{fileName}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getAttachmentFN(@PathParam("id") String id, @PathParam("attachId") String attachId) {
		return getAttachment(id, attachId);
	}

	@POST
	@Path("action/startApproving")
	public Response startApproving(Outgoing dto) {

		Outgoing entity = null;
		_Session ses = getSession();
		try {
			UUID id = dto.getId();
			if (dto.isNew()) {
				id = save(dto, new ValidationToStartApprove()).getId();
			}
			OutgoingDAO dao = new OutgoingDAO(ses);
			entity = dao.findById(id);
			OutgoingDomain omd = new OutgoingDomain(ses);
			omd.startApproving(entity);
			dao.update(entity, false);

			new Messages(getAppEnv()).notifyApprovers(entity, entity.getTitle());
			Outcome outcome = omd.getOutcome(entity);
			outcome.setTitle("approving_started");
			outcome.setMessage("approving_started");
			outcome.addPayload("result", "approving_started");

			return Response.ok(outcome).build();

		} catch (DTOException e) {
			return responseValidationError(e);
		} catch (DAOException | SecureException | ApprovalException e) {
			return responseException(e);
		}
	}

	@POST
	@Path("action/acceptApprovalBlock")
	public Response acceptApprovalBlock(Outgoing dto) {
		try {
			_Session ses = getSession();
			OutgoingDAO dao = new OutgoingDAO(ses);
			Outgoing entity = dao.findById(dto.getId());
			OutgoingDomain domain = new OutgoingDomain(ses);

			domain.acceptApprovalBlock(entity, ses.getUser());

			dao.update(entity, false);

			new Messages(getAppEnv()).notifyApprovers(entity, entity.getTitle());
			Outcome outcome = domain.getOutcome(entity);
			outcome.setTitle("acceptApprovalBlock");
			outcome.setMessage("acceptApprovalBlock");

			return Response.ok(outcome).build();
		} catch (DAOException | SecureException | ApprovalException e) {
			return responseException(e);
		}
	}

	@POST
	@Path("action/declineApprovalBlock")
	public Response declineApprovalBlock(DeclineApprovalBlockAction<Outgoing> actionDto) {
		try {
			_Session ses = getSession();
			OutgoingDAO dao = new OutgoingDAO(ses);
			Outgoing entity = dao.findById(actionDto.getModel().getId());
			OutgoingDomain domain = new OutgoingDomain(ses);

			domain.declineApprovalBlock(entity, ses.getUser(), actionDto.getComment());

			dao.update(entity, false);
			new Messages(getAppEnv()).notifyApprovers(entity, entity.getTitle());
			Outcome outcome = domain.getOutcome(entity);
			outcome.setTitle("declineApprovalBlock");
			outcome.setMessage("declineApprovalBlock");

			return Response.ok(outcome).build();
		} catch (DAOException | SecureException | ApprovalException e) {
			return responseException(e);
		}

	}

	private _ActionBar getActionBar(_Session session, Outgoing entity, OutgoingDomain outDomain) throws DAOException {
		_ActionBar actionBar = new _ActionBar(session);

		actionBar.addAction(action.close);
		if (entity.isEditable()) {
			actionBar.addAction(action.saveAndClose);
		}
		if (outDomain.approvalCanBeStarted(entity)) {
			actionBar.addAction(action.startApproving);
		}

		EmployeeDAO employeeDAO = new EmployeeDAO(session);

		if (outDomain.employeeCanDoDecisionApproval(entity, employeeDAO.findByUser(session.getUser()))) {
			if (ApprovalLifecycle.getProcessingBlock(entity).getType() == ApprovalType.SIGNING) {
				actionBar.addAction(action.signApprovalBlock);
			} else {
				actionBar.addAction(action.acceptApprovalBlock);
			}
			actionBar.addAction(action.declineApprovalBlock);
		}
		if (!entity.isNew() && entity.isEditable()) {
			actionBar.addAction(action.deleteDocument);
		}

		return actionBar;
	}

	private class ValidationToSaveAsDraft implements IValidation<Outgoing> {

		@Override
		public void check(Outgoing dto) throws DTOException {
			DTOException e = new DTOException();

			if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
				e.addError("title", "required", "field_is_empty");
			}
			if (dto.getRecipient() == null) {
				e.addError("recipient", "required", "field_is_empty");
			}
			if (dto.getDocSubject() == null) {
				e.addError("docSubject", "required", "field_is_empty");
			}
			if (dto.getDocLanguage() == null) {
				e.addError("docLanguage", "required", "field_is_empty");
			}
			if (dto.getDocType() == null) {
				e.addError("docType", "required", "field_is_empty");
			}
			if (e.hasError()) {
				throw e;
			}

		}

	}

	private class ValidationToStartApprove extends ValidationToSaveAsDraft {

		@Override
		public void check(Outgoing dto) throws DTOException {
			super.check(dto);
			DTOException e = new DTOException();

			List<Block> blocks = dto.getBlocks();
			if (blocks.size() == 0) {
				e.addError("blocks", "required", "field_is_empty");
			} else {
				List<Approver> approvers = blocks.get(0).getApprovers();
				if (approvers == null || approvers.size() == 0) {
					e.addError("approvers", "required", "field_is_empty");
				}
			}

			if (e.hasError()) {
				throw e;
			}

		}

	}
}
