package org.eclipse.ecf.provider.filetransfer.httpclient.extended;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemListener;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemRequest;
import org.eclipse.ecf.filetransfer.RemoteFileSystemException;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowser;
import org.eclipse.ecf.filetransfer.service.IRemoteFileSystemBrowserFactory;
import org.eclipse.ecf.provider.filetransfer.httpclient.HttpClientBrowseFileTransferFactory;
import org.eclipse.ecf.provider.filetransfer.httpclient.HttpClientFileSystemBrowser;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferNamespace;
import org.eclipse.osgi.util.NLS;

public class XHttpClientBrowseFileTransferFactory implements IRemoteFileSystemBrowserFactory {

	public IRemoteFileSystemBrowser newInstance() {
		return new IRemoteFileSystemBrowser() {

			private Proxy proxy;
			private IConnectContext connectContext;

			public Namespace getBrowseNamespace() {
				return IDFactory.getDefault().getNamespaceByName(FileTransferNamespace.PROTOCOL);
			}

			public IRemoteFileSystemRequest sendBrowseRequest(IFileID directoryOrFileId, IRemoteFileSystemListener listener) throws RemoteFileSystemException {
				Assert.isNotNull(directoryOrFileId);
				Assert.isNotNull(listener);
				URL url;
				try {
					url = directoryOrFileId.getURL();
				} catch (final MalformedURLException e) {
					throw new RemoteFileSystemException(NLS.bind("Exception creating URL for {0}", directoryOrFileId)); //$NON-NLS-1$
				}

				HttpClientFileSystemBrowser browser = new XHttpClientFileSystemBrowser(new HttpClient(new MultiThreadedHttpConnectionManager()), directoryOrFileId, listener, url, connectContext, proxy);
				return browser.sendBrowseRequest();
			}

			public void setConnectContextForAuthentication(IConnectContext connectContext) {
				this.connectContext = connectContext;
			}

			public void setProxy(Proxy proxy) {
				this.proxy = proxy;
			}

			public Object getAdapter(Class adapter) {
				return null;
			}

		};

	}
}
