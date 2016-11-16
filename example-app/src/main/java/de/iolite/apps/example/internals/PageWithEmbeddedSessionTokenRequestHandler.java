/*
 * Copyright (C) 2016 IOLITE GmbH, All rights reserved.
 */

package de.iolite.apps.example.internals;

import de.iolite.app.api.frontend.util.FrontendAPIRequestHandler;
import de.iolite.common.requesthandler.IOLITEHTTPRequest;
import de.iolite.common.requesthandler.IOLITEHTTPResponse;

/**
 * Handles requests for mostly static HTML page resources, which needs to be rendered to embed a current session token to send them as response for some
 * request.
 *
 * @author Falko Schwabe
 */
public class PageWithEmbeddedSessionTokenRequestHandler extends FrontendAPIRequestHandler {

	private final String template;

	/**
	 * Creates an instance with an not-yet rendered template HTML page, which contains a mark to replace with the session token.
	 *
	 * @param templateIn the content of the template HTML page
	 */
	public PageWithEmbeddedSessionTokenRequestHandler(final String templateIn) {
		if (templateIn == null) {
			throw new IllegalArgumentException("argument template must not be null");
		}
		this.template = templateIn;
	}

	/**
	 * Returns the name of the template parameter to substitute with the session token. Substitution is accomplished with
	 * {@link String#replace(CharSequence, CharSequence)}.
	 *
	 * @return name of the SID template parameter
	 */
	protected static String getSIDTemplateParameterName() {
		return "{{SID}}";
	}

	/**
	 * @return the indexTemplate
	 */
	public String getTemplate() {
		return this.template;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IOLITEHTTPResponse handleRequest(final IOLITEHTTPRequest request, final String subPath) {
		return new PageWithEmbeddedSessionTokenResponse(getTemplate().replace(getSIDTemplateParameterName(), request.getSessionToken()));
	}
}