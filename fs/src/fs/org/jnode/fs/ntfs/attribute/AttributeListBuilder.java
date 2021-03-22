/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.fs.ntfs.attribute;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jnode.util.ObjectUtils;

/**
 * A builder for reading and merging in {@link NTFSAttribute}s as they are read in.
 *
 * @author Luke Quinane
 */
public class AttributeListBuilder {

    /**
     * My logger
     */
    protected static final Logger log = Logger.getLogger(AttributeListBuilder.class);

    /**
     * The map of non-resident attributes by type, then by stream name.
     */
    private final Map<Integer, Map<String, NTFSNonResidentAttribute>> nonResByType =
        new LinkedHashMap<Integer, Map<String, NTFSNonResidentAttribute>>();

    /**
     * The attribute list being built.
     */
    private List<NTFSAttribute> attributeList = new ArrayList<NTFSAttribute>();

    /**
     * Adds a {@link NTFSAttribute} to the builder. Resident attributes are just added without further merging, but
     * non-resident attributes have their data runs merged if they are the same type and stream name as an previous
     * attribute.
     *
     * @param attribute the attribute to add.
     */
    public void add(NTFSAttribute attribute) {
        if (attribute == null) {
            return;
        }

        if (attribute.isResident()) {
            attributeList.add(attribute);
        } else {
            int attrType = attribute.getAttributeType();
            Map<String, NTFSNonResidentAttribute> typeByName = nonResByType.get(attrType);

            // Use an empty string for the default (null) stream name.
            String streamName = ObjectUtils.firstNonNull(attribute.getAttributeName(), "");
            NTFSNonResidentAttribute nextNonRes = (NTFSNonResidentAttribute) attribute;
            int dataRunsOffset = nextNonRes.getDataRunsOffset();

            if (typeByName != null && typeByName.containsKey(streamName)) {
                NTFSNonResidentAttribute firstAttribute = typeByName.get(streamName);

                // Read all of the subsequent data runs into the first attribute's list
                firstAttribute.getDataRunDecoder().readDataRuns(nextNonRes, dataRunsOffset);

            } else {
                if (typeByName == null) {
                    nonResByType.put(attrType, new LinkedHashMap<String, NTFSNonResidentAttribute>());
                    typeByName = nonResByType.get(attrType);
                }

                if (nextNonRes.getDataRunDecoder().getDataRuns().isEmpty() && dataRunsOffset != 0) {
                    nextNonRes.getDataRunDecoder().readDataRuns(nextNonRes, dataRunsOffset);
                }

                // Record the first non-resident attribute of each type, by stream name
                typeByName.put(streamName, nextNonRes);
                attributeList.add(attribute);
            }
        }
    }

    /**
     * Gets the attribute list.
     *
     * @return the list of attributes.
     */
    public List<NTFSAttribute> toList() {
        return attributeList;
    }
}
