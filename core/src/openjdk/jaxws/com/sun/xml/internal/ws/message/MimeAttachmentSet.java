/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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



package com.sun.xml.internal.ws.message;

import com.sun.xml.internal.ws.api.message.AttachmentSet;
import com.sun.xml.internal.ws.api.message.Attachment;
import com.sun.xml.internal.ws.encoding.MimeMultipartParser;
import com.sun.xml.internal.ws.message.stream.StreamAttachment;
import com.sun.xml.internal.ws.resources.EncodingMessages;
import com.sun.istack.internal.Nullable;

import javax.xml.ws.WebServiceException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

/**
 * {@link AttachmentSet} backed by {@link com.sun.xml.internal.ws.encoding.MimeMultipartParser}
 *
 * @author Vivek Pandey
 */
public final class MimeAttachmentSet implements AttachmentSet {
    private final MimeMultipartParser mpp;
    private Map<String, Attachment> atts = new HashMap<String, Attachment>();


    public MimeAttachmentSet(MimeMultipartParser mpp) {
        this.mpp = mpp;
    }

    @Nullable
    public Attachment get(String contentId) {
        Attachment att;
        /**
         * First try to get the Attachment from internal map, maybe this attachment
         * is added by the user.
         */
        att = atts.get(contentId);
        if(att != null)
            return att;
        try {
            /**
             * Attachment is not found in the internal map, now do look in
             * the mpp, if found add to the internal Attachment map.
             */
            att = mpp.getAttachmentPart(contentId);
            if(att != null){
                atts.put(contentId, att);
            }
        } catch (IOException e) {
            throw new WebServiceException(EncodingMessages.NO_SUCH_CONTENT_ID(contentId), e);
        }
        return att;
    }

    /**
     * This is expensive operation, its going to to read all the underlying
     * attachments in {@link MimeMultipartParser}.
     */
    public boolean isEmpty() {
        return atts.size() <= 0 && mpp.getAttachmentParts().isEmpty();
    }

    public void add(Attachment att) {
        atts.put(att.getContentId(), att);
    }

    /**
     * Expensive operation.
     */
    public Iterator<Attachment> iterator() {
        /**
         * Browse thru all the attachments in the mpp, add them to #atts,
         * then return whether its empty.
         */
        Map<String, StreamAttachment> attachments = mpp.getAttachmentParts();
        for(Map.Entry<String, StreamAttachment> att : attachments.entrySet()) {
            if(atts.get(att.getKey()) == null){
                atts.put(att.getKey(), att.getValue());
            }
        }

        return atts.values().iterator();
    }
}
