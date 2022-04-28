package nessusTools.web;

import nessusTools.data.entity.scan.*;
import org.apache.logging.log4j.*;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;


@WebServlet(name = "FoldersServlet", urlPatterns = "/web/folders")
public class FoldersServlet extends HttpServlet implements FieldChecker {
    private static Logger logger = LogManager.getLogger(FoldersServlet.class);

    private List<String> folderFields = new ArrayList<>();
    private List<String> scanFields = new ArrayList<>();

    private int folderBaseline;
    private int scanBaseline;


    @Override
    public void init() {
        Iterator<String> folder = new Folder().toJsonNode().fieldNames();
        Iterator<String> scan = new Scan().toJsonNode().fieldNames();

        while (folder.hasNext()) {
            folderFields.add(folder.next());
        }
        folderBaseline = folderFields.size();

        while (scan.hasNext()) {
            scanFields.add(scan.next());
        }
        scanBaseline = scanFields.size();

        this.getServletContext().setAttribute("folderFields", folderFields);
        this.getServletContext().setAttribute("scanFields", scanFields);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        try {
            List folders = Folder.dao.getAll();
            List scans = Scan.dao.getAll();

            this.checkFields(folders, folderFields, folderBaseline);
            this.checkFields(scans, scanFields, scanBaseline);

            req.setAttribute("folders", folders);
            req.setAttribute("scans", scans);

            // NOTE:  I am using the WEB-INF directory for my jsps so that they are not publicly exposed
            // and cannot be accessed directly (e.g. via /folders.jsp or /entityTable.jsp) but only through
            // the servlet logic
            // https://stackoverflow.com/a/19786283/18476572
            req.getRequestDispatcher("/WEB-INF/folders.jsp").forward(req, res);

        } catch (Exception e) {
            logger.error(e);
            res.setStatus(500);
        }
    }
}
