/**
 * Copyright (c) 2010, Intalio Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intalio Inc. - initial API and implementation
 */
package org.eclipse.ecf.provider.filetransfer.httpclient.extended;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.UnsupportedCallbackException;
import org.eclipse.ecf.filetransfer.IFileRangeSpecification;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.provider.filetransfer.httpclient.HttpClientRetrieveFileTransfer;

/**
 * Adds support for passing the user info in the url.
 * Typically used to pass username/password for HTTP Authentication.
 * For example http://joe:secret@example.com/repository
 * 
 * @author hmalphettes
 *
 */
public class XHttpClientRetrieveFileTransfer extends
		HttpClientRetrieveFileTransfer {

	
	private URL originalUrl;
	
	public XHttpClientRetrieveFileTransfer(HttpClient httpClient) {
		super(httpClient);
	}

	@Override
	protected Credentials getFileRequestCredentials()
			throws UnsupportedCallbackException, IOException {
		Credentials cred = super.getFileRequestCredentials();
		if (cred != null) {
			//the super class probably knows better.
			return cred;
		}
		if (originalUrl != null) {
			String userInfo = originalUrl.getUserInfo();
			UsernamePasswordCredentials userNamePass = new UsernamePasswordCredentials(userInfo);
			setPrivateUsername(userNamePass.getUserName());
			return userNamePass;
		}
		return cred;
	}
	
	protected URL getRemoteFileURL() {
		return remoteFileURL;
	}

	@Override
	public void sendRetrieveRequest(IFileID rFileID,
			IFileRangeSpecification rangeSpec,
			IFileTransferListener transferListener, Map ops)
			throws IncomingFileTransferException {
		try {
			URL ori = rFileID.getURL();
			URL url = XHttpClientFileSystemBrowser.getURLWithoutUserInfo(ori);
			if (url != null) {
				this.originalUrl = ori;
				Namespace namespace = rFileID.getNamespace();
				rFileID = (IFileID) IDFactory.getDefault().createID(namespace, new Object[] {url});
			}
		} catch (final MalformedURLException e) {
			setDoneException(e);
			fireTransferReceiveDoneEvent();
			return;
		}
		super.sendRetrieveRequest(rFileID, rangeSpec, transferListener, ops);
	}

	
	private static Field FIELD_username;
	private void setPrivateUsername(String username) {
		try {
			if (FIELD_username == null) {
				FIELD_username = HttpClientRetrieveFileTransfer.class.getDeclaredField("username");
				FIELD_username.setAccessible(true);
			}
			FIELD_username.set(this, username);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
}
