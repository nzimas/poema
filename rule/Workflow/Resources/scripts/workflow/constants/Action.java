package workflow.constants;

import com.exponentus.scripting.actions._Action;
import com.exponentus.scripting.actions._ActionType;
import workflow.init.AppConst;

/**
 * Created by medin on 4/21/17.
 */
public class Action {

    public static final _Action newIncoming = new _Action(_ActionType.LINK).caption("incoming").url(AppConst.BASE_URL + "incomings/new");
    public static final _Action newOutgoing = new _Action(_ActionType.LINK).caption("outgoing").url(AppConst.BASE_URL + "outgoings/new");
    public static final _Action newOfficeMemo = new _Action(_ActionType.LINK).caption("office_memo").url(AppConst.BASE_URL + "office-memos/new");

    public static final _Action startApproving = new _Action(_ActionType.API_ACTION).id("startApproving").caption("start_approving").url("startApproving");
    public static final _Action signApprovalBlock = new _Action(_ActionType.API_ACTION).id("acceptApprovalBlock").caption("sign").url("acceptApprovalBlock");
    public static final _Action acceptApprovalBlock = new _Action(_ActionType.API_ACTION).id("acceptApprovalBlock").caption("accept").url("acceptApprovalBlock");
    public static final _Action declineApprovalBlock = new _Action(_ActionType.API_ACTION).id("declineApprovalBlock").caption("decline").url("declineApprovalBlock");

    public static final _Action refreshVew = new _Action(_ActionType.RELOAD).id("refresh").icon("fa fa-refresh");
    public static final _Action close = new _Action(_ActionType.CLOSE).caption("close").icon("fa fa-chevron-left").cls("btn-back");
    public static final _Action saveAndClose = new _Action(_ActionType.SAVE_AND_CLOSE).caption("save_close").cls("btn-primary");
    public static final _Action deleteDocument = new _Action(_ActionType.DELETE_DOCUMENT).caption("delete").cls("btn-warning-effect");
}
