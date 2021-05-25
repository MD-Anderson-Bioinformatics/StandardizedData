// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.gdc.api;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Tod-Casasent
 */
public class DownloadUrl
{

	private String mUrl = null;
	private int mRetries = -1;
	private int mBaseTimeout = -1;
	private String mRequestMethod = null;
	private boolean mFollowRedirectsFlag = false;
	private String mContentType = null;
	private File mDestFile = null;
	private String mMD5 = null;
	private String mParameters = null;
	private Map<String, String> mRequestProperties = null;

	synchronized static public HttpURLConnection openConnection(String theUrl) throws MalformedURLException, IOException
	{
		URL myURL = new URL(theUrl);
		HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
		return connection;
	}

	public DownloadUrl(String theUrl, File theDestFile, String theMD5,
			int theRetries, int theBaseTimeout, String theRequestMethod,
			boolean theFollowRedirectsFlag, String theContentType,
			String theParameters, Map<String, String> theRequestProperties)
	{
		mUrl = theUrl;
		mDestFile = theDestFile;
		mMD5 = theMD5;
		mRetries = theRetries;
		mBaseTimeout = theBaseTimeout;
		mRequestMethod = theRequestMethod;
		mFollowRedirectsFlag = theFollowRedirectsFlag;
		mContentType = theContentType;
		mParameters = theParameters;
		mRequestProperties = theRequestProperties;
	}

	protected int checkResponseCode(HttpURLConnection theConnection) throws IOException
	{
		int code = theConnection.getResponseCode();
		//GDCAPI.printLn("Download_Mixin::makeConnection getResponseCode = " + code);
		if ((code >= 300) && (code < 400))
		{
			URL redirectUrl = new URL(theConnection.getHeaderField("Location"));
			GDCAPI.printLn("Download_Mixin::makeConnection redirectUrl = " + redirectUrl);
		}
		return code;
	}

	protected HttpURLConnection makeConnection() throws MalformedURLException, IOException
	{
		//GDCAPI.printLn("Download_Mixin::makeConnection mUrl = " + mUrl);
		HttpURLConnection connection = openConnection(mUrl);
		connection.setConnectTimeout(mBaseTimeout * 1);
		connection.setReadTimeout(mBaseTimeout * 10);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(mFollowRedirectsFlag);
		connection.setRequestMethod(mRequestMethod);
		connection.setRequestProperty("Content-Type", mContentType);
		if (null != mParameters)
		{
			connection.setRequestProperty("Content-Length", "" + Integer.toString(mParameters.getBytes().length));
		}
		if (null != mRequestProperties)
		{
			for (Entry<String, String> pair : mRequestProperties.entrySet())
			{
				connection.setRequestProperty(pair.getKey(), pair.getValue());
			}
		}
		connection.setUseCaches(false);
		return connection;
	}

	protected String downloadToString(HttpURLConnection theConnection) throws IOException
	{
		String result = null;
		try (DataOutputStream wr = new DataOutputStream(theConnection.getOutputStream()))
		{
			if (null != mParameters)
			{
				GDCAPI.printLn("downloadToString URL=" + mUrl);
				GDCAPI.printLn("downloadToString mParameters " + mParameters);
				wr.write(mParameters.getBytes());
			}
			wr.flush();
			checkResponseCode(theConnection);
			result = IOUtils.toString(theConnection.getInputStream(), Charset.defaultCharset());
		}
		return result;
	}

	protected File downloadToDir(HttpURLConnection theConnection) throws Exception
	{
		File result = null;
		File downloadFile = null;
		try
		{
			checkResponseCode(theConnection);
			String filename = theConnection.getHeaderField("Content-Disposition");
			if (filename.contains("filename="))
			{
				filename = filename.substring(filename.lastIndexOf("filename=") + "filename=".length());
			}
			downloadFile = new File(mDestFile, filename);
			GDCAPI.printLn("downloadToDir filename " + filename + " for URL=" + mUrl);
			if (downloadFile.exists())
			{
				GDCAPI.printLn("Skip download, already downloaded " + filename + " for URL=" + mUrl);
			}
			else
			{
				org.apache.commons.io.FileUtils.copyInputStreamToFile(theConnection.getInputStream(), mDestFile);
				GDCAPI.printLn("Download succeeded 1: " + mDestFile + " for URL=" + mUrl);
			}
			result = downloadFile;
		}
		catch (Exception rethrownExp)
		{
			if (null != downloadFile)
			{
				if (downloadFile.exists())
				{
					GDCAPI.printLn("remove partial download" + " for URL=" + mUrl);
					downloadFile.delete();
				}
			}
			throw rethrownExp;
		}
		return result;
	}

	protected File downloadToFile(HttpURLConnection theConnection) throws Exception
	{
		File result = null;
		try
		{
			checkResponseCode(theConnection);
			if (mDestFile.exists())
			{
				GDCAPI.printLn("Skip download, already downloaded " + mDestFile + " for URL=" + mUrl);
			}
			else
			{
				org.apache.commons.io.FileUtils.copyInputStreamToFile(theConnection.getInputStream(), mDestFile);
				GDCAPI.printLn("Download succeeded 2: " + mDestFile + " for URL=" + mUrl);
			}
			result = mDestFile;
		}
		catch (Exception rethrownExp)
		{
			if (null != mDestFile)
			{
				if (mDestFile.exists())
				{
					GDCAPI.printLn("remove partial download" + " for URL=" + mUrl);
					mDestFile.delete();
				}
			}
			throw rethrownExp;
		}
		return result;
	}

	public String download() throws Exception
	{
		GDCAPI.printLn("Using URL: " + mUrl);
		String result = null;
		int counts = 0;
		while ((counts <= mRetries) && (null == result))
		{
			counts = counts + 1;
			HttpURLConnection connection = null;
			try
			{
				connection = makeConnection();
				if (null == mDestFile)
				{
					result = downloadToString(connection);
				}
				else if (mDestFile.isDirectory())
				{
					result = downloadToDir(connection).getAbsolutePath();
				}
				else if (!mDestFile.isDirectory())
				{
					result = downloadToFile(connection).getAbsolutePath();
				}
				else
				{
					throw new Exception("Unable to determine how to download using " + mDestFile);
				}
				if ((null!=result)&&(null!=mMD5))
				{
					String md5 = new DigestUtils(MessageDigestAlgorithms.MD5).digestAsHex(new File(result));
					if (!md5.equals(mMD5))
					{
						throw new Exception("MD5 '" + md5 + "' does not match expected value '" + mMD5 + " for URL=" + mUrl);
					}
				}
			}
			catch (Exception rethrownExp)
			{
				if (null != connection)
				{
					InputStream errorStr = connection.getErrorStream();
					if (null != errorStr)
					{
						GDCAPI.printWarn("WARNING stream returned: " + IOUtils.toString(errorStr, Charset.defaultCharset()) + " for URL=" + mUrl);
					}
					else
					{
						GDCAPI.printWarn("No WARNING stream returned for URL=" + mUrl);
					}
				}
				GDCAPI.printWarn("exception thrown: " + rethrownExp.toString() + " for URL=" + mUrl);
				if (counts + 1 <= mRetries)
				{
					GDCAPI.printWarn("retrying #" + (counts + 1) + " in one minute for " + mUrl);
					try
					{
						Thread.sleep(1000 * 60);
					}
					catch (Exception exp)
					{
						// ignore
					}
				}
				else
				{
					GDCAPI.printErr("error tried " + counts + " no more retries for " + mUrl);
					throw rethrownExp;
				}
			}
			finally
			{
				if (null != connection)
				{
					connection.disconnect();
				}
			}
		}
		return result;
	}
}
