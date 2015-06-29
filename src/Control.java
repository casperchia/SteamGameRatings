

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import steamParser.GameBean;
import steamParser.GamesManager;
import steamParser.SteamIdManager;

/**
 * Servlet implementation class Control
 */
@WebServlet("/Control")
public class Control extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Control() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Set default page.
		String nextPage = "home.jsp";
		String action = request.getParameter("action");

		if (action == null){
			action = "home";
		}

		if (action.equals("gamesRequest")) {
			String username = request.getParameter("username");
			String steamid = SteamIdManager.getSteamId(username);
			System.out.println(steamid);
			if (steamid == null) {
				// Do something here
				// Print that user does not have vanity id, must use steamid instead.
			} else {
				List<GameBean> games = GamesManager.getGames(steamid);
				request.getSession().setAttribute("games", games);				
			}
			nextPage = "games.jsp";
		}
		
		// Dispatch control.
		RequestDispatcher myRequestDispatcher = request.getRequestDispatcher("/" + nextPage);
		myRequestDispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
