/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.fortress.core.ant;

import org.apache.directory.fortress.core.ldap.group.Group;

import java.util.ArrayList;
import java.util.List;


/**
 * The class is used by {@link FortressAntTask} to load {@link GroupAnt}s used to drive {@link org.apache.directory.fortress.core.ldap.group.GroupMgr#add(org.apache.directory.fortress.core.ldap.group.Group)} .
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
    final private List<Group> groups = new ArrayList<>();

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
    public void addGroup(Group group)
    {
        this.groups.add(group);
    }

    /**
     * Used by {@link FortressAntTask#deleteGroups()} to retrieve list of Groups as defined in input xml file.
     *
     * @return collection containing {@link GroupAnt}s targeted for removal.
     */
    public List<Group> getGroups()
    {
        return this.groups;
    }
}