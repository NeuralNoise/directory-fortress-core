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

import java.util.ArrayList;
import java.util.List;


/**
 * The class is used by {@link FortressAntTask} to load {@link GroupAnt}s used to drive {@link org.openldap.fortress.ldap.group.GroupMgr#add(org.openldap.fortress.ldap.group.Group)} .
 * It is not intended to be callable by programs outside of the Ant load utility.  The class name itself maps to the xml tag used by load utility.
 * <p>This class name, 'Delgroup', is used for the xml tag in the load script.</p>
 * <pre>
 * {@code
 * <target name="all">
 *     <FortressAdmin>
 *         <delgroup>
 *           ...
 *         </delgroup>
 *     </FortressAdmin>
 * </target>
 * }
 * </pre>
 *
 * @author Shawn McKinney
 */
public class Delgroup
{
    final private List<GroupAnt> groups = new ArrayList<>();

    /**
     * All Ant data entities must have a default constructor.
     */
    public Delgroup()
    {
    }

    /**
     * <p>This method name, 'addGroup', is used for derived xml tag 'group' in the load script.</p>
     * <pre>
     * {@code
     *  <delgroup>
     *      <group name="test001"/>
     *  </delgroup>
     * }
     * </pre>
     *
     * @param group contains reference to data element targeted for removal.
     */
    public void addGroup(GroupAnt group)
    {
        this.groups.add(group);
    }

    /**
     * Used by {@link FortressAntTask#deleteGroups()} to retrieve list of Groups as defined in input xml file.
     *
     * @return collection containing {@link GroupAnt}s targeted for removal.
     */
    public List<GroupAnt> getGroups()
    {
        return this.groups;
    }
}