package workflow.services;

import com.exponentus.dataengine.jpa.ViewPage;
import com.exponentus.exception.SecureException;
import com.exponentus.rest.RestProvider;
import com.exponentus.rest.ServiceDescriptor;
import com.exponentus.rest.ServiceMethod;
import com.exponentus.rest.outgoingpojo.Outcome;
import com.exponentus.scripting._ColumnOptions;
import com.exponentus.scripting._Session;
import com.exponentus.scripting._WebFormData;
import workflow.dao.OfficeMemoDAO;
import workflow.model.OfficeMemo;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("office-memos")
public class OfficeMemoService extends RestProvider {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getView(@Context UriInfo uriInfo) {
        Map<String, Object> payload = new HashMap<>();

        _Session session = getSession();
        _WebFormData formData = new _WebFormData(uriInfo.getQueryParameters(), httpRequest.getHeader("referer"));
        int pageSize = session.pageSize;
        int pageNum = formData.getNumberValueSilently("page", 0);

        OfficeMemoDAO dao = new OfficeMemoDAO(session);
        List<OfficeMemo> list = dao.findAll(pageNum, pageSize);
        ViewPage vp = new ViewPage<>(list, list.size(), pageSize, pageNum);

        // column options
        _ColumnOptions colOpts = new _ColumnOptions();
        colOpts.add("reg_number", "regNumber", "text", "both", "vw-reg-number");
        colOpts.add("", "attachment", "icon", "", "vw-icon");
        colOpts.add("applied_reg_date", "appliedRegDate", "date", "both", "vw-date");
        colOpts.add("approval", "approval", "localizedName", "both", "vw-name");
        colOpts.add("content", "content", "text", "both", "vw-content");
        colOpts.add("summary", "summary", "text", "", "vw-summary");

        payload.put("columnOptions", colOpts);
        payload.put("officememos", vp);

        Outcome outcome = new Outcome();
        outcome.setPayload(payload);

        return Response.ok(outcome).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") String id) {
        _Session ses = getSession();
        OfficeMemoDAO dao = new OfficeMemoDAO(ses);
        OfficeMemo entity = dao.findById(id);
        return Response.ok(new ViewPage<>(entity)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(OfficeMemo officeMemo) {
        _Session ses = getSession();
        OfficeMemoDAO dao = new OfficeMemoDAO(ses);
        OfficeMemo entity;
        try {
            entity = dao.add(officeMemo);
        } catch (SecureException e) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        return Response.ok(new ViewPage<>(entity)).build();
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id, OfficeMemo officeMemo) {
        _Session ses = getSession();
        OfficeMemoDAO dao = new OfficeMemoDAO(ses);
        OfficeMemo entity;
        try {
            entity = dao.update(officeMemo);
        } catch (SecureException e) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        return Response.ok(new ViewPage<>(entity)).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        _Session ses = getSession();
        OfficeMemoDAO dao = new OfficeMemoDAO(ses);
        OfficeMemo entity = dao.findById(id);
        if (entity != null) {
            try {
                dao.delete(entity);
            } catch (SecureException e) {
                return Response.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
        }
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/action1")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doAction(@PathParam("id") String id) {

        System.out.println(id);

        return Response.noContent().build();
    }

    @Override
    public ServiceDescriptor updateDescription(ServiceDescriptor sd) {
        sd.setName(getClass().getName());
        ServiceMethod m = new ServiceMethod();
        m.setMethod(HttpMethod.GET);
        m.setURL("/" + sd.getAppName() + sd.getUrlMapping());
        sd.addMethod(m);
        return sd;
    }
}
