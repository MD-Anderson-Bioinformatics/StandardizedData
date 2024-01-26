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

import edu.mda.bcb.stdmw.startup.Scheduled;
import edu.mda.bcb.stdmw.utils.ScanCheck;
import edu.mda.bcb.stdmwutils.mwdata.Analysis;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.utils.AnalysisUtil;
import edu.mda.bcb.stdmwutils.utils.RefMetUtil;
import edu.mda.bcb.stdmwutils.utils.MetaboliteMapUtil;
import edu.mda.bcb.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bcb.stdmwutils.utils.OtherIdsUtil;
import java.io.IOException;
import java.io.OutputStream;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author TDCasasent
 */
@WebServlet(name = "mtbltmap", urlPatterns =
{
	"/mtbltmap"
})
public class MetaboliteMap extends HttpServlet
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
			log("Servlet MetaboliteMap " + MWUrls.M_VERSION);
			//SummaryUtil summaryUtil = Scheduled.getSummary();
			AnalysisUtil analysisUtil = Scheduled.getAnalysis();
			MetaboliteUtil metaUtil = Scheduled.getMetaUtil();
			RefMetUtil refmetUtil = Scheduled.getRefmetUtil();
			OtherIdsUtil otherIdsUtil = Scheduled.getOtherIdsUtil();
			String study_hash = request.getParameter("study_hash");
			ScanCheck.checkForMetaCharacters(study_hash);
			String hash = request.getParameter("hash");
			ScanCheck.checkForMetaCharacters(hash);
			Analysis analysis = analysisUtil.getAnalysis(hash);
			log("Servlet MetaboliteMap hash = " + hash);
			if (null != analysis)
			{
				response.setContentType("text/tab-separated-values;charset=UTF-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"map_" + analysis.analysis_id + ".tsv\"");
				try (OutputStream out = response.getOutputStream())
				{
					MetaboliteMapUtil mmu = new MetaboliteMapUtil(metaUtil, refmetUtil, otherIdsUtil);
					mmu.streamTsv(out, analysis.analysis_id);
				}
				catch (Exception e)
				{
					System.out.println(e.getClass());
				}
			}
			else
			{
				throw new Exception("No Analysis found");
			}
			log("Servlet MetaboliteMap returning");
		}
		catch (Exception exp)
		{
			log("MetaboliteMap", exp);
			response.setStatus(400);
			response.sendError(400);
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
