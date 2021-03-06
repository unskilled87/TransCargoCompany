package controller.Servlet.Servlets.Driver;

import model.Entities.City;
import model.Entities.Driver;
import model.Entities.User;
import org.hibernate.SessionFactory;
import services.City.GetAllCities;
import services.Driver.GetAllDrivers;
import services.Driver.RemoveDriver;
import services.Driver.UpdateDriver;
import utils.servlet.CheckUserRole;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * The type Driver servlet.
 */
@WebServlet("/Driver")
public class DriverServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        SessionFactory sessionFactory = (SessionFactory)
                req.getServletContext().getAttribute("SessionFactory");
        List<Driver> allDrivers = GetAllDrivers.getAllDrivers(sessionFactory);
        List<City> allCities = GetAllCities.getAllCities(sessionFactory);

        req.setAttribute("cityList", allCities);
        req.setAttribute("resultList", allDrivers);

        User user = (User) req.getSession().getAttribute("user");
        String userRole = CheckUserRole.getUserRole(user);
        if (!userRole.equals("public")) {
            req.getRequestDispatcher(userRole + "/driver.jsp").include(req, resp);
        } else {
            resp.sendRedirect("public/index.jsp");
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        SessionFactory sessionFactory = (SessionFactory)
                req.getServletContext().getAttribute("SessionFactory");
        String delete = req.getParameter("delete");
        if (delete != null) {
            try {
                int id = Integer.parseInt(delete);
                RemoveDriver.removeDriver(id, sessionFactory);
                doGet(req, resp);
            } catch (NumberFormatException e) {
                req.setAttribute("errorMsg",
                        "<div class=\"alert alert-warning\">\n"
                                + "  <a href=\"#\" class=\"close\" "
                                + "data-dismiss=\"alert\""
                                + " aria-label=\"close\">&times;</a>\n"
                                + "<strong>Warning!</strong> Wrong id!\n"
                                + "</div>");
            }
        } else {
            String id = req.getParameter("id");
            String firstName = req.getParameter("firstName");
            String lastName = req.getParameter("lastName");
            String hours = req.getParameter("hours");
            String cityId = req.getParameter("city");
            String driverStatusId = req.getParameter("driverStatus");
            String wagonId = req.getParameter("wagon");
            String orderId = req.getParameter("orderId");
            UpdateDriver.updateDriver(Integer.parseInt(id), sessionFactory,
                    firstName, lastName, hours, cityId,
                    driverStatusId, wagonId, orderId);
            doGet(req, resp);
        }
    }
}
