package dk.brics.jwig.boost.rendering.renderables.configureUsers;

import dk.brics.jwig.boost.model.IUser;
import dk.brics.jwig.boost.rendering.uicomponents.tablewriter.DefaultTableFeature;
import dk.brics.xact.XML;

public class IUserNameFeature extends DefaultTableFeature<IUser> {
    @Override
    public XML getCaption() {
        return XML.parseTemplate("<[s_Name]>");
    }

    @Override
    public Object getLine(IUser user) {
        return XML.parseTemplate("<[NAME]>").plug("NAME", user.getName());
    }
}