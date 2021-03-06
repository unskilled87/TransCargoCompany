package controller.Servlet.Servlets.City;

import model.Entities.City;
import model.Entities.User;
import org.hibernate.SessionFactory;
import services.City.GetAllCities;
import services.City.RemoveCity;
import services.City.UpdateCity;
import utils.servlet.CheckUserRole;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * The type City servlet.
 */
@WebServlet("/City")
public class CityServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        SessionFactory sessionFactory = (SessionFactory)
                req.getServletContext().getAttribute("SessionFactory");
        List<City> allCities = GetAllCities.getAllCities(sessionFactory);
        req.setAttribute("resultList", allCities);
        User user = (User) req.getSession().getAttribute("user");
        String userRole = CheckUserRole.getUserRole(user);
        if (!userRole.equals("public")) {
            req.getRequestDispatcher(userRole + "/city.jsp").include(req, resp);
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
                RemoveCity.removeCity(id, sessionFactory);
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
            String cityName = req.getParameter("cityName");
            String distance = req.getParameter("distance");
            UpdateCity.updateCity(Integer.parseInt(id), cityName, sessionFactory);
            doGet(req, resp);
        }
    }
}
