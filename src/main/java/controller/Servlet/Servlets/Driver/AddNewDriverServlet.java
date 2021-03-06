package controller.Servlet.Servlets.Driver;

import org.hibernate.SessionFactory;
import services.Driver.AddNewDriver;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The type Add new driver servlet.
 */
@WebServlet("/AddNewDriver")
public class AddNewDriverServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        SessionFactory sessionFactory = (SessionFactory)
                req.getServletContext().getAttribute("SessionFactory");
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String currentCity = req.getParameter("currentCity");
        if (AddNewDriver.addNewDriver(firstName, lastName, currentCity , sessionFactory)) {
            req.getSession().setAttribute("errorMsg",
                    "<div class=\"alert alert-success\">\n"
                            + "  <a href=\"#\" class=\"close\" "
                            + "data-dismiss=\"alert\""
                            + " aria-label=\"close\">&times;</a>\n"
                            + "  <strong>Success!</strong> New driver added.\n"
                            + "</div>");
            resp.sendRedirect("/Driver");
        } else {
            RequestDispatcher requestDispatcher =
                    req.getRequestDispatcher("/Driver");
            req.setAttribute("errorMsg",
                    "<div class=\"alert alert-success\">\n"
                            + "  <a href=\"#\" class=\"close\" "
                            + "data-dismiss=\"alert\""
                            + " aria-label=\"close\">&times;</a>\n"
                            + "  <strong>Error!</strong> New driver not added\n"
                            + "</div>");
            requestDispatcher.include(req, resp);
        }

    }
}
