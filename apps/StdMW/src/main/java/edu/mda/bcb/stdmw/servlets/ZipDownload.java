// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.stdmw.servlets;

import com.google.common.io.Files;
import edu.mda.bcb.stdmw.utils.ScanCheck;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "zipdl", urlPatterns =
{
	"/zipdl"
})
public class ZipDownload extends HttpServlet
{

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		try
		{
			ScanCheck.checkForSecurity(request);
			log("Servlet ZipDownload " + MWUrls.M_VERSION);
			String analysis_hash = request.getParameter("analysis_hash");
			ScanCheck.checkForMetaCharacters(analysis_hash);
			String study_hash = request.getParameter("study_hash");
			ScanCheck.checkForMetaCharacters(study_hash);
			if ((null!=analysis_hash)&&(null!=study_hash))
			{
				File dataDir = new File(MWUrls.M_MWB_TEMP);
				checkPathExistsSafely(dataDir, study_hash);
				File subDir = new File(dataDir, study_hash);
				checkPathExistsSafely(subDir, analysis_hash);
				File lastDir = new File(subDir, analysis_hash);
				File [] cf = FileUtils.listFiles(lastDir, new String[] { "zip" }, false).toArray(File[]::new);
				if (cf.length>0)
				{
					File zipFile = cf[0];
					log("ZipDownload zipFile = " + zipFile.getAbsolutePath());
					if (zipFile.exists())
					{
						response.setContentType("application/zip;charset=UTF-8");
						response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"");
						try (OutputStream out = response.getOutputStream())
						{
							Files.copy(zipFile, out);
							out.flush();
						}
						// do not delete. Once a day, anything over an hour old is deleted
						//zipFile.delete();
					}
					else
					{
						throw new Exception("File not found 1" + zipFile.getAbsolutePath());
					}
				}
				else
				{
					throw new Exception("File not found 2");
				}
			}
			else
			{
				throw new Exception("No analysis found");
			}
			log("Servlet ZipDownload returning");
		}
		catch (Exception exp)
		{
			log("ZipDownload", exp);
			response.setStatus(400);
			response.sendError(400);
		}
	}
	
	protected void checkPathExistsSafely(File theDir, String theCheckDir) throws Exception
	{
		String [] dirs = theDir.list();
		ArrayList<String> dirList = new ArrayList<>(Arrays.asList(dirs));
		if (!dirList.contains(theCheckDir))
		{
			throw new Exception("Dir not found:" + theCheckDir);
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo()
	{
		return "Short description";
	}// </editor-fold>

}
