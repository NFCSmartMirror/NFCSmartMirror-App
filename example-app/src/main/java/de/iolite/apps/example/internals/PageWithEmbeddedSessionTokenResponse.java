/*
 * Copyright (C) 2016 IOLITE GmbH, All rights reserved.
 */

package de.iolite.apps.example.internals;

import java.util.Collections;
import java.util.Map;

import de.iolite.common.requesthandler.HTTPStatus;
import de.iolite.common.requesthandler.IOLITEHTTPResponse;
import de.iolite.common.requesthandler.IOLITEHTTPStaticResponse;
import de.iolite.common.requesthandler.IOLITEHTTPTextResponse;

/**
 * Represents an HTML page response with embedded session token.
 *
 * @author Falko Schwabe
 */
public final class PageWithEmbeddedSessionTokenResponse extends IOLITEHTTPTextResponse {

	private final String pageWithEmbeddedSessionToken;

	/**
	 * Creates an instance with the given rendered <code>pageWithEmbeddedSessionToken</code> that already contains the session token somewhere.
	 *
	 * @param pageWithEmbeddedSessionTokenIn the page content with the embedded session token
	 */
	public PageWithEmbeddedSessionTokenResponse(final String pageWithEmbeddedSessionTokenIn) {
		this.pageWithEmbeddedSessionToken = pageWithEmbeddedSessionTokenIn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getContentType() {
		return IOLITEHTTPResponse.HTML_CONTENT_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Map<String, String> getCookies() {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Map<String, String> getHeaderKeyValuePairs() {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getHTTPStatus() {
		return HTTPStatus.OK.code;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final String getCharset() {
		return IOLITEHTTPStaticResponse.ENCODING_UTF8;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final String getStringContent() {
		return this.pageWithEmbeddedSessionToken;
	}
}