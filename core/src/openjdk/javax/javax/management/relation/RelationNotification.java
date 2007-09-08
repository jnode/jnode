/*
 * Copyright 2000-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package javax.management.relation;

import javax.management.Notification;
import javax.management.ObjectName;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.jmx.mbeanserver.GetPropertyAction;
import static com.sun.jmx.mbeanserver.Util.cast;

/**
 * A notification of a change in the Relation Service.
 * A RelationNotification notification is sent when a relation is created via
 * the Relation Service, or an MBean is added as a relation in the Relation
 * Service, or a role is updated in a relation, or a relation is removed from
 * the Relation Service.
 *
 * <p>The <b>serialVersionUID</b> of this class is <code>-6871117877523310399L</code>.
 * 
 * @since 1.5
 */
@SuppressWarnings("serial")  // serialVersionUID not constant
public class RelationNotification extends Notification {

    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form 
    private static final long oldSerialVersionUID = -2126464566505527147L;
    //
    // Serial version for new serial form 
    private static final long newSerialVersionUID = -6871117877523310399L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields = 
    {
	new ObjectStreamField("myNewRoleValue", ArrayList.class),
	new ObjectStreamField("myOldRoleValue", ArrayList.class),
	new ObjectStreamField("myRelId", String.class),
	new ObjectStreamField("myRelObjName", ObjectName.class),
	new ObjectStreamField("myRelTypeName", String.class),
	new ObjectStreamField("myRoleName", String.class),
	new ObjectStreamField("myUnregMBeanList", ArrayList.class)
    };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields = 
    {
	new ObjectStreamField("newRoleValue", List.class),
	new ObjectStreamField("oldRoleValue", List.class),
	new ObjectStreamField("relationId", String.class),
	new ObjectStreamField("relationObjName", ObjectName.class),
	new ObjectStreamField("relationTypeName", String.class),
	new ObjectStreamField("roleName", String.class),
	new ObjectStreamField("unregisterMBeanList", List.class)
    };
    //
    // Actual serial version and serial form
    private static final long serialVersionUID;
    /**
     * @serialField relationId String Relation identifier of
     * created/removed/updated relation
     * @serialField relationTypeName String Relation type name of
     * created/removed/updated relation
     * @serialField relationObjName ObjectName {@link ObjectName} of
     * the relation MBean of created/removed/updated relation (only if
     * the relation is represented by an MBean)
     * @serialField unregisterMBeanList List List of {@link
     * ObjectName}s of referenced MBeans to be unregistered due to
     * relation removal
     * @serialField roleName String Name of updated role (only for role update)
     * @serialField oldRoleValue List Old role value ({@link
     * ArrayList} of {@link ObjectName}s) (only for role update)
     * @serialField newRoleValue List New role value ({@link
     * ArrayList} of {@link ObjectName}s) (only for role update)
     */
    private static final ObjectStreamField[] serialPersistentFields;
    private static boolean compat = false;  
    static {
	try {
	    GetPropertyAction act = new GetPropertyAction("jmx.serial.form");
	    String form = AccessController.doPrivileged(act);
	    compat = (form != null && form.equals("1.0"));
	} catch (Exception e) {
	    // OK : Too bad, no compat with 1.0
	}
	if (compat) {
	    serialPersistentFields = oldSerialPersistentFields;
	    serialVersionUID = oldSerialVersionUID;
	} else {
	    serialPersistentFields = newSerialPersistentFields;
	    serialVersionUID = newSerialVersionUID;
	}
    }
    //
    // END Serialization compatibility stuff

    //
    // Notification types
    //

    /**
     * Type for the creation of an internal relation.
     */
    public static final String RELATION_BASIC_CREATION = "jmx.relation.creation.basic";
    /**
     * Type for the relation MBean added into the Relation Service.
     */
    public static final String RELATION_MBEAN_CREATION = "jmx.relation.creation.mbean";
    /**
     * Type for an update of an internal relation.
     */
    public static final String RELATION_BASIC_UPDATE = "jmx.relation.update.basic";
    /**
     * Type for the update of a relation MBean.
     */
    public static final String RELATION_MBEAN_UPDATE = "jmx.relation.update.mbean";
    /**
     * Type for the removal from the Relation Service of an internal relation.
     */
    public static final String RELATION_BASIC_REMOVAL = "jmx.relation.removal.basic";
    /**
     * Type for the removal from the Relation Service of a relation MBean.
     */
    public static final String RELATION_MBEAN_REMOVAL = "jmx.relation.removal.mbean";

    //
    // Private members
    //

    /**
     * @serial Relation identifier of created/removed/updated relation
     */
    private String relationId = null;

    /**
     * @serial Relation type name of created/removed/updated relation
     */
    private String relationTypeName = null;

    /**
     * @serial {@link ObjectName} of the relation MBean of created/removed/updated relation
     *         (only if the relation is represented by an MBean)
     */
    private ObjectName relationObjName = null;

    /**
     * @serial List of {@link ObjectName}s of referenced MBeans to be unregistered due to
     *         relation removal
     */
    private List<ObjectName> unregisterMBeanList = null;

    /**
     * @serial Name of updated role (only for role update)
     */
    private String roleName = null;

    /** 
     * @serial Old role value ({@link ArrayList} of {@link ObjectName}s) (only for role update)
     */
    private List<ObjectName> oldRoleValue = null;

    /**
     * @serial New role value ({@link ArrayList} of {@link ObjectName}s) (only for role update)
     */
    private List<ObjectName> newRoleValue = null;

    //
    // Constructors
    //

    /**
     * Creates a notification for either a relation creation (RelationSupport
     * object created internally in the Relation Service, or an MBean added as a
     * relation) or for a relation removal from the Relation Service.
     *
     * @param notifType  type of the notification; either:
     * <P>- RELATION_BASIC_CREATION
     * <P>- RELATION_MBEAN_CREATION
     * <P>- RELATION_BASIC_REMOVAL
     * <P>- RELATION_MBEAN_REMOVAL
     * @param sourceObj  source object, sending the notification.  This is either
     * an ObjectName or a RelationService object.  In the latter case it must be
     * the MBean emitting the notification; the MBean Server will rewrite the
     * source to be the ObjectName under which that MBean is registered.
     * @param sequence  sequence number to identify the notification
     * @param timeStamp  time stamp
     * @param message  human-readable message describing the notification
     * @param id  relation id identifying the relation in the Relation
     * Service
     * @param typeName  name of the relation type
     * @param objectName  ObjectName of the relation object if it is an MBean
     * (null for relations internally handled by the Relation Service)
     * @param unregMBeanList  list of ObjectNames of referenced MBeans
     * expected to be unregistered due to relation removal (only for removal,
     * due to CIM qualifiers, can be null)
     *
     * @exception IllegalArgumentException  if:
     * <P>- no value for the notification type
     * <P>- the notification type is not RELATION_BASIC_CREATION,
     * RELATION_MBEAN_CREATION, RELATION_BASIC_REMOVAL or
     * RELATION_MBEAN_REMOVAL
     * <P>- no source object
     * <P>- the source object is not a Relation Service
     * <P>- no relation id
     * <P>- no relation type name
     */
    public RelationNotification(String notifType,
				Object sourceObj,
				long sequence,
				long timeStamp,
				String message,
				String id,
				String typeName,
				ObjectName objectName,
				List<ObjectName> unregMBeanList)
	throws IllegalArgumentException {

	super(notifType, sourceObj, sequence, timeStamp, message);

	// Can throw IllegalArgumentException
	initMembers(1,
		    notifType,
		    sourceObj,
		    sequence,
		    timeStamp,
		    message,
		    id,
		    typeName,
		    objectName,
		    unregMBeanList,
		    null,
		    null,
		    null);
	return;
    }

    /**
     * Creates a notification for a role update in a relation.
     *
     * @param notifType  type of the notification; either:
     * <P>- RELATION_BASIC_UPDATE
     * <P>- RELATION_MBEAN_UPDATE
     * @param sourceObj  source object, sending the notification. This is either
     * an ObjectName or a RelationService object.  In the latter case it must be
     * the MBean emitting the notification; the MBean Server will rewrite the
     * source to be the ObjectName under which that MBean is registered.
     * @param sequence  sequence number to identify the notification
     * @param timeStamp  time stamp
     * @param message  human-readable message describing the notification
     * @param id  relation id identifying the relation in the Relation
     * Service
     * @param typeName  name of the relation type
     * @param objectName  ObjectName of the relation object if it is an MBean
     * (null for relations internally handled by the Relation Service)
     * @param name  name of the updated role
     * @param newValue  new role value (List of ObjectName objects)
     * @param oldValue  old role value (List of ObjectName objects)
     *
     * @exception IllegalArgumentException  if null parameter
     */
    public RelationNotification(String notifType,
				Object sourceObj,
				long sequence,
				long timeStamp,
				String message,
				String id,
				String typeName,
				ObjectName objectName,
				String name,
				List<ObjectName> newValue,
				List<ObjectName> oldValue
				)
	    throws IllegalArgumentException {

	super(notifType, sourceObj, sequence, timeStamp, message);

	// Can throw IllegalArgumentException
	initMembers(2,
		    notifType,
		    sourceObj,
		    sequence,
		    timeStamp,
		    message,
		    id,
		    typeName,
		    objectName,
		    null,
		    name,
		    newValue,
		    oldValue);
	return;
    }

    //
    // Accessors
    //

    /**
     * Returns the relation identifier of created/removed/updated relation.
     *
     * @return the relation id.
     */
    public String getRelationId() {
	return relationId;
    }

    /**
     * Returns the relation type name of created/removed/updated relation.
     *
     * @return the relation type name.
     */
    public String getRelationTypeName() {
	return relationTypeName;
    }

    /**
     * Returns the ObjectName of the
     * created/removed/updated relation.
     *
     * @return the ObjectName if the relation is an MBean, otherwise null.
     */
    public ObjectName getObjectName() {
	return relationObjName;
    }

    /**
     * Returns the list of ObjectNames of MBeans expected to be unregistered
     * due to a relation removal (only for relation removal).
     *
     * @return a {@link List} of {@link ObjectName}.
     */
    public List<ObjectName> getMBeansToUnregister() {
	List<ObjectName> result = null;
	if (unregisterMBeanList != null) {
	    result = new ArrayList<ObjectName>(unregisterMBeanList);
	} else {
	    result = Collections.emptyList();
	}
	return result;
    }

    /**
     * Returns name of updated role of updated relation (only for role update).
     *
     * @return the name of the updated role.
     */
    public String getRoleName() {
	String result = null;
	if (roleName != null) {
	    result = roleName;
	}
	return result;
    }

    /**
     * Returns old value of updated role (only for role update).
     *
     * @return the old value of the updated role.
     */
    public List<ObjectName> getOldRoleValue() {
	List<ObjectName> result = null;
	if (oldRoleValue != null) {
	    result = new ArrayList<ObjectName>(oldRoleValue);
	} else {
	    result = Collections.emptyList();
	}
	return result;
    }

    /**
     * Returns new value of updated role (only for role update).
     *
     * @return the new value of the updated role.
     */
    public List<ObjectName> getNewRoleValue() {
	List<ObjectName> result = null;
	if (newRoleValue != null) {
	    result = new ArrayList<ObjectName>(newRoleValue);
	} else {
	    result = Collections.emptyList();
	}
	return result;
    }

    //
    // Misc
    //

    // Initializes members
    //
    // -param notifKind  1 for creation/removal, 2 for update
    // -param notifType  type of the notification; either:
    //  - RELATION_BASIC_UPDATE
    //  - RELATION_MBEAN_UPDATE
    //  for an update, or:
    //  - RELATION_BASIC_CREATION
    //  - RELATION_MBEAN_CREATION
    //  - RELATION_BASIC_REMOVAL
    //  - RELATION_MBEAN_REMOVAL
    //  for a creation or removal
    // -param sourceObj  source object, sending the notification. Will always
    //  be a RelationService object.
    // -param sequence  sequence number to identify the notification
    // -param timeStamp  time stamp
    // -param message  human-readable message describing the notification
    // -param id  relation id identifying the relation in the Relation
    //  Service
    // -param typeName  name of the relation type
    // -param objectName  ObjectName of the relation object if it is an MBean
    //  (null for relations internally handled by the Relation Service)
    // -param unregMBeanList  list of ObjectNames of MBeans expected to be
    //  removed due to relation removal
    // -param name  name of the updated role
    // -param newValue  new value (List of ObjectName objects)
    // -param oldValue  old value (List of ObjectName objects)
    //
    // -exception IllegalArgumentException  if:
    //  - no value for the notification type
    //  - incorrect notification type
    //  - no source object
    //  - the source object is not a Relation Service
    //  - no relation id
    //  - no relation type name
    //  - no role name (for role update)
    //  - no role old value (for role update)
    //  - no role new value (for role update)
    private void initMembers(int notifKind,
			     String notifType,
			     Object sourceObj,
			     long sequence,
			     long timeStamp,
			     String message,
			     String id,
			     String typeName,
			     ObjectName objectName,
			     List<ObjectName> unregMBeanList,
			     String name,
			     List<ObjectName> newValue,
			     List<ObjectName> oldValue)
	    throws IllegalArgumentException {

	boolean badInitFlg = false;

	if (notifType == null ||	  
	    sourceObj == null ||
	    (!(sourceObj instanceof RelationService) &&
             !(sourceObj instanceof ObjectName)) ||
	    id == null ||
	    typeName == null) {

	    badInitFlg = true;
	}

	if (notifKind == 1) {

	    if ((!(notifType.equals(RelationNotification.RELATION_BASIC_CREATION)))
		&&
		(!(notifType.equals(RelationNotification.RELATION_MBEAN_CREATION)))
		&&
		(!(notifType.equals(RelationNotification.RELATION_BASIC_REMOVAL)))
		&&
		(!(notifType.equals(RelationNotification.RELATION_MBEAN_REMOVAL)))
		) {

		// Creation/removal
		badInitFlg = true;
	    }

	} else if (notifKind == 2) {

	    if (((!(notifType.equals(RelationNotification.RELATION_BASIC_UPDATE)))
		 &&
		 (!(notifType.equals(RelationNotification.RELATION_MBEAN_UPDATE))))
		|| name == null ||
		oldValue == null ||
		newValue == null) {

		// Role update
		badInitFlg = true;
	    }
	}

	if (badInitFlg) {
	    String excMsg = "Invalid parameter.";
	    throw new IllegalArgumentException(excMsg);
	}

	relationId = id;
	relationTypeName = typeName;
	relationObjName = objectName;
	if (unregMBeanList != null) {
	    unregisterMBeanList = new ArrayList<ObjectName>(unregMBeanList);
	}
	if (name != null) {
	    roleName = name;
	}
	if (oldValue != null) {
	    oldRoleValue = new ArrayList<ObjectName>(oldValue);
	}
	if (newValue != null) {
	    newRoleValue = new ArrayList<ObjectName>(newValue);
	}
	return;
    }

    /**
     * Deserializes a {@link RelationNotification} from an {@link ObjectInputStream}.
     */
    private void readObject(ObjectInputStream in)
	    throws IOException, ClassNotFoundException {
      if (compat)
      {
        // Read an object serialized in the old serial form
        //
        ObjectInputStream.GetField fields = in.readFields();
	newRoleValue = cast(fields.get("myNewRoleValue", null));
	if (fields.defaulted("myNewRoleValue"))
        {
          throw new NullPointerException("newRoleValue");
        }
	oldRoleValue = cast(fields.get("myOldRoleValue", null));
	if (fields.defaulted("myOldRoleValue"))
        {
          throw new NullPointerException("oldRoleValue");
        }
	relationId = (String) fields.get("myRelId", null);
	if (fields.defaulted("myRelId"))
        {
          throw new NullPointerException("relationId");
        }
	relationObjName = (ObjectName) fields.get("myRelObjName", null);
	if (fields.defaulted("myRelObjName"))
        {
          throw new NullPointerException("relationObjName");
        }
	relationTypeName = (String) fields.get("myRelTypeName", null);
	if (fields.defaulted("myRelTypeName"))
        {
          throw new NullPointerException("relationTypeName");
        }
	roleName = (String) fields.get("myRoleName", null);
	if (fields.defaulted("myRoleName"))
        {
          throw new NullPointerException("roleName");
        }
	unregisterMBeanList = cast(fields.get("myUnregMBeanList", null));
	if (fields.defaulted("myUnregMBeanList"))
        {
          throw new NullPointerException("unregisterMBeanList");
        }
      }
      else
      {
        // Read an object serialized in the new serial form
        //
        in.defaultReadObject();
      }
    }


    /**
     * Serializes a {@link RelationNotification} to an {@link ObjectOutputStream}.
     */
    private void writeObject(ObjectOutputStream out)
	    throws IOException {
      if (compat)
      {
        // Serializes this instance in the old serial form
        //
        ObjectOutputStream.PutField fields = out.putFields();
	fields.put("myNewRoleValue", newRoleValue);
	fields.put("myOldRoleValue", oldRoleValue);
	fields.put("myRelId", relationId);
	fields.put("myRelObjName", relationObjName);
	fields.put("myRelTypeName", relationTypeName);
	fields.put("myRoleName",roleName);
	fields.put("myUnregMBeanList", unregisterMBeanList);
	out.writeFields();
      }
      else
      {
        // Serializes this instance in the new serial form
        //
        out.defaultWriteObject();
      }
    }
}
