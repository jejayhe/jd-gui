/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.net;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.api.model.Type;
import org.jd.gui.service.type.TypeFactoryService;
import org.jd.gui.spi.TypeFactory;
import org.jd.gui.util.exception.ExceptionUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class UriUtil {
    /*
     * Convert inner entry URI to outer entry uri with a fragment. Example:
     *  file://codebase/a/b/c/D$E.class => file://codebase/a/b/c/D.class#typeDeclaration=D$E
     */
    public static URI createURI(API api, Collection<Indexes> collectionOfIndexes, Container.Entry entry, String query, String fragment) {
        TypeFactory typeFactory = TypeFactoryService.getInstance().get(entry);

        if (typeFactory != null) {
            Type type = typeFactory.make(api, entry, fragment);

            if (type != null) {
                URI uri = entry.getUri();
                String path = getOuterPath(collectionOfIndexes, entry, type);

                try {
                    return new URI(uri.getScheme(), uri.getHost(), path, query, fragment);
                } catch (URISyntaxException e) {
                    ExceptionUtil.printStackTrace(e);
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    protected static String getOuterPath(Collection<Indexes> collectionOfIndexes, Container.Entry entry, Type type) {
        String outerName = type.getOuterName();

        if (outerName != null) {
            for (Indexes indexes : collectionOfIndexes) {
                Collection<Container.Entry> outerEntries = indexes.getIndex("typeDeclarations").get(outerName);

                if (outerEntries != null) {
                    for (Container.Entry outerEntry : outerEntries) {
                        if (outerEntry.getContainer() == entry.getContainer()) {
                            return outerEntry.getUri().getPath();
                        }
                    }
                }
            }
        }

        return entry.getUri().getPath();
    }
}