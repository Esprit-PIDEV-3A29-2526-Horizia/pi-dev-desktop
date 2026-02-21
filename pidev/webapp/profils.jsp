<%@ page import="tn.esprit.entities.Profil" %>
<%@ page import="java.util.List" %>

<%
    List<Profil> profilsList = (List<Profil>) request.getAttribute("profilsList");
    if(profilsList == null) profilsList = new java.util.ArrayList<>();
%>

<h2>Liste des Profils</h2>

<table border="1">
    <tr>
        <th>ID</th>
        <th>Nom</th>
        <th>Status</th>
    </tr>

    <% for(Profil p : profilsList) { %>
    <tr>
        <td><%= p.getId() %></td>
        <td><%= p.getNom() %></td>
        <td><%= p.getStatus() %></td>
    </tr>
    <% } %>
</table>

<a href="index.jsp">Retour au menu</a>
