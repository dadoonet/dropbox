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

import java.util.Map;

/**
 * Information about a user's account.
 */
public class DropboxAccount extends DropboxObject {

	private static final long serialVersionUID = 1L;

	/** The user's ISO country code. */
	public final String country;

	/** The user's "real" name. */
	public final String displayName;

	/** The user's quota, in bytes. */
	public final long quota;

	/** The user's quota excluding shared files. */
	public final long quotaNormal;

	/** The user's quota of shared files. */
	public final long quotaShared;

	/** The user's account ID. */
	public final long uid;

	/** The url the user can give to get referral credit. */
	public final String referralLink;
	
	/** The user's email */
	public final String email;

	/**
	 * Creates an account from a Map.
	 * 
	 * @param map
	 *            a Map that looks like:
	 * 
	 *            <pre>
	 * {"country": "",
	 *  "display_name": "John Q. User",
	 *  "email": "john@doe.com",
	 *  "quota_info": {
	 *    "shared": 37378890,
	 *    "quota": 62277025792,
	 *    "normal": 263758550
	 *   },
	 *  "uid": "174"}
	 * </pre>
	 */
	public DropboxAccount(Map<String, Object> map) {
		country = (String) map.get("country");
		displayName = (String) map.get("display_name");
		uid = getFromMapAsLong(map, "uid");
		referralLink = (String) map.get("referral_link");
		email = (String) map.get("email");

		Object quotaInfo = map.get("quota_info");
		@SuppressWarnings("unchecked")
		Map<String, Object> quotamap = (Map<String, Object>) quotaInfo;
		quota = getFromMapAsLong(quotamap, "quota");
		quotaNormal = getFromMapAsLong(quotamap, "normal");
		quotaShared = getFromMapAsLong(quotamap, "shared");
	}

	/**
	 * Creates an account object from an initial set of values.
	 */
	public DropboxAccount(String country, String displayName, String email, long uid,
			String referralLink, long quota, long quotaNormal, long quotaShared) {
		this.country = country;
		this.displayName = displayName;
		this.email = email;
		this.uid = uid;
		this.referralLink = referralLink;
		this.quota = quota;
		this.quotaNormal = quotaNormal;
		this.quotaShared = quotaShared;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the quota
	 */
	public long getQuota() {
		return quota;
	}

	/**
	 * @return the quotaNormal
	 */
	public long getQuotaNormal() {
		return quotaNormal;
	}

	/**
	 * @return the quotaShared
	 */
	public long getQuotaShared() {
		return quotaShared;
	}

	/**
	 * @return the uid
	 */
	public long getUid() {
		return uid;
	}

	/**
	 * @return the referralLink
	 */
	public String getReferralLink() {
		return referralLink;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	
}
