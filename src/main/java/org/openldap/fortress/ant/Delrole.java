/*
 * This work is part of OpenLDAP Software <http://www.openldap.org/>.
 *
 * Copyright 1998-2014 The OpenLDAP Foundation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the OpenLDAP
 * Public License.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.OpenLDAP.org/license.html>.
 */

package org.openldap.fortress.ant;

import org.openldap.fortress.rbac.Role;

import java.util.ArrayList;
import java.util.List;


/**
 * The class is used by {@link FortressAntTask} to load {@link org.openldap.fortress.rbac.Role}s used to drive {@link org.openldap.fortress.AdminMgr#deleteRole(org.openldap.fortress.rbac.Role)}}.
 * It is not intended to be callable by programs outside of the Ant load utility.  The class name itself maps to the xml tag used by load utility.
 * <p>This class name, 'Delrole', is used for the xml tag in the load script.</p>
 * <pre>
 * {@code
 * <target name="all">
 *     <FortressAdmin>
 *         <delrole>
 *           ...
 *         </delrole>
 *     </FortressAdmin>
 * </target>
 * }
 * </pre>
 *
 * @author Shawn McKinney
 */
public class Delrole
{
    final private List<Role> roles = new ArrayList<>();

    /**
     * All Ant data entities must have a default constructor.
     */
    public Delrole()
    {
    }

    /**
     * <p>This method name, 'addRole', is used for derived xml tag 'role' in the load script.</p>
     * <pre>
     * {@code
     * <delrole>
     *     <role name="role1"/>
     *     <role name="role2"/>
     * </delrole>
     * }
     * </pre>
     *
     * @param role contains reference to data element targeted for removal.
     */
    public void addRole(Role role)
    {
        this.roles.add(role);
    }

    /**
     * Used by {@link FortressAntTask#deleteRoles()} to retrieve list of Roles as defined in input xml file.
     *
     * @return collection containing {@link org.openldap.fortress.rbac.Role}s targeted for removal.
     */
    public List<Role> getRoles()
    {
        return this.roles;
    }
}
