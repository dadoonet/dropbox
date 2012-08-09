/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.river.dropbox.connector;

import java.io.Serializable;
import java.util.Map;

import fr.pilato.elasticsearch.river.dropbox.util.StringTools;

public class DropboxObject implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Helper function to read long JSON return values
	 * 
	 * @param map
	 *            the one to read from
	 * @param name
	 *            the parameter name to read
	 * @return the value, with 0 as a default if no parameter set
	 */
	protected static long getFromMapAsLong(Map<String, Object> map, String name) {
		Object val = map.get(name);
		long ret = 0;
		if (val != null) {
			if (val instanceof Number) {
				ret = ((Number) val).longValue();
			} else if (val instanceof String) {
				// To parse cases where JSON can't represent a Long, so
				// it's stored as a string
				ret = Long.parseLong((String) val, 16);
			}
		}
		return ret;
	}

    /**
     * Helper function to read boolean JSON return values
     *
     * @param map
     *            the one to read from
     * @param name
     *            the parameter name to read
     * @return the value, with false as a default if no parameter set
     */
    protected static boolean getFromMapAsBoolean(Map<String, Object> map, String name) {
        Object val = map.get(name);
        if (val != null && val instanceof Boolean) {
            return ((Boolean) val).booleanValue();
        } else {
            return false;
        }
    }


	@Override
	public String toString() {
		return StringTools.toString(this);
	}
}
