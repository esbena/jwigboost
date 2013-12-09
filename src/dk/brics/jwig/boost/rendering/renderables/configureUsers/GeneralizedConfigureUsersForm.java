package dk.brics.jwig.boost.rendering.renderables.configureUsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import dk.brics.jwig.persistence.HibernateQuerier;
import org.hibernate.Session;
import org.hibernate.Transaction;

import dk.brics.jwig.HiddenPoster;
import dk.brics.jwig.WebContext;
import dk.brics.jwig.boost.TextUtil;
import dk.brics.jwig.boost.model.IUser;
import dk.brics.jwig.boost.model.IUserRole;
import dk.brics.jwig.boost.rendering.hierarchy.Content;
import dk.brics.jwig.boost.rendering.hierarchy.Titled;
import dk.brics.jwig.boost.rendering.uicomponents.tablewriter.DefaultTableFeature;
import dk.brics.jwig.boost.rendering.uicomponents.tablewriter.TableFeature;
import dk.brics.jwig.boost.rendering.uicomponents.tablewriter.TableWriter;
import dk.brics.xact.XML;

/**
 * A form for configuring the roles of all users of the system.
 */
public class GeneralizedConfigureUsersForm implements Titled, Content {

    public static interface SubtypeDecider {
        public boolean decide(IUserRole role);
    }

    /**
     * Creates a hidden poster which will either add or remove a role from a
     * user, depending on if the user has the role type already or not.
     */
    private final class RoleChangingHiddenPoster extends HiddenPoster {
        private final IUserQuerier userQuerier;

        private RoleChangingHiddenPoster(IUserQuerier userQuerier,
                List<NamedAndExplainedRoleFactoryWithDecider<?>> factories) {
            this.userQuerier = userQuerier;
        }

        @SuppressWarnings("unused")
        public void run(String submit) {
            HibernateQuerier hq = new HibernateQuerier();
            Session session = hq.getSession();
            Transaction transaction = hq.getOrBeginTransaction();
            try {
                final String[] submitParts = submit.split(":");
                NamedAndExplainedRoleFactoryWithDecider<?> factory = factories
                        .get(TextUtil.safeParseInt(submitParts[0]));
                IUser user = userQuerier.get(submitParts[1]);
                @SuppressWarnings("unchecked")
                final Set<IUserRole> roles = (Set<IUserRole>) user
                        .getIUserRoles();
                IUserRole role = null;
                for (IUserRole r : roles) {
                    if (factory.getSubtypeDecider().decide(r))
                        role = r;
                }

                if (role == null) {
                    role = factory.createRole(user);
                    session.save(role);
                    roles.add(role);
                } else {
                    roles.remove(role);
                }
                transaction.commit();
                update(user);
            } catch (Exception e) {
                transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates a checkbox for a role type, which is checked if the user has the
     * corresponding role type.
     */
    public class CheckBoxFeature extends DefaultTableFeature<IUser> {

        private final NamedAndExplainedRoleFactoryWithDecider<?> factory;

        public CheckBoxFeature(
                NamedAndExplainedRoleFactoryWithDecider<?> factory) {
            this.factory = factory;
        }

        private XML fillCheckBox(IUser user) {
            XML xml = checkBoxTemplate;
            boolean checked = hasRole(user, factory.getSubtypeDecider());
            if (checked) {
                xml = xml.plug("CHECKED", "checked");
            }
            xml = xml.plug("NAME",
                    factories.indexOf(factory) + ":" + user.getId());
            return xml;
        }

        @Override
        public XML getCaption() {
            return factory.getName();
        }

        @Override
        public Object getLine(IUser user) {
            return fillCheckBox(user);
        }
    }

    /**
     * Simple interface for a Object which can return a user from an ID.
     */
    public interface IUserQuerier {
        IUser get(String string);
    }

    /**
     * All the data required for creating a column in a table.
     */
    public static interface NamedAndExplainedRoleFactoryWithDecider<T extends IUserRole> {
        public T createRole(IUser user);

        public XML getExplanation();

        public XML getName();

        public T getRole(Set<IUserRole> roles);

        public SubtypeDecider getSubtypeDecider();
    }

    /**
     * Compares two {@link IUser}s based on the order of the initially suplied
     * visitors, the visitors should yield true if they match a role.
     */
    public class OrderedIUserRoleIUserComparator implements Comparator<IUser> {

        private final List<SubtypeDecider> orderedDeciders;

        public OrderedIUserRoleIUserComparator(
                List<SubtypeDecider> orderedDeciders) {
            this.orderedDeciders = orderedDeciders;
        }

        @Override
        public int compare(IUser o1, IUser o2) {
            for (SubtypeDecider decider : orderedDeciders) {
                boolean match1 = hasRole(o1, decider);
                boolean match2 = hasRole(o2, decider);
                if (match1 != match2) {
                    return match1 ? -1 : 1;
                }
            }
            return 0;
        }

    }

    private final XML checkBoxTemplate;
    private final List<IUser> usersToSort;
    private final List<NamedAndExplainedRoleFactoryWithDecider<?>> factories;
    private final XML firstRow;
    private final List<IUser> usersToNotSort;

    @SuppressWarnings("unchecked")
    public GeneralizedConfigureUsersForm(List<? extends IUser> usersToSort,
            List<? extends IUser> usersToNotSort,
            final List<NamedAndExplainedRoleFactoryWithDecider<?>> factories,
            final IUserQuerier userQuerier, XML firstRow) {
        this.usersToSort = (List<IUser>) usersToSort;
        this.usersToNotSort = (List<IUser>) usersToNotSort;
        this.factories = factories;
        this.firstRow = firstRow;
        final HiddenPoster roleChanger = new RoleChangingHiddenPoster(
                userQuerier, factories);
        checkBoxTemplate = XML
                .parseTemplate(
                        "<form><input type='checkbox' name=[NAME] checked=[CHECKED] onclick=[HANDLER]/></form>")
                .plug("HANDLER", roleChanger.toStringReturnTrue());

    }

    @Override
    public XML getContent() {
        XML xml = XML
                .parseTemplate("<div><[TABLE]></div><div><[EXPLANATION]></div>");
        xml = xml.plug("EXPLANATION", makeExplanation());
        xml = xml.plug("TABLE",
                makeTable(factories, usersToSort, usersToNotSort));
        return xml;
    }

    @Override
    public XML getTitle() {
        return XML.parseTemplate("<[s_Configureusers]>");
    }

    /**
     * Extracts the visitors from a list of factories
     */
    private List<SubtypeDecider> getVisitors(
            List<NamedAndExplainedRoleFactoryWithDecider<?>> factories) {
        List<SubtypeDecider> deciders = new ArrayList<>();
        for (NamedAndExplainedRoleFactoryWithDecider<?> feature : factories) {
            deciders.add(feature.getSubtypeDecider());
        }
        return deciders;
    }

    /**
     * Checks if a {@link IUser} has a role of the desired type.
     */
    private boolean hasRole(IUser user, SubtypeDecider subtypeDecider) {
        for (IUserRole role : user.getIUserRoles()) {
            if (subtypeDecider.decide(role))
                return true;
        }
        return false;
    }

    /**
     * Creates a small table which explains each role.
     */
    private XML makeExplanation() {
        XML trTemplate = XML
                .parseTemplate("<tr><td><[NAME]></td><td><[EXPLANATION]></td></tr>");
        List<XML> explanations = new ArrayList<>();
        for (NamedAndExplainedRoleFactoryWithDecider<?> factory : factories) {
            XML tr = trTemplate;
            tr = tr.plug("NAME", factory.getName());
            tr = tr.plug("EXPLANATION", factory.getExplanation());
            explanations.add(tr);
        }
        return XML.parseTemplate(
                "<div><h3><[s_Explanation]></h3></div>" + "<div><table>"
                        + "<[BODY]>" + "</table></div>").plug("BODY",
                XML.concat(explanations));
    }

    /**
     * Creates the big table with all the checkboxes in.
     * 
     * @param usersToNotSort
     */
    private XML makeTable(
            List<NamedAndExplainedRoleFactoryWithDecider<?>> factories,
            List<IUser> usersToSort, List<IUser> usersToNotSort) {
        List<TableFeature<IUser>> features = new ArrayList<>();
        features.add(new IUserNameFeature());
        features.add(new IUserLoginFeature());
        for (NamedAndExplainedRoleFactoryWithDecider<?> factory : factories) {
            features.add(new CheckBoxFeature(factory));
        }
        TableWriter<IUser> tw = new TableWriter<>(features);
        Collections.sort(usersToSort, new OrderedIUserRoleIUserComparator(
                getVisitors(factories)));
        List<IUser> users = new ArrayList<>();
        users.addAll(usersToSort);
        users.addAll(usersToNotSort);
        for (IUser user : users) {
            tw.addRow(user);
            WebContext.addResponseInvalidator(user);
        }
        tw.setFirstRow(firstRow);
        return tw.getTable();
    }
}